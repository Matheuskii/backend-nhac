package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.exceptions.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class StatusPedidoTransicoesTest {

    @ParameterizedTest(name = "Transição válida de {0} para {1}")
    @CsvSource({
            "PENDENTE, PAGO",
            "PENDENTE, CANCELADO",
            "PAGO, PREPARANDO",
            "PAGO, CANCELADO",
            "PREPARANDO, SAIU_ENTREGA",
            "PREPARANDO, CANCELADO",
            "SAIU_ENTREGA, ENTREGUE"
    })
    @DisplayName("Deve permitir transições válidas de status")
    void devePermitirTransicoesValidas(StatusPedido atual, StatusPedido novo) {
        assertTrue(atual.podeMudarPara(novo));
    }

    @ParameterizedTest(name = "Transição inválida de {0} para {1}")
    @CsvSource({
            "PENDENTE, PREPARANDO",
            "PENDENTE, SAIU_ENTREGA",
            "PENDENTE, ENTREGUE",
            "PAGO, PENDENTE",
            "PAGO, SAIU_ENTREGA",
            "PAGO, ENTREGUE",
            "PREPARANDO, PENDENTE",
            "PREPARANDO, PAGO",
            "PREPARANDO, ENTREGUE",
            "SAIU_ENTREGA, PENDENTE",
            "SAIU_ENTREGA, PAGO",
            "SAIU_ENTREGA, PREPARANDO",
            "SAIU_ENTREGA, CANCELADO",
            "ENTREGUE, PENDENTE",
            "ENTREGUE, PAGO",
            "ENTREGUE, PREPARANDO",
            "ENTREGUE, SAIU_ENTREGA",
            "ENTREGUE, CANCELADO",
            "CANCELADO, PENDENTE",
            "CANCELADO, PAGO",
            "CANCELADO, PREPARANDO",
            "CANCELADO, SAIU_ENTREGA",
            "CANCELADO, ENTREGUE"
    })
    @DisplayName("Deve rejeitar transições inválidas lançando TransicaoStatusInvalidaException")
    void deveRejeitarTransicoesInvalidas(StatusPedido atual, StatusPedido novo) {
        TransicaoStatusInvalidaException ex = assertThrows(
                TransicaoStatusInvalidaException.class,
                () -> atual.podeMudarPara(novo)
        );
        assertTrue(ex.getMessage().contains("Transição de status inválida"));
        assertEquals(atual.name(), ex.getDetails().get("statusAtual"));
        assertEquals(novo.name(), ex.getDetails().get("statusNovo"));
    }

    @ParameterizedTest(name = "Mesmo status: {0} -> {0}")
    @CsvSource({
            "PENDENTE",
            "PAGO",
            "PREPARANDO",
            "SAIU_ENTREGA",
            "ENTREGUE",
            "CANCELADO"
    })
    @DisplayName("Deve rejeitar transição para o mesmo status")
    void deveRejeitarTransicaoParaMesmoStatus(StatusPedido status) {
        TransicaoStatusInvalidaException ex = assertThrows(
                TransicaoStatusInvalidaException.class,
                () -> status.podeMudarPara(status)
        );
        assertTrue(ex.getMessage().contains("já está no status"));
    }
}
