package br.com.nhac.backend_nhac.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnviarCodigoSmsDTO(
    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "O telefone deve estar no formato internacional E.164 (ex: +5511999999999)")
    String telefone
) {}
