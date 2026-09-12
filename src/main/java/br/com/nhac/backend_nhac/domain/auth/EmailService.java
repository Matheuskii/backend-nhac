package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.exceptions.ServicoIndisponivelException;
import br.com.nhac.backend_nhac.infra.email.BrevoApiException;
import br.com.nhac.backend_nhac.infra.email.BrevoEmailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final BrevoEmailClient brevoEmailClient;

    @Value("${nhac.email.mock-mode:true}")
    private boolean mockMode;

    public EmailService(BrevoEmailClient brevoEmailClient) {
        this.brevoEmailClient = brevoEmailClient;
    }

    @Async
    public void enviarEmailHtml(String para, String assunto, String htmlConteudo) {
        if (mockMode) {
            logger.info("=================================================");
            logger.info("[E-MAIL MOCK - NHAC DELIVERY]");
            logger.info("Destinatário: {}", para);
            logger.info("Assunto: {}", assunto);
            logger.info("Conteúdo: (Omitido no mock, código enviado!)");
            logger.info("=================================================");
            return;
        }

        int maxRetries = 3;
        for (int tentativa = 1; tentativa <= maxRetries; tentativa++) {
            try {
                brevoEmailClient.enviar(para, assunto, htmlConteudo);
                return; // Sucesso, sai do loop
            } catch (BrevoApiException e) {
                if (!e.isRetryable()) {
                    logger.error("Erro permanente/não-retryable ao enviar e-mail para {}: {}", para, e.getMessage());
                    throw new ServicoIndisponivelException("Não foi possível enviar o e-mail de verificação. O serviço de e-mail pode estar indisponível.");
                }

                logger.error("Tentativa {}/{} falhou ao enviar e-mail via Brevo para {}: {}", tentativa, maxRetries, para, e.getMessage());
                if (tentativa == maxRetries) {
                    logger.error("ALERTA: Provedor de e-mail Brevo indisponível após {} tentativas.", maxRetries);
                    throw new ServicoIndisponivelException("Não foi possível enviar o e-mail de verificação. O serviço de e-mail pode estar indisponível.");
                }
                dormir(tentativa);
            } catch (Exception e) {
                logger.error("Tentativa {}/{} falhou com erro inesperado ao enviar e-mail para {}: {}", tentativa, maxRetries, para, e.getMessage());
                if (tentativa == maxRetries) {
                    throw new ServicoIndisponivelException("Não foi possível enviar o e-mail de verificação. O serviço de e-mail pode estar indisponível.");
                }
                dormir(tentativa);
            }
        }
    }

    private void dormir(int tentativa) {
        try {
            Thread.sleep((long) Math.pow(2, tentativa) * 1000); // Backoff exponencial: 2s, 4s, 8s
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ServicoIndisponivelException("Interrompido durante o reenvio de e-mail.");
        }
    }
}