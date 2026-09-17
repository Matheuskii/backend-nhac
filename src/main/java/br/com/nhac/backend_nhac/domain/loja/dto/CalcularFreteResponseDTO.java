package br.com.nhac.backend_nhac.domain.loja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Resposta do cálculo dinâmico de frete")
public record CalcularFreteResponseDTO(
    @Schema(description = "Valor calculado para o frete", example = "5.50")
    BigDecimal valor,

    @Schema(description = "Tempo estimado de entrega em minutos", example = "45")
    Integer tempoEstimadoMinutos
) {
}
