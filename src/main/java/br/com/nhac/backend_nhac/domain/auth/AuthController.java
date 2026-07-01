package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.domain.auth.dto.LoginRequestDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.LoginResponseDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.RegistroRequestDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para Login, Registro e Emissão de Tokens JWT")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO body) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(body.email()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado ou senha inválida."));

        if (passwordEncoder.matches(body.senha(), usuario.getSenha())) {
            String token = tokenService.gerarToken(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getId(), usuario.getNome()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/registrar")
    public ResponseEntity<LoginResponseDTO> registrar(@RequestBody @Valid RegistroRequestDTO body) {
        boolean emailJaExiste = usuarioRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(body.email()));

        if (emailJaExiste) {
            return ResponseEntity.badRequest().build();
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setId(body.id());
        novoUsuario.setNome(body.nome());
        novoUsuario.setEmail(body.email());
        novoUsuario.setTelefone(body.telefone());
        novoUsuario.setSenha(passwordEncoder.encode(body.senha()));
        novoUsuario.setEnderecos(new ArrayList<>());

        usuarioRepository.save(novoUsuario);

        String token = tokenService.gerarToken(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDTO(token, novoUsuario.getId(), novoUsuario.getNome()));
    }
}