package br.com.nhac.backend_nhac.domain.chat;

import java.time.Instant;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * Um canal contínuo entre uma Loja e UM participante do outro lado — que pode
 * ser um CLIENTE (chat de pedido/dúvida) ou, desde a V039, um ENTREGADOR
 * (combinar retirada, avisar atraso, etc). Os dois tipos nunca coexistem na
 * mesma Conversa: são canais paralelos e independentes, cada um com seu
 * próprio par único (loja, participante, tipo).
 *
 * O nome da coluna/campo "clienteId" foi mantido por compatibilidade — ela
 * guarda o id do usuário do lado participante, seja ele CLIENTE ou
 * ENTREGADOR. Renomear a coluna exigiria migração de dados sem benefício
 * real; participanteTipo já deixa o significado explícito.
 */
@Entity
@Table(name = "tb_conversas", uniqueConstraints = @UniqueConstraint(columnNames = {"loja_id", "cliente_id", "participante_tipo"}))
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

    /** Id do usuário do lado participante (cliente OU entregador — ver participanteTipo). */
    @Column(name = "cliente_id", nullable = false, length = 50)
    private String clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participante_tipo", nullable = false, length = 20)
    private ParticipanteTipo participanteTipo = ParticipanteTipo.CLIENTE;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "ultima_mensagem_em", nullable = false)
    private Instant ultimaMensagemEm;

    @Column(name = "ultima_mensagem_preview")
    private String ultimaMensagemPreview;

    @Column(name = "nao_lidas_loja", nullable = false)
    private int naoLidasLoja = 0;

    /** Não lidas do lado do participante (cliente ou entregador). */
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

    public Conversa(String id, Loja loja, String participanteId, ParticipanteTipo participanteTipo) {
        this.id = id;
        this.loja = loja;
        this.clienteId = participanteId;
        this.participanteTipo = participanteTipo;
        this.criadaEm = Instant.now();
        this.ultimaMensagemEm = Instant.now();
    }

    /**
     * Construtor de conveniência mantido por compatibilidade: todo o código
     * (e os testes) escritos antes da V039 chamavam Conversa(id, loja,
     * clienteId) assumindo implicitamente que era sempre uma conversa com
     * cliente. Sem este overload, ChatConcorrenciaIT, ChatWebSocketIT e
     * outros testes que ainda não foram atualizados param de compilar.
     */
    public Conversa(String id, Loja loja, String participanteId) {
        this(id, loja, participanteId, ParticipanteTipo.CLIENTE);
    }

    public void registrarNovaMensagem(RemetenteTipo remetente, String preview) {
        this.ultimaMensagemEm = Instant.now();
        this.ultimaMensagemPreview = preview;
        if (remetente == RemetenteTipo.LOJA) {
            this.naoLidasCliente++;
        } else {
            // CLIENTE ou ENTREGADOR: qualquer um dos dois é sempre "o
            // participante" desta conversa específica.
            this.naoLidasLoja++;
        }
    }

    public void marcarComoLidaPelaLoja() {
        this.naoLidasLoja = 0;
    }

    public void marcarComoLidaPeloCliente() {
        this.naoLidasCliente = 0;
    }
}
