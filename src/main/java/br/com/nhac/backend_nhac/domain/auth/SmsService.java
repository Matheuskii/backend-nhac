package br.com.nhac.backend_nhac.domain.auth;

public interface SmsService {
    void enviarSms(String telefoneDestino, String mensagem);
}
