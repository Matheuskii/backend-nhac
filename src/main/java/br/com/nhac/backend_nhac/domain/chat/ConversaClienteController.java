package br.com.nhac.backend_nhac.domain.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

/**
 * Endpoints do app do cliente para o chat:
 *  - POST /conversas/lojas/{lojaId}          → abre/obtém a conversa com a loja (idempotente)
 *  - PATCH /conversas/{conversaId}/lida      → marca a conversa como lida pelo cliente
 *
 * O envio de mensagem é via WebSocket (ChatWebSocketController).
 *
 * O endpoint de abertura só permite papel CLIENTE (validado no service) — um
 * LOJISTA usando essa rota criaria conversas "cliente=lojista" que poluem a
 * lista e permitem spam de conversas para lojas alheias.
 */
@RestController
@RequestMapping("/api/v1/conversas")
@Tag(name = "Chat (cliente)", description = "Endpoints do app do cliente: abrir conversa com uma loja e marcar como lida")
public class ConversaClienteController {

    private final ChatService chatService;

    public ConversaClienteController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(
            summary = "Abrir ou obter conversa com uma loja",
            description = "Idempotente: se já existir uma conversa entre o cliente autenticado e a loja, retorna o id dela. Apenas CLIENTE pode chamar."
    )
    @PostMapping("/lojas/{lojaId}")
    public ResponseEntity<String> obterOuCriar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PathVariable @NotBlank String lojaId) {
        Conversa conversa = chatService.obterOuCriarConversa(lojaId, usuarioLogado);
        return ResponseEntity.ok(conversa.getId());
    }

    @Operation(
            summary = "Marcar conversa como lida",
            description = "Zera o contador de não lidas do lado do cliente. Só o cliente dono da conversa pode chamar."
    )
    @PatchMapping("/{conversaId}/lida")
    public ResponseEntity<Void> marcarComoLida(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PathVariable String conversaId) {
        chatService.marcarComoLidaPeloCliente(conversaId, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}