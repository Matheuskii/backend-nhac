package br.com.nhac.backend_nhac.repositories;


import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, String> {
}
