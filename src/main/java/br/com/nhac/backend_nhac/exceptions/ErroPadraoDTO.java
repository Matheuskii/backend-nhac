package br.com.nhac.backend_nhac.exceptions;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ErroPadraoDTO(
        @Schema(example = "horario")
        Instant timestamp,
                             @Schema(example = "400")
                             Integer status,
                             @Schema(example = "problema não esta em vc <3")
                             String erro,
                             @Schema(example = "mensagem especifica do erro")
                             String mensagem,
                             @Schema(example = "api/v1/rota")
                             String path) {

}
