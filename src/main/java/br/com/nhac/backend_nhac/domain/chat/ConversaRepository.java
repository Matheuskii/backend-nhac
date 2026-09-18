package br.com.nhac.backend_nhac.domain.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, String> {

    Optional<Conversa> findByLojaIdAndClienteIdAndParticipanteTipo(
            String lojaId, String clienteId, ParticipanteTipo participanteTipo);

    Page<Conversa> findByLojaIdOrderByUltimaMensagemEmDesc(String lojaId, Pageable pageable);

    Page<Conversa> findByLojaIdAndParticipanteTipoOrderByUltimaMensagemEmDesc(
            String lojaId, ParticipanteTipo participanteTipo, Pageable pageable);

    @Query("SELECT c FROM Conversa c WHERE c.id = :id AND c.loja.id = :lojaId")
    Optional<Conversa> findByIdAndLojaId(@Param("id") String id, @Param("lojaId") String lojaId);

    @Query("SELECT c FROM Conversa c JOIN FETCH c.loja WHERE c.id = :id")
    Optional<Conversa> findByIdComLoja(@Param("id") String id);
}
