package br.com.nhac.backend_nhac.domain.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tb_mensagens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Mensagem {

    @Id
    @Column(updatable = false, nullable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    @Enumerated(EnumType.STRING)
    @Column(name = "remetente_tipo", nullable = false, length = 10)
    private RemetenteTipo remetenteTipo;

    // Quem realmente digitou: o cliente, o dono, ou um funcionário (cargo é só
    // rótulo — pro cliente, toda mensagem do lado LOJA aparece só como "a loja").
    @Column(name = "remetente_usuario_id", nullable = false, length = 50)
    private String remetenteUsuarioId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "enviada_em", nullable = false)
    private Instant enviadaEm;

    public Mensagem(String id, Conversa conversa, RemetenteTipo remetenteTipo, String remetenteUsuarioId, String conteudo) {
        this.id = id;
        this.conversa = conversa;
        this.remetenteTipo = remetenteTipo;
        this.remetenteUsuarioId = remetenteUsuarioId;
        this.conteudo = conteudo;
        this.enviadaEm = Instant.now();
    }
}
