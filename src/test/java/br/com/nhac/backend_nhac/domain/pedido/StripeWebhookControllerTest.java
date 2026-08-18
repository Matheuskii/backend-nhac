package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.services.PedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = StripeWebhookController.class,
    properties = {"stripe.webhook.secret=whsec_test_secret_aqui"}
)
@AutoConfigureMockMvc(addFilters = false)
public class StripeWebhookControllerTest {

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
    @DisplayName("Deve retornar 400 Bad Request se a assinatura do Stripe for inválida")
    void deveRetornar400SeAssinaturaInvalida() throws Exception {
        String payload = "{\"type\": \"payment_intent.payment_failed\"}";
        String signature = "t=123,v1=assinatura_invalida";

        mockMvc.perform(post("/api/v1/webhooks/stripe")
                .header("Stripe-Signature", signature)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request se faltar o header de assinatura")
    void deveRetornar400SeFaltarHeader() throws Exception {
        String payload = "{\"type\": \"payment_intent.payment_failed\"}";

        // A versão do StripeWebhookController pode ou não exigir o Header.
        // O Webhook.constructEvent joga IllegalArgumentException se o header for nulo ou vazio, 
        // mas o Spring pode rejeitar antes se houver @RequestHeader sem required=false.
        // No StripeWebhookController atual, @RequestHeader("Stripe-Signature") String sigHeader
        // é exigido por padrão, então lançará 400.
        mockMvc.perform(post("/api/v1/webhooks/stripe")
                .content(payload))
                .andExpect(status().isBadRequest());
    }

}
