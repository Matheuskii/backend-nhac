package br.com.nhac.backend_nhac.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para solicitação de redefinição de senha via telefone")
public record EsqueciSenhaDTO(
        @Schema(description = "Número de telefone cadastrado", example = "11999998888")
        @NotBlank(message = "O telefone é obrigatório.")
        String telefone
) {
}
