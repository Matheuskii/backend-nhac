package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.ItemPedido;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCreateDTO;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.*;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.PedidoRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final LojaRepository lojaRepository;
    private final ProdutoRepository produtoRepository;
    private final StripePaymentService stripePaymentService;
    private final AsaasPaymentService asaasPaymentService;

    public PedidoService(PedidoRepository pedidoRepository, LojaRepository lojaRepository, ProdutoRepository produtoRepository, StripePaymentService stripePaymentService, AsaasPaymentService asaasPaymentService) {
        this.pedidoRepository = pedidoRepository;
        this.lojaRepository = lojaRepository;
        this.produtoRepository = produtoRepository;
        this.stripePaymentService = stripePaymentService;
        this.asaasPaymentService = asaasPaymentService;
    }

    @Transactional
    public br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO finalizarPedido(PedidoCreateDTO dto, Usuario usuarioLogado, String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (pedidoRepository.existsByIdempotencyKey(idempotencyKey)) {
                throw new RegraDeNegocioException("Pedido já processado com esta chave de idempotência.");
            }
        }

        Loja loja = lojaRepository.findByIdAndIsAbertoTrue(dto.lojaId())
                .orElseThrow(() -> new LojaFechadaException(dto.lojaId()));

        if (!loja.isAberto()) {
            throw new LojaFechadaException(dto.lojaId());
        }

        Pedido pedido = dto.toEntity(loja);
        pedido.setIdempotencyKey(idempotencyKey);
        pedido.setUsuarioId(usuarioLogado.getId());

        BigDecimal valorTotalItens = BigDecimal.ZERO;

        for (PedidoCreateDTO.ItemPedidoDTO itemDto : dto.itens()) {
            if (itemDto.quantidade() <= 0) {
                throw new QuantidadeInvalidaException("A quantidade deve ser maior que zero", Map.of("produtoId", itemDto.produtoId(), "quantidade", itemDto.quantidade()));
            }

            Produto produtoReal = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new ProdutoNaoEncontradoException(itemDto.produtoId(), loja.getId()));

            if (!produtoReal.getLoja().getId().equals(loja.getId())) {
                throw new RegraDeNegocioException("O produto '" + produtoReal.getNome() + "' não pertence à loja selecionada.");
            }

            if (!produtoReal.isAtivo()) {
                throw new ProdutoInativoException("O produto '" + produtoReal.getNome() + "' está inativo.", Map.of("produtoId", produtoReal.getId()));
            }

            if (produtoReal.getEstoque() == null || produtoReal.getEstoque() < itemDto.quantidade()) {
                throw new EstoqueInsuficienteException(produtoReal.getId(), itemDto.quantidade(), produtoReal.getEstoque() == null ? 0 : produtoReal.getEstoque());
            }

            // Atualiza o estoque
            produtoReal.setEstoque(produtoReal.getEstoque() - itemDto.quantidade());
            produtoRepository.save(produtoReal);

            ItemPedido novoItem = itemDto.toEntity(produtoReal);
            BigDecimal precoReal = produtoReal.getPreco();
            novoItem.setPrecoHistorico(precoReal);

            BigDecimal subtotal = precoReal.multiply(BigDecimal.valueOf(novoItem.getQuantidade()));
            valorTotalItens = valorTotalItens.add(subtotal);

            pedido.adicionarItem(novoItem);
        }

        if (pedido.getEnderecoEntrega() == null) {
            throw new CampoObrigatorioFaltandoException("enderecoEntrega");
        }

        BigDecimal taxaFrete = loja.getDadosOperacionais() != null
                && loja.getDadosOperacionais().getTaxaEntregaBase() != null
                ? loja.getDadosOperacionais().getTaxaEntregaBase()
                : new BigDecimal("5.00");
        pedido.setTaxaFrete(taxaFrete);
        pedido.setValorTotal(valorTotalItens.add(taxaFrete));

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        try {
            if ("PIX".equalsIgnoreCase(pedido.getFormaPagamento())) {
                if (dto.cpfPagador() == null || dto.cpfPagador().isBlank()) {
                    throw new RegraDeNegocioException("O CPF do pagador é obrigatório para pagamento via PIX.");
                }
                return asaasPaymentService.criarCobrancaPix(
                        pedidoSalvo, usuarioLogado.getNome(), usuarioLogado.getEmail(), dto.cpfPagador());
            } else if ("CARTAO".equalsIgnoreCase(pedido.getFormaPagamento()) || 
                       "GOOGLE_PAY".equalsIgnoreCase(pedido.getFormaPagamento()) ||
                       "STRIPE".equalsIgnoreCase(pedido.getFormaPagamento())) {
                
                return stripePaymentService.criarPaymentIntentCartao(pedidoSalvo);
            }
            
            return new br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO(pedidoSalvo.getId(), null, null, null);
        } catch (Exception e) {
            // Em caso de erro de pagamento, o @Transactional garante rollback e volta estoque
            throw new PagamentoRecusadoException("Não foi possível processar seu pagamento", e);
        }
    }

    @Transactional
    public void marcarComoPagoPorPaymentIntentId(String paymentIntentId) {
        Pedido pedido = pedidoRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido com PaymentIntent " + paymentIntentId + " não encontrado."));
        
        pedido.alterarStatus(StatusPedido.PAGO);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void marcarComoPagoPorAsaasPaymentId(String asaasPaymentId) {
        Pedido pedido = pedidoRepository.findByAsaasPaymentId(asaasPaymentId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido com Asaas Payment ID " + asaasPaymentId + " não encontrado."));
        
        pedido.alterarStatus(StatusPedido.PAGO);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void cancelarPorFalhaPagamentoAsaas(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido não encontrado para cancelamento por falha de pagamento Asaas."));

        pedido.alterarStatus(StatusPedido.CANCELADO);
        devolverEstoque(pedido);
        pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedido(String id, String usuarioIdLogado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido não encontrado."));

        if (!pedido.getUsuarioId().equals(usuarioIdLogado)) {
            throw new AcessoNegadoException("Acesso negado: você não tem permissão para visualizar este pedido.");
        }

        return new PedidoResponseDTO(pedido);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResumoDTO> listarMeusPedidos(String usuarioId, Pageable pageable) {
        Page<Pedido> page = pedidoRepository.findByUsuarioId(usuarioId, pageable);
        return page.map(PedidoResumoDTO::new);
    }

    @Transactional
    public void atualizarStatus(String pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido não encontrado."));

        pedido.alterarStatus(novoStatus);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void cancelarPedido(String pedidoId, String usuarioIdLogado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido não encontrado."));

        if (!pedido.getUsuarioId().equals(usuarioIdLogado)) {
            throw new AcessoNegadoException("Acesso negado: você não tem permissão para cancelar este pedido.");
        }

        pedido.alterarStatus(StatusPedido.CANCELADO);
        devolverEstoque(pedido);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void marcarComoCanceladoPorFalhaDePagamento(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido não encontrado para cancelamento por webhook."));

        pedido.alterarStatus(StatusPedido.CANCELADO);
        devolverEstoque(pedido);
        pedidoRepository.save(pedido);
    }
    
    private void devolverEstoque(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (produto.getEstoque() != null) {
                produto.setEstoque(produto.getEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }
        }
    }
}