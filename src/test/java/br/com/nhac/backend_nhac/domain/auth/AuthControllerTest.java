package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.domain.auth.dto.LoginRequestDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.LoginResponseDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.RegistroRequestDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.CredenciaisInvalidasException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenService tokenService;

    @InjectMocks private AuthController authController;

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
}