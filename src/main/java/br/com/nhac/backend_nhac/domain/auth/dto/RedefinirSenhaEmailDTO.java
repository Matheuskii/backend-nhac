package br.com.nhac.backend_nhac.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para concluir redefinição de senha via e-mail")
public record RedefinirSenhaEmailDTO(
        @Schema(description = "E-mail do usuário", example = "usuario@email.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        @Schema(description = "Código de verificação recebido por e-mail", example = "123456")
        @NotBlank(message = "O código é obrigatório.")
        @Size(min = 6, max = 6, message = "O código deve ter 6 dígitos.")
        String codigo,

        @Schema(description = "Nova senha do usuário", example = "novaSenha123")
        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
        String novaSenha
) {
}
