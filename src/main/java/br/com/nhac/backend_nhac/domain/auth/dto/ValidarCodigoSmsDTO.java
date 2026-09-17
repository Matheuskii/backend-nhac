package br.com.nhac.backend_nhac.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ValidarCodigoSmsDTO(
    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "O telefone deve estar no formato internacional E.164 (ex: +5511999999999)")
    String telefone,

    @NotBlank(message = "O código é obrigatório")
    @Size(min = 6, max = 6, message = "O código deve conter 6 dígitos")
    String codigo,

    String nome
) {}
