package br.com.nhac.backend_nhac.infra.security;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock private TokenService tokenService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private SecurityFilter securityFilter;

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private Usuario usuarioDeTeste() {
        Usuario usuario = new Usuario();
        usuario.setId("user_1");
        usuario.setEmail("matheus@nhac.com");
        return usuario;
    }

    @Test
    @DisplayName("Deve seguir a cadeia de filtros sem autenticar quando não houver header Authorization")
    void deveSeguirSemAutenticarQuandoSemHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("Deve seguir sem autenticar quando o header não começar com 'Bearer '")
    void deveSeguirSemAutenticarQuandoHeaderForaDoPadrao() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("Deve autenticar o usuário no contexto quando o token for válido e o usuário existir")
    void deveAutenticarComTokenValido() throws Exception {
        Usuario usuario = usuarioDeTeste();

        when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
        when(tokenService.validarToken("token_valido")).thenReturn("matheus@nhac.com");
        when(usuarioRepository.findByEmailIgnoreCase("matheus@nhac.com")).thenReturn(java.util.Optional.of(usuario));

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o token for inválido")
    void naoDeveAutenticarComTokenInvalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token_invalido");
        when(tokenService.validarToken("token_invalido")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(usuarioRepository, never()).findByEmailIgnoreCase(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o e-mail do token não corresponder a nenhum usuário")
    void naoDeveAutenticarQuandoUsuarioNaoEncontrado() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
        when(tokenService.validarToken("token_valido")).thenReturn("fantasma@nhac.com");
        when(usuarioRepository.findByEmailIgnoreCase("fantasma@nhac.com")).thenReturn(java.util.Optional.empty());

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
