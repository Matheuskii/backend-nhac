package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class LojaNaoEncontradaException extends NhacException {
    
    public LojaNaoEncontradaException(String lojaId) {
        super("Loja com ID '" + lojaId + "' não encontrada.", 
              ErrorCode.LOJA_NAO_ENCONTRADA, 
              Map.of("lojaId", lojaId));
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
