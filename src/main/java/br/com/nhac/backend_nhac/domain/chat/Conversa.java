package br.com.nhac.backend_nhac.domain.chat;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tb_conversas", uniqueConstraints = @UniqueConstraint(columnNames = {"loja_id", "cliente_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Conversa {

    @Id
    @Column(updatable = false, nullable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Column(name = "cliente_id", nullable = false, length = 50)
    private String clienteId;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "ultima_mensagem_em", nullable = false)
    private Instant ultimaMensagemEm;

    @Column(name = "ultima_mensagem_preview")
    private String ultimaMensagemPreview;

    @Column(name = "nao_lidas_loja", nullable = false)
    private int naoLidasLoja = 0;

    @Column(name = "nao_lidas_cliente", nullable = false)
    private int naoLidasCliente = 0;

    public Conversa(String id, Loja loja, String clienteId) {
        this.id = id;
        this.loja = loja;
        this.clienteId = clienteId;
        this.criadaEm = Instant.now();
        this.ultimaMensagemEm = Instant.now();
    }

    public void registrarNovaMensagem(RemetenteTipo remetente, String preview) {
        this.ultimaMensagemEm = Instant.now();
        this.ultimaMensagemPreview = preview;
        if (remetente == RemetenteTipo.CLIENTE) {
            this.naoLidasLoja++;
        } else {
            this.naoLidasCliente++;
        }
    }

    public void marcarComoLidaPelaLoja() {
        this.naoLidasLoja = 0;
    }

    public void marcarComoLidaPeloCliente() {
        this.naoLidasCliente = 0;
    }
}
