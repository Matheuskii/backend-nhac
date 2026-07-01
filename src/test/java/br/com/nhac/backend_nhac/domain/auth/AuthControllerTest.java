package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.domain.auth.dto.LoginRequestDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.CredenciaisInvalidasException;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
}