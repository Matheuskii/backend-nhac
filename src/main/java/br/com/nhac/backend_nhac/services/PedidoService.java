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
    public String finalizarPedido(PedidoCreateDTO dto) {

        Loja loja = lojaRepository.findByIdAndIsAbertoTrue(dto.lojaId())
                .orElseThrow(() -> new IdNaoEncontradoException("A loja informada não existe ou está fechada no momento."));

        Pedido pedido = dto.toEntity(loja);

        for (PedidoCreateDTO.ItemPedidoDTO itemDto : dto.itens()) {

            Produto produtoReferencia = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new IdNaoEncontradoException(
                            "O produto com ID '" + itemDto.produtoId() + "' não existe no catálogo."
                    ));
            ItemPedido novoItem = itemDto.toEntity(produtoReferencia);

            pedido.adicionarItem(novoItem);
        }

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        return pedidoSalvo.getId();
    }
}