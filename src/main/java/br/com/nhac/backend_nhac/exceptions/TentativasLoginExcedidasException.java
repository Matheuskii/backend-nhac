package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;

public class TentativasLoginExcedidasException extends NhacException {
    
    public TentativasLoginExcedidasException(String email) {
        super("Número máximo de tentativas de login excedido para o usuário: " + email, ErrorCode.TENTATIVAS_LOGIN_EXCEDIDAS);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}
