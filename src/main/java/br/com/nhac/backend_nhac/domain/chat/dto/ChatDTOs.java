package br.com.nhac.backend_nhac.domain.chat.dto;

import br.com.nhac.backend_nhac.domain.chat.Conversa;
import br.com.nhac.backend_nhac.domain.chat.Mensagem;
import br.com.nhac.backend_nhac.domain.chat.RemetenteTipo;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class ChatDTOs {

    /** Resumo de conversa para a listagem no painel do lojista. */
    public record ConversaResumoDTO(
            String id,
            String clienteId,
            String clienteNome,
            String ultimaMensagemPreview,
            Instant ultimaMensagemEm,
            int naoLidas
    ) {
        public ConversaResumoDTO(Conversa conversa, String clienteNome) {
            this(
                    conversa.getId(),
                    conversa.getClienteId(),
                    clienteNome,
                    conversa.getUltimaMensagemPreview(),
                    conversa.getUltimaMensagemEm(),
                    conversa.getNaoLidasLoja()
            );
        }
    }

    /** Uma mensagem, tanto no histórico REST quanto transmitida via WebSocket. */
    public record MensagemDTO(
            String id,
            String conversaId,
            RemetenteTipo remetenteTipo,
            String remetenteUsuarioId,
            String conteudo,
            Instant enviadaEm
    ) {
        public MensagemDTO(Mensagem mensagem) {
            this(
                    mensagem.getId(),
                    mensagem.getConversa().getId(),
                    mensagem.getRemetenteTipo(),
                    mensagem.getRemetenteUsuarioId(),
                    mensagem.getConteudo(),
                    mensagem.getEnviadaEm()
            );
        }
    }

    /** Payload enviado pelo cliente WebSocket (STOMP) para /app/conversas/{id}/enviar. */
    public record EnviarMensagemDTO(@NotBlank String conteudo) {}
}
