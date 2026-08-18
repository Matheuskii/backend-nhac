package br.com.nhac.backend_nhac.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para concluir a redefinição de senha informando o código")
public record RedefinirSenhaDTO(
        @Schema(description = "Número de telefone cadastrado", example = "11999998888")
        @NotBlank(message = "O telefone é obrigatório.")
        String telefone,

        @Schema(description = "Código de verificação recebido via SMS", example = "123456")
        @NotBlank(message = "O código é obrigatório.")
        String codigo,

        @Schema(description = "Nova senha do usuário", example = "novaSenhaForte123")
        @NotBlank(message = "A nova senha é obrigatória.")
        String novaSenha
) {
}
