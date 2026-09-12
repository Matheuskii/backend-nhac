package br.com.nhac.backend_nhac.infra.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Chat via WebSocket (STOMP sobre SockJS), substituindo o polling que estava
 * cogitado antes da decisão de arquitetura (item 7 da spec).
 *
 * Endpoint de handshake: /ws (com fallback SockJS pra ambientes que bloqueiam
 * WebSocket puro). Prefixo de destino de aplicação: /app (mensagens que o
 * cliente ENVIA, roteadas pros @MessageMapping do ChatWebSocketController).
 * Broker simples em memória em /topic (broadcast pra todos inscritos numa
 * conversa) e /queue (mensagens ponto-a-ponto, ex.: erros pro usuário
 * específico via /user/queue/...).
 *
 * Broker "simples" (em memória, não RabbitMQ/ActiveMQ) é suficiente pro
 * volume esperado agora; se precisar escalar pra múltiplas instâncias do
 * backend depois, trocar por um STOMP broker externo aqui é a única mudança
 * necessária nesta classe.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    public WebSocketConfig(StompAuthChannelInterceptor stompAuthChannelInterceptor) {
        this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Endpoint sem SockJS, para clientes (apps mobile/desktop) que falam
        // WebSocket nativo diretamente e não precisam do fallback HTTP.
        registry.addEndpoint("/ws-native").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
