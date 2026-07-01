package br.com.nhac.backend_nhac.domain.usuario;

import br.com.nhac.backend_nhac.domain.usuario.dto.EnderecoUsuarioDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioDTO;
import br.com.nhac.backend_nhac.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarUsuario(@PathVariable String id) {
        return ResponseEntity.ok(usuarioService.buscarUsuario(id));
    }

    @PostMapping
    public ResponseEntity<Void> criarUsuario(@RequestBody UsuarioDTO dto) {
        usuarioService.salvarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> atualizarDadosUsuario(@PathVariable String id, @RequestBody Map<String, Object> dados) {
        usuarioService.atualizarUsuarioParcial(id, dados);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{usuarioId}/enderecos")
    public ResponseEntity<List<EnderecoUsuarioDTO>> buscarEnderecos(@PathVariable String usuarioId) {
        return ResponseEntity.ok(usuarioService.listarEnderecos(usuarioId));
    }

    @PostMapping("/{usuarioId}/enderecos")
    public ResponseEntity<Void> adicionarEndereco(@PathVariable String usuarioId, @RequestBody EnderecoUsuarioDTO dto) {
        usuarioService.adicionarEndereco(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{usuarioId}/enderecos/{enderecoId}")
    public ResponseEntity<Void> atualizarEndereco(
            @PathVariable String usuarioId,
            @PathVariable String enderecoId,
            @RequestBody EnderecoUsuarioDTO dto) {
        usuarioService.atualizarEndereco(usuarioId, enderecoId, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{usuarioId}/enderecos/{enderecoId}")
    public ResponseEntity<Void> removerEndereco(@PathVariable String usuarioId, @PathVariable String enderecoId) {
        usuarioService.removerEndereco(usuarioId, enderecoId);
        return ResponseEntity.noContent().build();
    }
}