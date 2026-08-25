package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender emailSender;

    @Value("${app.email.from:noreply@nhac.com.br}")
    private String fromEmail;

    @Value("${nhac.email.mock-mode:true}")
    private boolean mockMode;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

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

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(htmlConteudo, true);
            
            emailSender.send(message);
        } catch (Exception e) {
            logger.error("Falha ao enviar e-mail para {}: {}", para, e.getMessage());
            throw new RegraDeNegocioException("Não foi possível enviar o e-mail de verificação. O serviço de e-mail pode estar indisponível.");
        }
    }
}