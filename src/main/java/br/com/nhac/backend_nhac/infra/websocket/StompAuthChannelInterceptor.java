package br.com.nhac.backend_nhac.infra.websocket;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

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
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public StompAuthChannelInterceptor(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extrairToken(accessor.getFirstNativeHeader("Authorization"));
            Usuario usuario = autenticar(token);

            if (usuario == null) {
                throw new WebSocketAutenticacaoException("Token inválido ou ausente no CONNECT.");
            }

            var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            accessor.setUser(authentication);
        }

        return message;
    }

    private Usuario autenticar(String token) {
        if (token == null) {
            return null;
        }
        String usuarioId = tokenService.validarToken(token);
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findById(usuarioId).orElse(null);
    }

    private String extrairToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}
