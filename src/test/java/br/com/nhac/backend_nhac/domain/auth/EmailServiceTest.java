package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.exceptions.ServicoIndisponivelException;
import br.com.nhac.backend_nhac.infra.email.BrevoApiException;
import br.com.nhac.backend_nhac.infra.email.BrevoEmailClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private BrevoEmailClient brevoEmailClient;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Quando mockMode for true, não deve chamar o BrevoEmailClient")
    void quandoMockModeTrueNaoChamaCliente() {
        ReflectionTestUtils.setField(emailService, "mockMode", true);

        emailService.enviarEmailHtml("cliente@nhac.com", "Assunto", "<p>Texto</p>");

        verifyNoInteractions(brevoEmailClient);
    }

    @Test
    @DisplayName("Quando mockMode for false e envio for bem-sucedido, deve chamar o BrevoEmailClient 1 vez")
    void quandoEnvioComSucessoChamaUmaVez() {
        ReflectionTestUtils.setField(emailService, "mockMode", false);
        doNothing().when(brevoEmailClient).enviar("cliente@nhac.com", "Assunto", "<p>Texto</p>");

        assertDoesNotThrow(() ->
                emailService.enviarEmailHtml("cliente@nhac.com", "Assunto", "<p>Texto</p>"));

        verify(brevoEmailClient, times(1)).enviar("cliente@nhac.com", "Assunto", "<p>Texto</p>");
    }

    @Test
    @DisplayName("Quando ocorrer falha NÃO-retryable, deve lançar ServicoIndisponivelException chamando apenas 1 vez (sem retries)")
    void quandoFalhaNaoRetryableFalhaImediatamente() {
        ReflectionTestUtils.setField(emailService, "mockMode", false);
        doThrow(new BrevoApiException(false, 401, "Unauthorized"))
                .when(brevoEmailClient).enviar(anyString(), anyString(), anyString());

        ServicoIndisponivelException ex = assertThrows(ServicoIndisponivelException.class, () ->
                emailService.enviarEmailHtml("cliente@nhac.com", "Assunto", "<p>Texto</p>"));

        assertEquals("Não foi possível enviar o e-mail de verificação. O serviço de e-mail pode estar indisponível.", ex.getMessage());
        verify(brevoEmailClient, times(1)).enviar("cliente@nhac.com", "Assunto", "<p>Texto</p>");
    }

    @Test
    @DisplayName("Quando ocorrer falha RETRYABLE, deve tentar 3 vezes antes de lançar ServicoIndisponivelException")
    void quandoFalhaRetryableTentaTresVezes() {
        ReflectionTestUtils.setField(emailService, "mockMode", false);
        doThrow(new BrevoApiException(true, 500, "Internal Server Error"))
                .when(brevoEmailClient).enviar(anyString(), anyString(), anyString());

        ServicoIndisponivelException ex = assertThrows(ServicoIndisponivelException.class, () ->
                emailService.enviarEmailHtml("cliente@nhac.com", "Assunto", "<p>Texto</p>"));

        assertEquals("Não foi possível enviar o e-mail de verificação. O serviço de e-mail pode estar indisponível.", ex.getMessage());
        verify(brevoEmailClient, times(3)).enviar("cliente@nhac.com", "Assunto", "<p>Texto</p>");
    }
}
