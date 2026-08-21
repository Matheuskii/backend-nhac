package br.com.nhac.backend_nhac.repositories;

import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, String> {
    Optional<Pedido> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<Pedido> findByAsaasPaymentId(String asaasPaymentId);
    boolean existsByIdempotencyKey(String idempotencyKey);
    Page<Pedido> findByUsuarioId(String usuarioId, Pageable pageable);
}
