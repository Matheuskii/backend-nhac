package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class LojaFechadaException extends NhacException {
    
    public LojaFechadaException(String mensagem, Map<String, Object> details) {
        super(mensagem, ErrorCode.LOJA_FECHADA, details);
    }
    
    public LojaFechadaException(String lojaId) {
        this("A loja informada está fechada no momento.", Map.of("lojaId", lojaId));
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
