package br.com.nhac.backend_nhac.domain.loja.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload para abrir ou fechar a loja rapidamente, sem precisar reenviar o cadastro completo")
public record AtualizarAberturaDTO(
        @Schema(description = "true para abrir a loja, false para fechar", example = "true")
        @JsonProperty("isAberto")
        @NotNull(message = "O campo isAberto é obrigatório.")
        Boolean isAberto
) {
}