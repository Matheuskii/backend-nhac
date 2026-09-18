package br.com.nhac.backend_nhac.domain.chat;

/**
 * Quem enviou a mensagem, do ponto de vista de quem lê. LOJA cobre dono e
 * qualquer funcionário (o app nunca expõe qual funcionário especificamente —
 * ver Mensagem.remetenteUsuarioId para rastreabilidade interna).
 *
 * ENTREGADOR adicionado na V039 para o chat loja↔motoboy; é espelho de
 * CLIENTE (ambos são "o lado participante" da conversa, nunca aparecem juntos
 * numa mesma Conversa — ver Conversa.participanteTipo).
 */
public enum RemetenteTipo {
    CLIENTE,
    LOJA,
    ENTREGADOR
}
