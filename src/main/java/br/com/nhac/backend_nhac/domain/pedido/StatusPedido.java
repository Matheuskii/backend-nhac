package br.com.nhac.backend_nhac.domain.pedido;

import java.util.Set;

public enum StatusPedido {
    PENDENTE,
    PAGO,
    PREPARANDO,
    SAIU_ENTREGA,
    ENTREGUE,
    CANCELADO;
    
    public boolean podeMudarPara(StatusPedido novoStatus) {
        if (this == novoStatus) {
            throw new br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException("O pedido já está no status " + novoStatus);
        }
        
        boolean isValido = switch (this) {
            case PENDENTE -> Set.of(PAGO, CANCELADO).contains(novoStatus);
            case PAGO -> Set.of(PREPARANDO, CANCELADO).contains(novoStatus);
            case PREPARANDO -> Set.of(SAIU_ENTREGA, CANCELADO).contains(novoStatus);
            case SAIU_ENTREGA -> Set.of(ENTREGUE).contains(novoStatus);
            case ENTREGUE, CANCELADO -> false;
        };
        
        if (!isValido) {
            throw new br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException("Transição de status inválida de " + this + " para " + novoStatus);
        }
        return true;
    }
}
