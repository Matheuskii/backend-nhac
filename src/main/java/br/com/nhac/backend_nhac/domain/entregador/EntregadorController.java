package br.com.nhac.backend_nhac.domain.entregador;

import br.com.nhac.backend_nhac.domain.entregador.dto.AtualizarLocalizacaoDTO;
import br.com.nhac.backend_nhac.domain.entregador.dto.AtualizarStatusDTO;
import br.com.nhac.backend_nhac.domain.entregador.dto.CadastroEntregadorDTO;
import br.com.nhac.backend_nhac.domain.entregador.dto.EntregadorResponseDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/entregador")
@Tag(name = "Entregador", description = "Endpoints para gerenciamento do perfil e status do motoboy/entregador")
public class EntregadorController {

    private final EntregadorService entregadorService;

    public EntregadorController(EntregadorService entregadorService) {
        this.entregadorService = entregadorService;
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastra o usuário logado como entregador parceiro")
    public ResponseEntity<EntregadorResponseDTO> cadastrar(
            @RequestBody @Valid CadastroEntregadorDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        EntregadorResponseDTO resposta = entregadorService.cadastrar(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/perfil")
    @Operation(summary = "Retorna os dados cadastrais e status atual do entregador logado")
    public ResponseEntity<EntregadorResponseDTO> obterPerfil(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return ResponseEntity.ok(entregadorService.obterPerfil(usuarioLogado));
    }

    @PatchMapping("/status")
    @Operation(summary = "Altera o status operacional do entregador (ONLINE, OFFLINE)")
    public ResponseEntity<EntregadorResponseDTO> atualizarStatus(
            @RequestBody @Valid AtualizarStatusDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return ResponseEntity.ok(entregadorService.atualizarStatus(dto, usuarioLogado));
    }

    @PatchMapping("/localizacao")
    @Operation(summary = "Atualiza a localização GPS atual do entregador (Heartbeat)")
    public ResponseEntity<EntregadorResponseDTO> atualizarLocalizacao(
            @RequestBody @Valid AtualizarLocalizacaoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return ResponseEntity.ok(entregadorService.atualizarLocalizacao(dto, usuarioLogado));
    }
}
