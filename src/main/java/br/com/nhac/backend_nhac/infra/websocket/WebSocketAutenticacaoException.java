package br.com.nhac.backend_nhac.infra.websocket;

/**
 * Lançada pelo StompAuthChannelInterceptor quando o frame CONNECT não traz um
 * token válido. Deixar propagar faz o Spring recusar a conexão STOMP (o
 * cliente recebe um frame ERROR e a conexão cai) — não usa NhacException
 * porque essa família é pra respostas HTTP (tem getHttpStatus()), e aqui não
 * existe uma resposta HTTP pra devolver.
 */
public class WebSocketAutenticacaoException extends RuntimeException {
    public WebSocketAutenticacaoException(String mensagem) {
        super(mensagem);
    }
}
