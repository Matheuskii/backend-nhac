package br.com.nhac.backend_nhac.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    @Value("${nhac.sms.mock-mode:true}")
    private boolean mockMode;

    @PostConstruct
    public void init() {
        if (!mockMode && accountSid != null && !accountSid.isBlank() && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio SMS Service inicializado com sucesso.");
        } else {
            logger.info("Twilio SMS operando em modo MOCK / Log no terminal.");
        }
    }

    @Override
    public void enviarSms(String telefoneDestino, String mensagem) {
        if (mockMode || accountSid == null || accountSid.isBlank()) {
            logger.info("=================================================");
            logger.info("[SMS MOCK - NHAC DELIVERY]");
            logger.info("Destinatário: {}", telefoneDestino);
            logger.info("Mensagem: {}", mensagem);
            logger.info("=================================================");
            return;
        }

        try {
            Message.creator(
                new PhoneNumber(telefoneDestino),
                new PhoneNumber(fromNumber),
                mensagem
            ).create();
            logger.info("SMS enviado com sucesso via Twilio para {}", telefoneDestino);
        } catch (Exception e) {
            logger.error("Falha ao enviar SMS para {}: {}", telefoneDestino, e.getMessage(), e);
            throw new RuntimeException("Não foi possível enviar o SMS de verificação.", e);
        }
    }
}
