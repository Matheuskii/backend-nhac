package br.com.nhac.backend_nhac.infra.websocket;

import java.security.Principal;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import br.com.nhac.backend_nhac.domain.chat.ChatService;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.infra.security.TokenService;

/**
 * Autentica a conexão WebSocket dentro do frame STOMP CONNECT, não no
 * handshake HTTP. Motivo: o WebSocket nativo do browser não deixa mandar
 * headers customizados no handshake, mas o protocolo STOMP permite headers
 * arbitrários no CONNECT — por isso o handshake em si (/ws/**) é permitAll no
 * SecurityConfig, e a autenticação de verdade acontece aqui, reaproveitando o
 * mesmo TokenService (JWT) usado pelo SecurityFilter do REST.
 *
 * Cliente deve mandar, no frame CONNECT: header "Authorization: Bearer <token>"
 * (bibliotecas STOMP como @stomp/stompjs suportam isso via `connectHeaders`).
 *
 * Também valida SUBSCRIBE para /topic/conversas/{id}: sem isso, qualquer
 * usuário autenticado poderia assinar o tópico de qualquer conversa e receber
 * mensagens alheias em tempo real (o broker simples do Spring não filtra por
 * padrão).
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String PREFIXO_TOPICO_CONVERSA = "/topic/conversas/";

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final ChatService chatService;

    public StompAuthChannelInterceptor(TokenService tokenService,
                                        UsuarioRepository usuarioRepository,
                                        ChatService chatService) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            autenticarConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            autorizarSubscribe(accessor);
        }

        return message;
    }

    private void autenticarConnect(StompHeaderAccessor accessor) {
        String token = extrairToken(accessor.getFirstNativeHeader("Authorization"));
        Usuario usuario = autenticar(token);

        if (usuario == null) {
            throw new WebSocketAutenticacaoException("Token inválido ou ausente no CONNECT.");
        }

        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        accessor.setUser(authentication);
    }

    /**
     * Bloqueia SUBSCRIBE em /topic/conversas/{id} quando o usuário autenticado
     * não faz parte da conversa. Outros tópicos passam sem checagem adicional.
     */
    private void autorizarSubscribe(StompHeaderAccessor accessor) {
        String destino = accessor.getDestination();
        if (destino == null || !destino.startsWith(PREFIXO_TOPICO_CONVERSA)) {
            return;
        }

        Principal principal = accessor.getUser();
        if (!(principal instanceof UsernamePasswordAuthenticationToken auth)
                || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new WebSocketAutenticacaoException("SUBSCRIBE sem usuário autenticado.");
        }

        String conversaId = destino.substring(PREFIXO_TOPICO_CONVERSA.length());
        if (!chatService.podeAcessarConversa(conversaId, usuario)) {
            throw new WebSocketAutenticacaoException("Sem permissão para esta conversa.");
        }
    }

    private Usuario autenticar(String token) {
        if (token == null) {
            return null;
        }
        String usuarioId = tokenService.validarToken(token);
        if (usuarioId == null) {
            return null;
        }
        // Checa isEnabled(): sem isso, um funcionário desativado continuaria
        // usando o WS até o token expirar — mesmo com o SecurityFilter HTTP
        // já bloqueando requisições REST pra ele.
        return usuarioRepository.findById(usuarioId)
                .filter(Usuario::isEnabled)
                .orElse(null);
    }

    private String extrairToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}