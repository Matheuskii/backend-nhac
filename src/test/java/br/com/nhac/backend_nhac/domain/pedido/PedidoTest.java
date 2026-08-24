package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    @Test
    @DisplayName("Deve testar getters e setters do Pedido")
    void deveTestarSetters() {
        Pedido pedido = new Pedido();
        pedido.setId("ped_1");
        pedido.setUsuarioId("user_1");
        
        Loja loja = new Loja();
        loja.setId("loja_1");
        pedido.setLoja(loja);
        
        pedido.setFormaPagamento("PIX");
        pedido.setObservacao("Nenhuma");
        pedido.setValorTotal(new BigDecimal("100.00"));
        pedido.setTaxaFrete(new BigDecimal("10.00"));
        pedido.setTrocoPara(BigDecimal.ZERO);
        pedido.setStatus(StatusPedido.PAGO);
        
        EnderecoEntrega endereco = new EnderecoEntrega("Rua", "1", "B", "C", "SP", "0000000", null);
        pedido.setEnderecoEntrega(endereco);
        
        Instant agora = Instant.now();
        pedido.setCriadoEm(agora);
        pedido.setStripePaymentIntentId("pi_123");
        pedido.setAsaasPaymentId("pay_123");
        pedido.setIdempotencyKey("idempotency_123");
        pedido.setItens(new ArrayList<>());

        assertEquals("ped_1", pedido.getId());
        assertEquals("user_1", pedido.getUsuarioId());
        assertEquals(loja, pedido.getLoja());
        assertEquals("PIX", pedido.getFormaPagamento());
        assertEquals("Nenhuma", pedido.getObservacao());
        assertEquals(new BigDecimal("100.00"), pedido.getValorTotal());
        assertEquals(new BigDecimal("10.00"), pedido.getTaxaFrete());
        assertEquals(BigDecimal.ZERO, pedido.getTrocoPara());
        assertEquals(StatusPedido.PAGO, pedido.getStatus());
        assertEquals(endereco, pedido.getEnderecoEntrega());
        assertEquals(agora, pedido.getCriadoEm());
        assertEquals("pi_123", pedido.getStripePaymentIntentId());
        assertEquals("pay_123", pedido.getAsaasPaymentId());
        assertEquals("idempotency_123", pedido.getIdempotencyKey());
        assertNotNull(pedido.getItens());
    }
}
