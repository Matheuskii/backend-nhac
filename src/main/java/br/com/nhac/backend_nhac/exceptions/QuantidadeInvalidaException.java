package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class QuantidadeInvalidaException extends NhacException {
    
    public QuantidadeInvalidaException(String mensagem, Map<String, Object> details) {
        super(mensagem, ErrorCode.QUANTIDADE_INVALIDA, details);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
