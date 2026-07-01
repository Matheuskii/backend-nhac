package br.com.nhac.backend_nhac.repositories;


import br.com.nhac.backend_nhac.domain.usuario.EnderecoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnderecoUsuarioRepository extends JpaRepository<EnderecoUsuario, String> {
    List<EnderecoUsuario> findByUsuarioId(String usuarioId);
}
