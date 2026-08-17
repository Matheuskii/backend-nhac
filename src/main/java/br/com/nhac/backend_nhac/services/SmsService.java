package br.com.nhac.backend_nhac.services;

public interface SmsService {
    void enviarSms(String telefoneDestino, String mensagem);
}
