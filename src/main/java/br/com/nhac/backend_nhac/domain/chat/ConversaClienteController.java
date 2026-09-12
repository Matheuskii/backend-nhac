package br.com.nhac.backend_nhac.domain.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;

/**
 * Fora do escopo principal desta entrega (que é o painel do lojista), mas sem
 * isso a lista de conversas do lojista nunca teria nenhum item: é o cliente
 * quem inicia uma conversa com a loja. Mantido mínimo de propósito — o app do
 * cliente pode ter seus próprios endpoints de chat mais ricos no futuro
 * (listar as próprias conversas, etc.), isso aqui só cobre "abrir/obter o
 * canal com uma loja" o suficiente pra poder conectar no WebSocket depois.
 */
@RestController
@RequestMapping("/api/v1/conversas")
@Tag(name = "Chat (cliente)", description = "Endpoint mínimo para o app do cliente abrir uma conversa com uma loja")
public class ConversaClienteController {

    private final ChatService chatService;

    public ConversaClienteController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "Abrir ou obter conversa com uma loja", description = "Idempotente: se já existir uma conversa entre o cliente autenticado e a loja, retorna o id dela.")
    @PostMapping("/lojas/{lojaId}")
    public ResponseEntity<String> obterOuCriar(@AuthenticationPrincipal Usuario usuarioLogado, @PathVariable @NotBlank String lojaId) {
        Conversa conversa = chatService.obterOuCriarConversa(lojaId, usuarioLogado.getId());
        return ResponseEntity.ok(conversa.getId());
    }
}
