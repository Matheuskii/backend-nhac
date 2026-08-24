package br.com.nhac.backend_nhac.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para solicitação de redefinição de senha via e-mail")
public record EsqueciSenhaEmailDTO(
        @Schema(description = "E-mail cadastrado", example = "usuario@email.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email
) {
}
