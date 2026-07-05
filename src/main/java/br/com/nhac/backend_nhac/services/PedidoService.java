package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.ItemPedido;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.PedidoRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final LojaRepository lojaRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, LojaRepository lojaRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.lojaRepository = lojaRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public String finalizarPedido(PedidoCreateDTO dto, String usuarioIdLogado) {

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

        pedido.setTrocoPara(dto.trocoPara());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        return pedidoSalvo.getId();
    }
}