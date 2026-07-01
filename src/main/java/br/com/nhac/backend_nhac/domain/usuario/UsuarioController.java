package br.com.nhac.backend_nhac.domain.usuario;

import br.com.nhac.backend_nhac.domain.usuario.dto.EnderecoUsuarioDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioResponseDTO;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException; // 🔴 NOVO IMPORT
import br.com.nhac.backend_nhac.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 🔴 NOVO IMPORT
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private void validarPropriedade(String idNaUrl, Usuario usuarioLogado) {
        if (!idNaUrl.equals(usuarioLogado.getId())) {
            throw new AcessoNegadoException("Acesso negado: não tem permissão para aceder ou modificar os dados de outro utilizador.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuario(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(id, usuarioLogado);
        return ResponseEntity.ok(usuarioService.buscarUsuario(id));
    }

    @PostMapping
    public ResponseEntity<Void> criarUsuario(@RequestBody @Valid UsuarioCreateDTO dto) {
        usuarioService.salvarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> atualizarDadosUsuario(
            @PathVariable String id,
            @RequestBody Map<String, Object> dados,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(id, usuarioLogado);
        usuarioService.atualizarUsuarioParcial(id, dados);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{usuarioId}/enderecos")
    public ResponseEntity<List<EnderecoUsuarioDTO>> buscarEnderecos(
            @PathVariable String usuarioId,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(usuarioId, usuarioLogado);
        return ResponseEntity.ok(usuarioService.listarEnderecos(usuarioId));
    }

    @PostMapping("/{usuarioId}/enderecos")
    public ResponseEntity<Void> adicionarEndereco(
            @PathVariable String usuarioId,
            @RequestBody @Valid EnderecoUsuarioDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(usuarioId, usuarioLogado);
        usuarioService.adicionarEndereco(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{usuarioId}/enderecos/{enderecoId}")
    public ResponseEntity<Void> atualizarEndereco(
            @PathVariable String usuarioId,
            @PathVariable String enderecoId,
            @RequestBody @Valid EnderecoUsuarioDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(usuarioId, usuarioLogado);
        usuarioService.atualizarEndereco(usuarioId, enderecoId, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{usuarioId}/enderecos/{enderecoId}")
    public ResponseEntity<Void> removerEndereco(
            @PathVariable String usuarioId,
            @PathVariable String enderecoId,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(usuarioId, usuarioLogado);
        usuarioService.removerEndereco(usuarioId, enderecoId);
        return ResponseEntity.noContent().build();
    }
}