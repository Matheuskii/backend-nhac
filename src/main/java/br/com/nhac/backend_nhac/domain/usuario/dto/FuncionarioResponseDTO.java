package br.com.nhac.backend_nhac.domain.usuario.dto;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;

import java.time.Instant;

public record FuncionarioResponseDTO(
        String id,
        String nomeCompleto,
        String email,
        String telefone,
        String cargo,
        String fotoUrl,
        boolean ativo,
        Instant dataCadastro
) {
    public FuncionarioResponseDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getCargo(),
                usuario.getImagemUrl(),
                usuario.isAtivo(),
                usuario.getCriadoEm()
        );
    }
}
