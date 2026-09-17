package br.com.nhac.backend_nhac.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para alteração de senha de um usuário autenticado")
public record AlterarSenhaDTO(
        @Schema(description = "Senha atual do usuário", example = "senhaAntiga123")
        @NotBlank(message = "A senha atual é obrigatória.")
        String senhaAtual,

        @Schema(description = "Nova senha do usuário", example = "novaSenhaForte456")
        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres.")
        String novaSenha
) {}
