package br.com.nhac.backend_nhac.domain.auth.dto;

public record LoginResponseDTO(
        String token,
        String usuarioId,
        String nome
) {}