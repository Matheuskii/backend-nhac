package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;

public class RegraDeNegocioException extends NhacException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem, ErrorCode.REGRA_DE_NEGOCIO);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}