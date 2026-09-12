package br.com.nhac.backend_nhac.domain.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemRepository extends JpaRepository<Mensagem, String> {

    Page<Mensagem> findByConversaIdOrderByEnviadaEmDesc(String conversaId, Pageable pageable);

    long countByConversaId(String conversaId);
}
