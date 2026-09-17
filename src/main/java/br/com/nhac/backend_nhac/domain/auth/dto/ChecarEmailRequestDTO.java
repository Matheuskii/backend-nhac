package br.com.nhac.backend_nhac.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChecarEmailRequestDTO(
        @NotBlank(message = "O e-mail não pode estar vazio")
        @Email(message = "Formato de e-mail inválido")
        String email
) {
}
