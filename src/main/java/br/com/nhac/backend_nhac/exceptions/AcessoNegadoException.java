package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;

public class AcessoNegadoException extends NhacException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem, ErrorCode.ACESSO_NEGADO);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}