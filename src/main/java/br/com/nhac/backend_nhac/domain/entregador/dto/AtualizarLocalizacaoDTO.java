package br.com.nhac.backend_nhac.domain.entregador.dto;

import jakarta.validation.constraints.NotNull;

public record AtualizarLocalizacaoDTO(
        @NotNull(message = "A latitude é obrigatória.")
        Double latitude,

        @NotNull(message = "A longitude é obrigatória.")
        Double longitude
) {
}
