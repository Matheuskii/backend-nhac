package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class EstoqueInsuficienteException extends NhacException {
    
    public EstoqueInsuficienteException(String produtoId, int quantidadeDesejada, int estoqueAtual) {
        super("Estoque insuficiente para o produto.", 
              ErrorCode.ESTOQUE_INSUFICIENTE, 
              Map.of("produtoId", produtoId, "quantidadeDesejada", quantidadeDesejada, "estoqueAtual", estoqueAtual));
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
