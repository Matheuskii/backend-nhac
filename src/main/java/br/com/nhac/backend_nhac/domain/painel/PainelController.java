package br.com.nhac.backend_nhac.domain.painel;

import br.com.nhac.backend_nhac.domain.painel.dto.PainelResumoDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lojista/painel")
@Tag(name = "Painel do Lojista - Dashboard", description = "Resumo agregado para a tela inicial do painel (item 3.1 da spec)")
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @Operation(summary = "Resumo do painel", description = "Faturamento de hoje, contagem de pedidos por grupo de status, faturamento dos últimos 7 dias e os 5 pedidos mais recentes da loja do usuário autenticado (dono ou funcionário).")
    @GetMapping
    public ResponseEntity<PainelResumoDTO> obterResumo(@AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(painelService.obterResumo(usuarioLogado));
    }
}
