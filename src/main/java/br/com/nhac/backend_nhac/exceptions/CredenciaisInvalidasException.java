package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;

public class CredenciaisInvalidasException extends NhacException {
    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem, ErrorCode.CREDENCIAIS_INVALIDAS);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}