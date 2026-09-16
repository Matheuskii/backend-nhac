package br.com.nhac.backend_nhac.domain.entrega;

import br.com.nhac.backend_nhac.domain.entregador.Entregador;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tb_ofertas_entrega")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class OfertaEntrega {

    @Id
    @Column(updatable = false, nullable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id", nullable = false)
    private Entregador entregador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusOferta status = StatusOferta.PENDENTE;

    @Column(name = "criado_em", nullable = false)
    @Builder.Default
    private Instant criadoEm = Instant.now();

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    public boolean isExpirada() {
        return Instant.now().isAfter(expiraEm);
    }
}
