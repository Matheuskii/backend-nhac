package br.com.nhac.backend_nhac.repositories;

import br.com.nhac.backend_nhac.domain.produto.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {

    // GET /produtos?lojaId=X
    Page<Produto> findByLojaId(String lojaId, Pageable pageable);

    // GET /produtos?precoMaximo=20.0
    Page<Produto> findByPrecoLessThanEqual(BigDecimal precoMaximo, Pageable pageable);

    // GET /produtos?categoriaMenu=X
    Page<Produto> findByCategoriaMenuIgnoreCase(String categoriaMenu, Pageable pageable);

    // GET /produtos?nome=X
    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}