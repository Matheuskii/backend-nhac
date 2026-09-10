package br.com.nhac.backend_nhac.infra.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BrevoEmailClientTest {

    private BrevoProperties properties;
    private BrevoEmailClient emailClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        properties = new BrevoProperties();
        properties.setApiKey("chave-teste-123");
        properties.setApiBaseUrl("https://api.brevo.com/v3");
        properties.setSenderEmail("noreply@nhac.com.br");
        properties.setSenderName("Nhac Delivery");
        properties.setTimeoutSeconds(5);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getApiBaseUrl())
                .defaultHeader("api-key", properties.getApiKey());

        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        emailClient = new BrevoEmailClient(restClient, properties);
    }

    @Test
    @DisplayName("Deve enviar e-mail com sucesso para a API da Brevo (201 Created)")
    void deveEnviarEmailComSucesso() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "chave-teste-123"))
                .andExpect(jsonPath("$.sender.email").value("noreply@nhac.com.br"))
                .andExpect(jsonPath("$.sender.name").value("Nhac Delivery"))
                .andExpect(jsonPath("$.to[0].email").value("cliente@nhac.com"))
                .andExpect(jsonPath("$.subject").value("Recuperação de Senha"))
                .andExpect(jsonPath("$.htmlContent").value("<p>Seu código é 123456</p>"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"messageId\": \"<msg-123@brevo>\"}"));

        assertDoesNotThrow(() ->
                emailClient.enviar("cliente@nhac.com", "Recuperação de Senha", "<p>Seu código é 123456</p>"));

        mockServer.verify();
    }

    @Test
    @DisplayName("Deve lançar BrevoApiException NÃO retryable quando API retornar 401 Unauthorized")
    void deveLancarExcecaoNaoRetryableQuando401() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": \"unauthorized\", \"message\": \"Key not found\"}"));

        BrevoApiException ex = assertThrows(BrevoApiException.class, () ->
                emailClient.enviar("cliente@nhac.com", "Assunto", "<p>Texto</p>"));

        assertFalse(ex.isRetryable());
        assertEquals(401, ex.getStatusCode());
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve lançar BrevoApiException NÃO retryable quando API retornar 400 Bad Request")
    void deveLancarExcecaoNaoRetryableQuando400() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": \"invalid_parameter\", \"message\": \"invalid email\"}"));

        BrevoApiException ex = assertThrows(BrevoApiException.class, () ->
                emailClient.enviar("invalido", "Assunto", "<p>Texto</p>"));

        assertFalse(ex.isRetryable());
        assertEquals(400, ex.getStatusCode());
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve lançar BrevoApiException RETRYABLE quando API retornar 429 Too Many Requests")
    void deveLancarExcecaoRetryableQuando429() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": \"rate_limit_exceeded\", \"message\": \"Too many requests\"}"));

        BrevoApiException ex = assertThrows(BrevoApiException.class, () ->
                emailClient.enviar("cliente@nhac.com", "Assunto", "<p>Texto</p>"));

        assertTrue(ex.isRetryable());
        assertEquals(429, ex.getStatusCode());
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve lançar BrevoApiException RETRYABLE quando API retornar 500 Internal Server Error")
    void deveLancarExcecaoRetryableQuando500() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": \"internal_error\", \"message\": \"Service temporarily down\"}"));

        BrevoApiException ex = assertThrows(BrevoApiException.class, () ->
                emailClient.enviar("cliente@nhac.com", "Assunto", "<p>Texto</p>"));

        assertTrue(ex.isRetryable());
        assertEquals(500, ex.getStatusCode());
        mockServer.verify();
    }
}
