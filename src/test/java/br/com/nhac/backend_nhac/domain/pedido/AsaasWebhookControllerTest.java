package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.services.PedidoService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = AsaasWebhookController.class,
    properties = {"asaas.webhook.token=test_webhook_token"}
)
@AutoConfigureMockMvc(addFilters = false)
public class AsaasWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private br.com.nhac.backend_nhac.infra.security.TokenService tokenService;

    @MockitoBean
    private br.com.nhac.backend_nhac.repositories.UsuarioRepository usuarioRepository;

    // Ignora a segurança apenas para o Webhook no ambiente de teste de Controller
    @TestConfiguration
    static class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando API key do header for inválida")
    void deveRetornar401SeApiKeyInvalida() throws Exception {
        String payload = "{\"notification\": \"PAYMENT_RECEIVED\"}";
        String invalidApiKey = "invalid_api_key";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", invalidApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());

        verify(pedidoService, never()).marcarComoPagoPorAsaasPaymentId(anyString());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando notificação estiver ausente")
    void deveRetornar400SeNotificacaoAusente() throws Exception {
        String payload = "{}";
        String validToken = "test_webhook_token";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve processar PAYMENT_RECEIVED e marcar pedido como pago")
    void deveProcessarPagamentoRecebido() throws Exception {
        String pedidoId = "pedido-123";
        String asaasPaymentId = "pay_123456";
        String payload = String.format(
            "{\"notification\": \"PAYMENT_RECEIVED\", \"payment\": {\"id\": \"%s\", \"externalReference\": \"%s\"}}",
            asaasPaymentId, pedidoId
        );
        String validToken = "test_webhook_token";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        verify(pedidoService).marcarComoPagoPorAsaasPaymentId(asaasPaymentId);
        verify(pedidoService, never()).cancelarPorFalhaPagamentoAsaas(anyString());
    }

    @Test
    @DisplayName("Deve processar PAYMENT_OVERDUE e cancelar pedido")
    void deveProcessarPagamentoVencido() throws Exception {
        String pedidoId = "pedido-456";
        String payload = String.format(
            "{\"notification\": \"PAYMENT_OVERDUE\", \"payment\": {\"externalReference\": \"%s\"}}",
            pedidoId
        );
        String validToken = "test_webhook_token";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        verify(pedidoService).cancelarPorFalhaPagamentoAsaas(pedidoId);
        verify(pedidoService, never()).marcarComoPagoPorAsaasPaymentId(anyString());
    }

    @Test
    @DisplayName("Deve processar PAYMENT_CANCELLED e cancelar pedido")
    void deveProcessarPagamentoCancelado() throws Exception {
        String pedidoId = "pedido-789";
        String payload = String.format(
            "{\"notification\": \"PAYMENT_CANCELLED\", \"payment\": {\"externalReference\": \"%s\"}}",
            pedidoId
        );
        String validToken = "test_webhook_token";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        verify(pedidoService).cancelarPorFalhaPagamentoAsaas(pedidoId);
        verify(pedidoService, never()).marcarComoPagoPorAsaasPaymentId(anyString());
    }

    @Test
    @DisplayName("Deve ignorar eventos não tratados sem erro")
    void deveIgnorarEventosNaoTratados() throws Exception {
        String payload = "{\"notification\": \"UNKNOWN_EVENT\", \"payment\": {\"externalReference\": \"pedido-999\"}}";
        String validToken = "test_webhook_token";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        verify(pedidoService, never()).marcarComoPagoPorAsaasPaymentId(anyString());
        verify(pedidoService, never()).cancelarPorFalhaPagamentoAsaas(anyString());
    }

    @Test
    @DisplayName("Deve extrair ID do pedido de referência externa com prefixo")
    void deveExtrairPedidoIdComPrefixo() throws Exception {
        String pedidoId = "abc-123-def";
        String externalReference = "pedidoId_" + pedidoId;
        String payload = String.format(
            "{\"notification\": \"PAYMENT_RECEIVED\", \"payment\": {\"id\": \"pay_999\", \"externalReference\": \"%s\"}}",
            externalReference
        );
        String validToken = "test_webhook_token";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        // O controller deve extrair apenas o ID sem o prefixo
        verify(pedidoService).marcarComoPagoPorAsaasPaymentId("pay_999");
    }

    @Test
    @DisplayName("Deve usar formato alternativo de evento no payload")
    void deveUsarFormatoAlternativoEvento() throws Exception {
        String pedidoId = "pedido-alt";
        String payload = String.format(
            "{\"event\": \"PAYMENT_RECEIVED\", \"payment\": {\"id\": \"pay_alt\", \"externalReference\": \"%s\"}}",
            pedidoId
        );
        String validToken = "test_webhook_token";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        verify(pedidoService).marcarComoPagoPorAsaasPaymentId("pay_alt");
    }
}
