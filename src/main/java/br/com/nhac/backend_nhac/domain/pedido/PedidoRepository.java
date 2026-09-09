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
}
