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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();


    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private GoogleAuthService googleAuthService;

    @Mock
    private SmsAuthService smsAuthService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("Deve retornar 200 e o token JWT ao realizar login por SMS com sucesso")
    void deveAutenticarComSmsComSucesso() throws Exception {
        br.com.nhac.backend_nhac.domain.auth.dto.ValidarCodigoSmsDTO requisicao = new br.com.nhac.backend_nhac.domain.auth.dto.ValidarCodigoSmsDTO("+5511999999999", "123456", null);

        LoginResponseDTO respostaEsperada = new LoginResponseDTO("jwt_gerado_pelo_backend_sms", "user_novo", "Novo Usuário", true);

        when(smsAuthService.autenticarComSms(requisicao)).thenReturn(respostaEsperada);

        mockMvc.perform(post("/api/v1/auth/login-sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_gerado_pelo_backend_sms"))
                .andExpect(jsonPath("$.usuarioId").value("user_novo"))
                .andExpect(jsonPath("$.isNovoUsuario").value(true));
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException (Erro 401) quando a senha estiver incorreta")
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        LoginRequestDTO requisicao = new LoginRequestDTO("matheus@nhac.com", "senha_errada");

        Usuario usuarioDoBanco = new Usuario();
        usuarioDoBanco.setEmail("matheus@nhac.com");
        usuarioDoBanco.setSenha("hash_da_senha_correta");

        when(usuarioRepository.findByEmailIgnoreCase("matheus@nhac.com")).thenReturn(Optional.of(usuarioDoBanco));

        when(passwordEncoder.matches("senha_errada", "hash_da_senha_correta")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class, () -> {
            authController.login(requisicao);
        });

        verify(tokenService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("Deve efetuar login com sucesso e devolver o token quando as credenciais forem válidas")
    void deveEfetuarLoginComSucesso() {
        LoginRequestDTO requisicao = new LoginRequestDTO("matheus@nhac.com", "senha_correta");

        Usuario usuarioDoBanco = new Usuario();
        usuarioDoBanco.setId("user_1");
        usuarioDoBanco.setNome("Matheus Alves");
        usuarioDoBanco.setEmail("matheus@nhac.com");
        usuarioDoBanco.setSenha("hash_da_senha_correta");

        when(usuarioRepository.findByEmailIgnoreCase("matheus@nhac.com")).thenReturn(Optional.of(usuarioDoBanco));
        when(passwordEncoder.matches("senha_correta", "hash_da_senha_correta")).thenReturn(true);
        when(tokenService.gerarToken(usuarioDoBanco)).thenReturn("token_jwt_gerado");

        ResponseEntity<LoginResponseDTO> resposta = authController.login(requisicao);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("token_jwt_gerado", resposta.getBody().token());
        assertEquals("user_1", resposta.getBody().usuarioId());
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando o e-mail não for encontrado")
    void deveLancarExcecaoQuandoEmailNaoEncontrado() {
        LoginRequestDTO requisicao = new LoginRequestDTO("fantasma@nhac.com", "qualquer_senha");

        when(usuarioRepository.findByEmailIgnoreCase("fantasma@nhac.com")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class, () -> authController.login(requisicao));

        verify(tokenService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso quando o e-mail ainda não estiver em uso")
    void deveRegistrarUsuarioComSucesso() {
        RegistroRequestDTO requisicao = new RegistroRequestDTO(
                "user_novo", "Novo Usuário", "novo@nhac.com", "11999998888", "senha123");

        when(usuarioRepository.findByEmailIgnoreCase("novo@nhac.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senha_encriptada");
        when(tokenService.gerarToken(any(Usuario.class))).thenReturn("token_jwt_gerado");

        ResponseEntity<LoginResponseDTO> resposta = authController.registrar(requisicao);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals("token_jwt_gerado", resposta.getBody().token());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("novo@nhac.com", captor.getValue().getEmail());
        assertEquals("senha_encriptada", captor.getValue().getSenha());
    }

    @Test
    @DisplayName("Deve lançar RegraDeNegocioException ao tentar registrar um e-mail já em uso")
    void deveLancarExcecaoAoRegistrarEmailDuplicado() {
        RegistroRequestDTO requisicao = new RegistroRequestDTO(
                "user_novo", "Novo Usuário", "matheus@nhac.com", "11999998888", "senha123");

        when(usuarioRepository.findByEmailIgnoreCase("matheus@nhac.com"))
                .thenReturn(Optional.of(new Usuario()));

        assertThrows(RegraDeNegocioException.class, () -> authController.registrar(requisicao));

        verify(usuarioRepository, never()).save(any());
        verify(tokenService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("Deve retornar 200 e o token JWT ao receber um ID Token válido do Google")
    void deveAutenticarComGoogleComSucesso() throws Exception {
        SocialLoginRequestDTO requisicao = new SocialLoginRequestDTO("token_google_falso_mas_mockado");

        LoginResponseDTO respostaEsperada = new LoginResponseDTO("jwt_gerado_pelo_backend", "user_1", "Usuário Nhac", false);

        when(googleAuthService.autenticarComGoogle("token_google_falso_mas_mockado")).thenReturn(respostaEsperada);

        mockMvc.perform(post("/api/v1/auth/social")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_gerado_pelo_backend"))
                .andExpect(jsonPath("$.usuarioId").value("user_1"));
    }

    @Test
    @DisplayName("Deve retornar existe true quando o e-mail já estiver cadastrado")
    void deveRetornarExisteTrueQuandoEmailCadastrado() {
        ChecarEmailRequestDTO requisicao = new ChecarEmailRequestDTO("existente@nhac.com");

        when(usuarioRepository.findByEmailIgnoreCase("existente@nhac.com")).thenReturn(Optional.of(new Usuario()));

        ResponseEntity<ChecarEmailResponseDTO> resposta = authController.checarEmail(requisicao);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertTrue(resposta.getBody().existe());
    }

    @Test
    @DisplayName("Deve retornar existe false quando o e-mail não estiver cadastrado")
    void deveRetornarExisteFalseQuandoEmailNaoCadastrado() {
        ChecarEmailRequestDTO requisicao = new ChecarEmailRequestDTO("novo@nhac.com");

        when(usuarioRepository.findByEmailIgnoreCase("novo@nhac.com")).thenReturn(Optional.empty());

        ResponseEntity<ChecarEmailResponseDTO> resposta = authController.checarEmail(requisicao);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertFalse(resposta.getBody().existe());
    }
}