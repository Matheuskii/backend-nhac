package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class ProdutoInativoException extends NhacException {
    
    public ProdutoInativoException(String mensagem, Map<String, Object> details) {
        super(mensagem, ErrorCode.PRODUTO_INATIVO, details);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
