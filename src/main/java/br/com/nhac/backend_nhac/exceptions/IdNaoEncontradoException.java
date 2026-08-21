package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;

public class IdNaoEncontradoException extends NhacException {
    public IdNaoEncontradoException(String mensagem) {
        super(mensagem, ErrorCode.RECURSO_NAO_ENCONTRADO);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
