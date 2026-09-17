package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class PagamentoRecusadoException extends NhacException {
    
    public PagamentoRecusadoException(String mensagem, Throwable cause) {
        super(mensagem, cause, ErrorCode.PAGAMENTO_RECUSADO, null);
    }
    
    public PagamentoRecusadoException(String mensagem, Map<String, Object> details) {
        super(mensagem, ErrorCode.PAGAMENTO_RECUSADO, details);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.PAYMENT_REQUIRED; // ou BAD_REQUEST, dependendo da semântica, deixaremos 402 PAYMENT_REQUIRED ou 400
    }
}
