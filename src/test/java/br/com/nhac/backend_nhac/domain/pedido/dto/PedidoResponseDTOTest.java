package br.com.nhac.backend_nhac.domain.pedido.dto;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.EnderecoEntrega;
import br.com.nhac.backend_nhac.domain.pedido.ItemPedido;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PedidoResponseDTOTest {

    @Test
    @DisplayName("Deve construir PedidoResponseDTO a partir de uma entidade Pedido")
    void deveConstruirAPartirDePedido() {

        Loja loja = new Loja();
        loja.setId("loja_1");
        loja.setNome("Minha Loja");

        Pedido pedido = new Pedido();
        pedido.setId("ped_1");
        pedido.setUsuarioId("user_1");
        pedido.setLoja(loja);
        pedido.setFormaPagamento("DINHEIRO");
        pedido.setTaxaFrete(new BigDecimal("5.00"));
        pedido.setTrocoPara(new BigDecimal("50.00"));
        pedido.setObservacao("Obs");
        pedido.setValorTotal(new BigDecimal("30.00"));
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setCriadoEm(Instant.now());
        pedido.setEnderecoEntrega(new EnderecoEntrega("Rua X", "1", "B", "C", "SP", "0", null));
        pedido.setItens(new ArrayList<>());

        Produto produto = new Produto();
        produto.setId("prod_1");

        ItemPedido item = new ItemPedido();
        item.setId("item_1");
        item.setProduto(produto);
        item.setNome("Burger");
        item.setImagemUrl("img.jpg");
        item.setQuantidade(2);
        item.setPrecoHistorico(new BigDecimal("15.00"));
        pedido.getItens().add(item);

        PedidoResponseDTO dto = new PedidoResponseDTO(pedido);

        assertEquals("ped_1", dto.id());
        assertEquals("user_1", dto.usuarioId());
        assertEquals("loja_1", dto.lojaId());
        assertEquals("Minha Loja", dto.lojaNome());
        assertEquals(StatusPedido.PENDENTE, dto.status());
        assertEquals(new BigDecimal("30.00"), dto.valorTotal());
        assertEquals(new BigDecimal("5.00"), dto.taxaFrete());
        assertEquals("DINHEIRO", dto.formaPagamento());
        assertEquals(new BigDecimal("50.00"), dto.trocoPara());
        assertEquals("Obs", dto.observacao());
        assertNotNull(dto.criadoEm());
        assertEquals(1, dto.itens().size());

        PedidoResponseDTO.ItemPedidoResponseDTO itemDto = dto.itens().get(0);
        assertEquals("prod_1", itemDto.produtoId());
        assertEquals("Burger", itemDto.nome());
        assertEquals("img.jpg", itemDto.imagemUrl());
        assertEquals(2, itemDto.quantidade());
        assertEquals(new BigDecimal("15.00"), itemDto.preco());
    }
}
