package br.com.nhac.backend_nhac.domain.favorito.dto;

import java.time.Instant;

public record FavoritoResponseDTO(
        String id,
        String lojaId,
        String lojaNome,
        String lojaImagemUrl,
        Instant criadoEm
) {}
