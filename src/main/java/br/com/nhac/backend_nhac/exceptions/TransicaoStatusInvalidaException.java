package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class TransicaoStatusInvalidaException extends NhacException {

    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem, ErrorCode.TRANSICAO_STATUS_INVALIDA);
    }

    public TransicaoStatusInvalidaException(String mensagem, Map<String, Object> details) {
        super(mensagem, ErrorCode.TRANSICAO_STATUS_INVALIDA, details);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
