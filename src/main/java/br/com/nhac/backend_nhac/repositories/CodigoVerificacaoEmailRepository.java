package br.com.nhac.backend_nhac.repositories;

import br.com.nhac.backend_nhac.domain.auth.CodigoVerificacaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CodigoVerificacaoEmailRepository extends JpaRepository<CodigoVerificacaoEmail, Long> {

    @Modifying
    @Query("UPDATE CodigoVerificacaoEmail c SET c.utilizado = true WHERE c.email = :email AND c.utilizado = false")
    void inativarCodigosAtivosPorEmail(@Param("email") String email);

    Optional<CodigoVerificacaoEmail> findTopByEmailAndUtilizadoFalseAndDataExpiracaoAfterOrderByCriadoEmDesc(String email, LocalDateTime data);
}
