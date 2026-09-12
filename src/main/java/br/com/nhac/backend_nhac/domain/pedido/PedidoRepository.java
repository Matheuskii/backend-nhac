package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
        WHERE p.loja.id = :lojaId
        AND (:status IS NULL OR p.status = :status)
        ORDER BY p.criadoEm DESC
        """,
            countQuery = """
        SELECT COUNT(p) FROM Pedido p
        WHERE p.loja.id = :lojaId
        AND (:status IS NULL OR p.status = :status)
        """)
    Page<Pedido> findByLoja(
            @Param("lojaId") String lojaId,
            @Param("status") StatusPedido status,
            Pageable pageable
    );

    long countByUsuarioId(String usuarioId);
    long countByUsuarioIdAndCupomIdIsNotNull(String usuarioId);

    long countByLojaIdAndStatusIn(String lojaId, java.util.List<StatusPedido> status);

    java.util.List<Pedido> findTop5ByLojaIdOrderByCriadoEmDesc(String lojaId);

    @Query("SELECT p FROM Pedido p WHERE p.loja.id = :lojaId AND p.criadoEm >= :inicio AND p.criadoEm <= :fim")
    java.util.List<Pedido> findByLojaIdAndPeriodo(
            @Param("lojaId") String lojaId,
            @Param("inicio") java.time.Instant inicio,
            @Param("fim") java.time.Instant fim
    );
}
