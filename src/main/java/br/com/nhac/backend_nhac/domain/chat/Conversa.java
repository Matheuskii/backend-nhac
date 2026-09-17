package br.com.nhac.backend_nhac.domain.chat;

import java.time.Instant;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    /**
     * Optimistic lock: duas mensagens simultâneas na mesma conversa disparam
     * UPDATE nos contadores nao_lidas_*. Sem @Version, a última transação
     * sobrescreve a anterior e o contador fica errado.
     * Com @Version, a segunda recebe OptimisticLockException e o Spring retenta.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

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