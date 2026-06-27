package br.com.nhac.backend_nhac.repositories;

import br.com.nhac.backend_nhac.domain.produto.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {


    Page<Produto> findByLojaId(String lojaId, Pageable pageable);
}
