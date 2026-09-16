package br.com.nhac.backend_nhac.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarEmailDTO(
    @NotBlank(message = "E-mail é obrigatório")
    String email,
    
    @NotBlank(message = "Código é obrigatório")
    String codigo
) {
}
