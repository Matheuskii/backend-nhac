package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.ItemPedido;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCreateDTO;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.PedidoRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResponseDTO;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final LojaRepository lojaRepository;
    private final ProdutoRepository produtoRepository;
    private final StripePaymentService stripePaymentService;

    public PedidoService(PedidoRepository pedidoRepository, LojaRepository lojaRepository, ProdutoRepository produtoRepository, StripePaymentService stripePaymentService) {
        this.pedidoRepository = pedidoRepository;
        this.lojaRepository = lojaRepository;
        this.produtoRepository = produtoRepository;
        this.stripePaymentService = stripePaymentService;
    }

    @Transactional
    public br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO finalizarPedido(PedidoCreateDTO dto, String usuarioIdLogado) {

        Loja loja = lojaRepository.findByIdAndIsAbertoTrue(dto.lojaId())
                .orElseThrow(() -> new IdNaoEncontradoException("A loja informada não existe ou está fechada."));

        Pedido pedido = dto.toEntity(loja);


        pedido.setUsuarioId(usuarioIdLogado);

        BigDecimal valorTotalItens = BigDecimal.ZERO;

        for (PedidoCreateDTO.ItemPedidoDTO itemDto : dto.itens()) {

            Produto produtoReal = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new IdNaoEncontradoException(
                            "O produto com ID '" + itemDto.produtoId() + "' não existe no catálogo."
                    ));

            if (!produtoReal.getLoja().getId().equals(loja.getId())) {
                throw new IllegalArgumentException("O produto '" + produtoReal.getNome() + "' não pertence à loja selecionada.");
            }

            ItemPedido novoItem = itemDto.toEntity(produtoReal);

            BigDecimal precoReal = produtoReal.getPreco();
            novoItem.setPrecoHistorico(precoReal);

            BigDecimal subtotal = precoReal.multiply(BigDecimal.valueOf(novoItem.getQuantidade()));
            valorTotalItens = valorTotalItens.add(subtotal);

            pedido.adicionarItem(novoItem);
        }

        BigDecimal taxaFrete = loja.getDadosOperacionais() != null
                && loja.getDadosOperacionais().getTaxaEntregaBase() != null
                ? loja.getDadosOperacionais().getTaxaEntregaBase()
                : new BigDecimal("5.00");
        pedido.setTaxaFrete(taxaFrete);
        pedido.setValorTotal(valorTotalItens.add(taxaFrete));

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Se for PIX, já cria o PaymentIntent no Stripe
        if ("PIX".equalsIgnoreCase(pedido.getFormaPagamento())) {
            // Isso também salva o intent_id na entidade
            // E retorna o DTO pronto com o CopiaECola
            return stripePaymentService.criarPaymentIntentPix(pedidoSalvo);
        }

        // Caso seja outra forma que não exija integração, só retorna o ID
        return new br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCriadoDTO(pedidoSalvo.getId(), null, null, null);
    }

    @Transactional
    public void marcarComoPagoPorPaymentIntentId(String paymentIntentId) {
        Pedido pedido = pedidoRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido com PaymentIntent " + paymentIntentId + " não encontrado."));
        
        pedido.setStatus(br.com.nhac.backend_nhac.domain.pedido.StatusPedido.PAGO);
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

        validarTransicaoDeStatus(pedido.getStatus(), novoStatus);
        
        pedido.setStatus(novoStatus);
        pedidoRepository.save(pedido);
    }

    private void validarTransicaoDeStatus(StatusPedido atual, StatusPedido novoStatus) {
        if (atual == novoStatus) {
            throw new RegraDeNegocioException("O pedido já está no status " + novoStatus);
        }

        boolean transicaoValida = switch (atual) {
            case PENDENTE -> novoStatus == StatusPedido.PREPARANDO;
            case PREPARANDO -> novoStatus == StatusPedido.SAIU_ENTREGA;
            case SAIU_ENTREGA -> novoStatus == StatusPedido.ENTREGUE;
            case ENTREGUE, CANCELADO -> false; // Estados finais
        };

        if (!transicaoValida) {
            throw new RegraDeNegocioException(
                    String.format("Transição de status inválida de %s para %s", atual, novoStatus)
            );
        }
    }

    @Transactional
    public void cancelarPedido(String pedidoId, String usuarioIdLogado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido não encontrado."));

        if (!pedido.getUsuarioId().equals(usuarioIdLogado)) {
            throw new AcessoNegadoException("Acesso negado: você não tem permissão para cancelar este pedido.");
        }

        if (pedido.getStatus() != StatusPedido.PENDENTE && pedido.getStatus() != StatusPedido.PREPARANDO) {
            throw new RegraDeNegocioException("Não é possível cancelar um pedido que já saiu para entrega ou foi entregue.");
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void marcarComoCanceladoPorFalhaDePagamento(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido não encontrado para cancelamento por webhook."));

        if (pedido.getStatus() != StatusPedido.PENDENTE && pedido.getStatus() != StatusPedido.PREPARANDO) {
            // Se já foi entregue ou saiu para entrega, idealmente o fluxo de negócio seria mais complexo,
            // mas de acordo com os requisitos simplificados vamos lançar regra de negócio.
            throw new RegraDeNegocioException("Falha de pagamento recebida, mas o pedido não está em um estado que permite cancelamento.");
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }
}