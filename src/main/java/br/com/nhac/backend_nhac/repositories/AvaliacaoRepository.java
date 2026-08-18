package br.com.nhac.backend_nhac.repositories;

import br.com.nhac.backend_nhac.domain.avaliacao.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, String> {

    boolean existsByPedidoId(String pedidoId);

    Page<Avaliacao> findByLojaId(String lojaId, Pageable pageable);
}
