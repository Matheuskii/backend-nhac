package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.domain.produto.Produto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemPedidoTest {

    @Test
    @DisplayName("Deve instanciar ItemPedido e usar setters")
    void deveInstanciarETestarSetters() {
        Produto produto = new Produto();
        produto.setId("prod_1");

        Pedido pedido = new Pedido();
        pedido.setId("ped_1");

        ItemPedido item = new ItemPedido();
        
        item.setId("item_1");
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setNome("Burger");
        item.setImagemUrl("img.png");
        item.setQuantidade(5);
        item.setPrecoHistorico(new BigDecimal("10.00"));

        assertEquals("item_1", item.getId());
        assertEquals(pedido, item.getPedido());
        assertEquals(produto, item.getProduto());
        assertEquals("Burger", item.getNome());
        assertEquals("img.png", item.getImagemUrl());
        assertEquals(5, item.getQuantidade());
        assertEquals(new BigDecimal("10.00"), item.getPrecoHistorico());
    }
}
