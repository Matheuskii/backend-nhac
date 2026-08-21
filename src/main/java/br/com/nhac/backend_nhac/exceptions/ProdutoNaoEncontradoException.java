package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class ProdutoNaoEncontradoException extends NhacException {
    
    public ProdutoNaoEncontradoException(String produtoId, String lojaId) {
        super("Produto com ID '" + produtoId + "' não encontrado" + (lojaId != null ? " na loja '" + lojaId + "'." : "."), 
              ErrorCode.PRODUTO_NAO_ENCONTRADO, 
              lojaId != null ? Map.of("produtoId", produtoId, "lojaId", lojaId) : Map.of("produtoId", produtoId));
    }
    
    public ProdutoNaoEncontradoException(String produtoId) {
        this(produtoId, null);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
