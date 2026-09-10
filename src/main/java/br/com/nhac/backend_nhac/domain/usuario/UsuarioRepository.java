package br.com.nhac.backend_nhac.domain.usuario;

import br.com.nhac.backend_nhac.domain.auth.CodigoVerificacaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByTelefone(String telefone);

    @Query("SELECT c FROM CodigoVerificacaoEmail c WHERE c.email = :email AND c.tipo = :tipo AND c.utilizado = true AND c.criadoEm >= :dataLimite")
    Optional<CodigoVerificacaoEmail> findCodigoVerificacaoPorEmailETipo(
        @Param("email") String email,
        @Param("tipo") CodigoVerificacaoEmail.TipoCodigo tipo,
        @Param("dataLimite") LocalDateTime dataLimite
    );
}