package br.com.nhac.backend_nhac.domain.entregador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregadorRepository extends JpaRepository<Entregador, String> {

    Optional<Entregador> findByUsuarioId(String usuarioId);

    List<Entregador> findByStatusOperacionalAndAtivoTrue(StatusOperacional statusOperacional);

    boolean existsByUsuarioId(String usuarioId);
}
