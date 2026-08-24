package br.com.nhac.backend_nhac.domain.favorito.dto;

import jakarta.validation.constraints.NotBlank;

public record FavoritoCreateDTO(
        @NotBlank(message = "O ID da loja é obrigatório.")
        String lojaId
) {}
