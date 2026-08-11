package br.com.nhac.backend_nhac.domain.usuario;

import br.com.nhac.backend_nhac.domain.usuario.dto.EnderecoUsuarioDTO;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/usuarios")
public class EnderecoUsuarioController {

    private final UsuarioService usuarioService;

    public EnderecoUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private void validarPropriedade(String idNaUrl, Usuario usuarioLogado) {
        if (!idNaUrl.equals(usuarioLogado.getId())) {
            throw new AcessoNegadoException("Acesso negado: não tem permissão para aceder ou modificar os dados de outro utilizador.");
        }
    }



    @GetMapping("/{usuarioId}/enderecos")
    public ResponseEntity<List<EnderecoUsuarioDTO>> buscarEnderecos(
            @PathVariable String usuarioId,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        usuarioService.validarPropriedade(usuarioId, usuarioLogado);
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
