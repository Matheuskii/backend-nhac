package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.domain.auth.dto.ChecarEmailRequestDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.ChecarEmailResponseDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.LoginRequestDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.LoginResponseDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.RegistroRequestDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.SocialLoginRequestDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.CredenciaisInvalidasException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import br.com.nhac.backend_nhac.services.GoogleAuthService;
import br.com.nhac.backend_nhac.services.SmsAuthService;
import br.com.nhac.backend_nhac.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final GoogleAuthService googleAuthService;
    private final SmsAuthService smsAuthService;
    private final br.com.nhac.backend_nhac.services.VerificacaoTelefoneService verificacaoTelefoneService;
    private final br.com.nhac.backend_nhac.services.VerificacaoEmailService verificacaoEmailService;
    private final UsuarioService usuarioService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService, GoogleAuthService googleAuthService, SmsAuthService smsAuthService, br.com.nhac.backend_nhac.services.VerificacaoTelefoneService verificacaoTelefoneService, br.com.nhac.backend_nhac.services.VerificacaoEmailService verificacaoEmailService, UsuarioService usuarioService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.googleAuthService = googleAuthService;
        this.smsAuthService = smsAuthService;
        this.verificacaoTelefoneService = verificacaoTelefoneService;
        this.verificacaoEmailService = verificacaoEmailService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO body) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(body.email())
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail não encontrado ou senha inválida."));

        if (passwordEncoder.matches(body.senha(), usuario.getSenha())) {
            String token = tokenService.gerarToken(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getId(), usuario.getNome(), false));
        }

        throw new CredenciaisInvalidasException("E-mail não encontrado ou senha inválida.");
    }

    @PostMapping("/registrar")
    public ResponseEntity<LoginResponseDTO> registrar(@RequestBody @Valid RegistroRequestDTO body) {
        if (usuarioRepository.findByEmailIgnoreCase(body.email()).isPresent()) {
            throw new RegraDeNegocioException("Este e-mail já está em uso.");
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
        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDTO(token, novoUsuario.getId(), novoUsuario.getNome(), false));
    }

    @PostMapping("/social")
    public ResponseEntity<LoginResponseDTO> loginSocial(@RequestBody @Valid SocialLoginRequestDTO dto){
        LoginResponseDTO response = googleAuthService.autenticarComGoogle(dto.idToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-sms")
    @Operation(summary = "Realiza login via SMS (Passwordless)", description = "Valida o OTP. Se o telefone não existir, cadastra um novo usuário de forma invisível.")
    public ResponseEntity<LoginResponseDTO> loginSms(@RequestBody @Valid br.com.nhac.backend_nhac.domain.auth.dto.ValidarCodigoSmsDTO dto) {
        LoginResponseDTO response = smsAuthService.autenticarComSms(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verifica se um e-mail já existe", description = "Retorna se o e-mail informado já está cadastrado no sistema. Útil para direcionar o usuário para o fluxo de login ou de cadastro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "E-mail inválido ou não informado")
    })
    @PostMapping("/checar-email")
    public ResponseEntity<ChecarEmailResponseDTO> checarEmail(@RequestBody @Valid ChecarEmailRequestDTO body) {
        boolean existe = usuarioRepository.findByEmailIgnoreCase(body.email()).isPresent();
        return ResponseEntity.ok(new ChecarEmailResponseDTO(existe));
    }

    @Operation(summary = "Solicitar redefinição de senha", description = "Envia um SMS com código para redefinição caso o telefone exista.")
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(
            @RequestBody @Valid br.com.nhac.backend_nhac.domain.auth.dto.EsqueciSenhaDTO dto) {
        
        Usuario usuario = usuarioRepository.findByTelefone(dto.telefone().trim())
                .orElseThrow(() -> new br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException("Nenhum usuário encontrado com este telefone."));

        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioException("Usuário inativo.");
        }

        verificacaoTelefoneService.enviarCodigoReset(dto.telefone());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Concluir redefinição de senha", description = "Valida o código SMS e atualiza a senha do usuário.")
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(
            @RequestBody @Valid br.com.nhac.backend_nhac.domain.auth.dto.RedefinirSenhaDTO dto) {

        verificacaoTelefoneService.verificarCodigoValido(dto.telefone(), dto.codigo());
        usuarioService.atualizarSenha(dto.telefone(), dto.novaSenha());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Solicitar redefinição de senha por e-mail", description = "Envia um e-mail com código para redefinição caso o e-mail exista.")
    @PostMapping("/esqueci-senha/email")
    public ResponseEntity<Void> esqueciSenhaEmail(
            @RequestBody @Valid br.com.nhac.backend_nhac.domain.auth.dto.EsqueciSenhaEmailDTO dto) {
        
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dto.email().trim())
                .orElseThrow(() -> new br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException("Nenhum usuário encontrado com este e-mail."));

        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioException("Usuário inativo.");
        }

        verificacaoEmailService.enviarCodigoReset(dto.email());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Concluir redefinição de senha por e-mail", description = "Valida o código do e-mail e atualiza a senha do usuário.")
    @PostMapping("/redefinir-senha/email")
    public ResponseEntity<Void> redefinirSenhaEmail(
            @RequestBody @Valid br.com.nhac.backend_nhac.domain.auth.dto.RedefinirSenhaEmailDTO dto) {

        verificacaoEmailService.verificarCodigoValido(dto.email(), dto.codigo());
        usuarioService.atualizarSenhaPorEmail(dto.email(), dto.novaSenha());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário logado validando a senha atual.")
    @PutMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuarioLogado,
            @RequestBody @Valid br.com.nhac.backend_nhac.domain.auth.dto.AlterarSenhaDTO dto) {

        if (usuarioLogado.getSenha() == null) {
            throw new RegraDeNegocioException("Esta conta não possui senha cadastrada. Ela foi criada via login por telefone.");
        }

        if (!passwordEncoder.matches(dto.senhaAtual(), usuarioLogado.getSenha())) {
            throw new br.com.nhac.backend_nhac.exceptions.CredenciaisInvalidasException("A senha atual informada está incorreta.");
        }

        usuarioLogado.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuarioLogado);

        return ResponseEntity.ok().build();
    }
}