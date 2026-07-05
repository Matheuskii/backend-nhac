package br.com.nhac.backend_nhac.repositories;

import br.com.nhac.backend_nhac.domain.produto.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {

    // GET /produtos/{id}
    Optional<Produto> findByIdAndIsAtivoTrue(String id);

    // GET /produtos?lojaId=X
    Page<Produto> findByLojaIdAndIsAtivoTrue(String lojaId, Pageable pageable);

    // GET /produtos?precoMaximo=20.0
    Page<Produto> findByPrecoLessThanEqualAndIsAtivoTrue(BigDecimal precoMaximo, Pageable pageable);

    // GET /produtos?categoriaMenu=X
    Page<Produto> findByCategoriaMenuIgnoreCaseAndIsAtivoTrue(String categoriaMenu, Pageable pageable);

    // GET /produtos?nome=X
    Page<Produto> findByNomeContainingIgnoreCaseAndIsAtivoTrue(String nome, Pageable pageable);

    // GET /produtos (sem filtros)
    Page<Produto> findByIsAtivoTrue(Pageable pageable);
}