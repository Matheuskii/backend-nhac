package br.com.nhac.backend_nhac.exceptions;


import java.time.Instant;

public record ErroPadraoDTO( Instant timestamp,
                             Integer status,
                             String erro,
                             String mensagem,
                             String path) {

}
