package br.com.nhac.backend_nhac.repositories;

import br.com.nhac.backend_nhac.domain.auth.CodigoVerificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CodigoVerificacaoRepository extends JpaRepository<CodigoVerificacao, Long> {
    Optional<CodigoVerificacao> findTopByTelefoneAndUtilizadoFalseAndDataExpiracaoAfterOrderByCriadoEmDesc(
        String telefone,
        LocalDateTime agora
    );

    @Modifying
    @Query("UPDATE CodigoVerificacao c SET c.utilizado = true WHERE c.telefone = :telefone AND c.utilizado = false")
    void inativarCodigosAtivosPorTelefone(@Param("telefone") String telefone);
}
