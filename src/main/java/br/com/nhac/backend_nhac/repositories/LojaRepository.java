package br.com.nhac.backend_nhac.repositories;


import br.com.nhac.backend_nhac.domain.loja.Loja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LojaRepository extends JpaRepository<Loja, String> {

    // findAll com paginação direta na url
    Page<Loja> findByIsAbertoTrue(Pageable pageable);


    Optional<Loja> findByIdAndIsAbertoTrue(String id);
}
