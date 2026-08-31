package br.com.nhac.backend_nhac.domain.loja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para cálculo dinâmico de frete")
public record CalcularFreteRequestDTO(
    @Schema(description = "Latitude do cliente", example = "-23.550520")
    @NotNull(message = "A latitude é obrigatória")
    Double lat,

    @Schema(description = "Longitude do cliente", example = "-46.633308")
    @NotNull(message = "A longitude é obrigatória")
    Double lng
) {
}
