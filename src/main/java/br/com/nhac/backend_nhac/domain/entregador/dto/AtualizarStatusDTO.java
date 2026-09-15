package br.com.nhac.backend_nhac.domain.entregador.dto;

import br.com.nhac.backend_nhac.domain.entregador.StatusOperacional;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusDTO(
        @NotNull(message = "O status operacional é obrigatório.")
        StatusOperacional statusOperacional
) {
}
