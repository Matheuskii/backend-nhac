package br.com.nhac.backend_nhac.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequestDTO(
        @Schema(description = "token do provedor")
        @NotBlank(message = "O token do provedor é obrigatório")
        String idToken
) {
}
