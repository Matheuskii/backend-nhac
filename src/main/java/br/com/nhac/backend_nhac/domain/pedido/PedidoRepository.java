package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, String> {
    Optional<Pedido> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<Pedido> findByAsaasPaymentId(String asaasPaymentId);
    Optional<Pedido> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
    Page<Pedido> findByUsuarioId(String usuarioId, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT p FROM Pedido p
        JOIN FETCH p.loja
        WHERE p.loja.usuarioId = :usuarioId
        AND (:status IS NULL OR p.status = :status)
        """,
            countQuery = """
        SELECT COUNT(p) FROM Pedido p
        WHERE p.loja.usuarioId = :usuarioId
        AND (:status IS NULL OR p.status = :status)
        """)
    Page<Pedido> findByLojista(
            @Param("usuarioId") String usuarioId,
            @Param("status") StatusPedido status,
            Pageable pageable
    );

    long countByUsuarioId(String usuarioId);
    long countByUsuarioIdAndCupomIdIsNotNull(String usuarioId);

    // Queries para Painel e Financeiro
    
    @Query("""
        SELECT SUM(p.valorTotal) 
        FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        AND p.status = 'ENTREGUE'
        AND FUNCTION('DATE', p.criadoEm) = FUNCTION('DATE', CURRENT_TIMESTAMP)
    """)
    BigDecimal calcularFaturamentoHoje(@Param("lojaId") String lojaId);

    @Query("""
        SELECT COUNT(p) 
        FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        AND p.status = :status
    """)
    long contarPedidosPorStatus(@Param("lojaId") String lojaId, @Param("status") StatusPedido status);

    @Query("""
        SELECT SUM(p.valorTotal) 
        FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        AND p.status = 'ENTREGUE'
        AND p.criadoEm >= :dataInicio
    """)
    BigDecimal calcularFaturamentoUltimos7Dias(@Param("lojaId") String lojaId, @Param("dataInicio") Instant dataInicio);

    @Query("""
        SELECT p FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        ORDER BY p.criadoEm DESC
    """)
    Page<Pedido> encontrarPedidosRecentes(@Param("lojaId") String lojaId, Pageable pageable);

    @Query("""
        SELECT SUM(p.valorTotal) 
        FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        AND p.status = 'ENTREGUE'
    """)
    BigDecimal calcularFaturamentoTotal(@Param("lojaId") String lojaId);

    @Query("""
        SELECT COUNT(p) 
        FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        AND p.status = 'ENTREGUE'
    """)
    long contarPedidosEntregues(@Param("lojaId") String lojaId);

    @Query("""
        SELECT DATE(p.criadoEm) as data, SUM(p.valorTotal) as valor
        FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        AND p.status = 'ENTREGUE'
        AND p.criadoEm >= :dataInicio
        GROUP BY DATE(p.criadoEm)
        ORDER BY data
    """)
    List<Object[]> calcularFaturamentoPorDia(@Param("lojaId") String lojaId, @Param("dataInicio") Instant dataInicio);

    @Query("""
        SELECT prod.categoriaMenu, SUM(ip.precoHistorico * ip.quantidade) as valor
        FROM ItemPedido ip
        JOIN ip.produto prod
        WHERE ip.pedido.loja.id = :lojaId
        AND ip.pedido.status = 'ENTREGUE'
        GROUP BY prod.categoriaMenu
    """)
    List<Object[]> calcularVendasPorCategoria(@Param("lojaId") String lojaId);

    @Query("""
        SELECT p.formaPagamento, SUM(p.valorTotal) as valor
        FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        AND p.status = 'ENTREGUE'
        GROUP BY p.formaPagamento
    """)
    List<Object[]> calcularVendasPorFormaPagamento(@Param("lojaId") String lojaId);

    @Query("""
        SELECT ip.produto.id, ip.nome, SUM(ip.quantidade) as qtd, SUM(ip.precoHistorico * ip.quantidade) as valor
        FROM ItemPedido ip
        WHERE ip.pedido.loja.id = :lojaId
        AND ip.pedido.status = 'ENTREGUE'
        GROUP BY ip.produto.id, ip.nome
        ORDER BY qtd DESC
    """)
    List<Object[]> encontrarProdutoMaisVendido(@Param("lojaId") String lojaId, Pageable pageable);

    @Query("""
        SELECT HOUR(p.criadoEm) as hora, SUM(p.valorTotal) as valor
        FROM Pedido p 
        WHERE p.loja.id = :lojaId 
        AND p.status = 'ENTREGUE'
        GROUP BY HOUR(p.criadoEm)
        ORDER BY hora
    """)
    List<Object[]> calcularFaturamentoPorHora(@Param("lojaId") String lojaId);
}
