package br.com.nhac.backend_nhac.domain.usuario;

import br.com.nhac.backend_nhac.domain.auth.dto.LoginResponseDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.EnderecoUsuarioDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioAtualizarDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioResponseDTO;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException; // 🔴 NOVO IMPORT
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
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

    private final UsuarioRepository usuarioRepository;

    private final TokenService tokenService;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository, TokenService tokenService) {
        this.usuarioService = usuarioService;
        this.usuarioRepository= usuarioRepository;
        this.tokenService = tokenService;
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

    @PutMapping("/{id}")
    public ResponseEntity<LoginResponseDTO> atualizarDadosUsuario(
            @PathVariable String id,
            @RequestBody @Valid UsuarioAtualizarDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(id, usuarioLogado);

        usuarioService.atualizarUsuarioParcial(id, dados);

        Usuario usuarioAtualizado = usuarioRepository.findById(id).get();

        String novoToken = tokenService.gerarToken(usuarioAtualizado);

        return ResponseEntity.ok(new LoginResponseDTO(novoToken, usuarioAtualizado.getId(), usuarioAtualizado.getNome()));
    }


}