package br.com.nhac.backend_nhac.repositories;

import br.com.nhac.backend_nhac.domain.produto.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {

    @Query("SELECT p FROM Produto p JOIN FETCH p.loja WHERE p.id = :id AND p.isAtivo = true")
    Optional<Produto> findByIdAndIsAtivoTrue(@Param("id") String id);

    long countByLojaId(String lojaId);

    @Query("""
        SELECT p FROM Produto p 
        JOIN FETCH p.loja 
        WHERE p.isAtivo = true
        AND (:lojaId IS NULL OR p.loja.id = :lojaId)
        AND (:categoriaMenu IS NULL OR LOWER(p.categoriaMenu) = LOWER(:categoriaMenu))
        AND (:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
        AND (:precoMaximo IS NULL OR p.preco <= :precoMaximo)
    """)
    Page<Produto> findAllWithFilters(
        @Param("lojaId") String lojaId,
        @Param("categoriaMenu") String categoriaMenu,
        @Param("nome") String nome,
        @Param("precoMaximo") BigDecimal precoMaximo,
        Pageable pageable
    );

    Page<Produto> findByLojaIdAndIsAtivoTrue(String lojaId, Pageable pageable);

    Page<Produto> findByPrecoLessThanEqualAndIsAtivoTrue(BigDecimal precoMaximo, Pageable pageable);

    Page<Produto> findByCategoriaMenuIgnoreCaseAndIsAtivoTrue(String categoriaMenu, Pageable pageable);

    Page<Produto> findByNomeContainingIgnoreCaseAndIsAtivoTrue(String nome, Pageable pageable);

    Page<Produto> findByIsAtivoTrue(Pageable pageable);
}