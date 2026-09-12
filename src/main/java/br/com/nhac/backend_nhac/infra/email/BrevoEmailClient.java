package br.com.nhac.backend_nhac.infra.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class BrevoEmailClient {

    private static final Logger logger = LoggerFactory.getLogger(BrevoEmailClient.class);

    private final RestClient brevoRestClient;
    private final BrevoProperties properties;

    public BrevoEmailClient(RestClient brevoRestClient, BrevoProperties properties) {
        this.brevoRestClient = brevoRestClient;
        this.properties = properties;
    }

    public void enviar(String destinatario, String assunto, String htmlConteudo) {
        BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoEmailRequest.Sender(properties.getSenderEmail(), properties.getSenderName()),
                List.of(new BrevoEmailRequest.Recipient(destinatario)),
                assunto,
                htmlConteudo
        );

        try {
            BrevoEmailResponse response = brevoRestClient.post()
                    .uri("/smtp/email")
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                        int code = res.getStatusCode().value();
                        String responseBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        logger.error("Erro 4xx ao enviar e-mail via Brevo (status {}): {}", code, responseBody);
                        boolean retryable = (code == 429);
                        throw new BrevoApiException(retryable, code, "Erro de cliente ao enviar e-mail via Brevo (HTTP " + code + ")");
                    })
                    .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                        int code = res.getStatusCode().value();
                        String responseBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        logger.error("Erro 5xx no servidor da Brevo (status {}): {}", code, responseBody);
                        throw new BrevoApiException(true, code, "Erro no servidor da Brevo (HTTP " + code + ")");
                    })
                    .body(BrevoEmailResponse.class);

            if (response != null && response.messageId() != null) {
                logger.info("E-mail enviado com sucesso via Brevo para {}. MessageId: {}", destinatario, response.messageId());
            } else {
                logger.info("E-mail enviado com sucesso via Brevo para {}.", destinatario);
            }
        } catch (BrevoApiException e) {
            throw e;
        } catch (ResourceAccessException e) {
            logger.error("Falha de rede ou timeout ao comunicar com a Brevo para {}: {}", destinatario, e.getMessage());
            throw new BrevoApiException(true, 0, "Falha de rede/timeout com o serviço da Brevo", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao enviar e-mail via Brevo para {}: {}", destinatario, e.getMessage());
            throw new BrevoApiException(true, 0, "Erro inesperado ao comunicar com o serviço da Brevo", e);
        }
    }
}
