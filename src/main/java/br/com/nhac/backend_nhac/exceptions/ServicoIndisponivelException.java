package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;

public class ServicoIndisponivelException extends NhacException {
    public ServicoIndisponivelException(String mensagem) {
        super(mensagem, ErrorCode.SERVICO_INDISPONIVEL);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}
