package br.com.nhac.backend_nhac.domain.chat;

import br.com.nhac.backend_nhac.domain.chat.dto.ChatDTOs.ConversaResumoDTO;
import br.com.nhac.backend_nhac.domain.chat.dto.ChatDTOs.MensagemDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Histórico via REST + marcação de lido. O ENVIO de mensagem em tempo real é
 * via WebSocket (ver ChatWebSocketController /app/conversas/{id}/enviar) —
 * isso aqui é só pra abrir uma conversa já existente e ver o que já rolou,
 * ou pra quando o cliente WS cair e precisar recarregar o histórico.
 */
@RestController
@RequestMapping("/api/v1/lojista/conversas")
@Tag(name = "Painel do Lojista - Chat", description = "Histórico de conversas com clientes. Envio de mensagem em tempo real é via WebSocket em /ws (STOMP), não por aqui.")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "Listar conversas", description = "Lista as conversas da loja do usuário autenticado (dono ou funcionário), mais recentes primeiro.")
    @GetMapping
    public ResponseEntity<Page<ConversaResumoDTO>> listar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(chatService.listarConversasDaLoja(usuarioLogado, pageable));
    }

    @Operation(summary = "Histórico de mensagens", description = "Mensagens de uma conversa da loja do usuário autenticado, mais recentes primeiro (paginado).")
    @GetMapping("/{id}/mensagens")
    public ResponseEntity<Page<MensagemDTO>> listarMensagens(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PathVariable String id,
            @PageableDefault(size = 30, sort = "enviadaEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(chatService.listarMensagens(id, usuarioLogado, pageable));
    }

    @Operation(summary = "Marcar conversa como lida", description = "Zera o contador de não lidas do lado da loja.")
    @PatchMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PathVariable String id) {
        chatService.marcarComoLidaPelaLoja(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
