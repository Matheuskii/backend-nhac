package br.com.nhac.backend_nhac.domain.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, String> {

    Optional<Conversa> findByLojaIdAndClienteId(String lojaId, String clienteId);

    Page<Conversa> findByLojaIdOrderByUltimaMensagemEmDesc(String lojaId, Pageable pageable);

    @Query("SELECT c FROM Conversa c WHERE c.id = :id AND c.loja.id = :lojaId")
    Optional<Conversa> findByIdAndLojaId(@Param("id") String id, @Param("lojaId") String lojaId);
}
