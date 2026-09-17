package br.com.nhac.backend_nhac.infra.security;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
        when(tokenService.validarToken("token_valido")).thenReturn("user_1");
        when(usuarioRepository.findById("user_1")).thenReturn(java.util.Optional.of(usuario));

        // A Authentication só existe DENTRO da cadeia — o filtro limpa o contexto
        // no finally (SecurityContextHolder.clearContext()). Por isso capturamos
        // o valor no momento em que doFilter é invocado, e não depois do método.
        final Authentication[] capturada = new Authentication[1];
        doAnswer(invocation -> {
            capturada[0] = SecurityContextHolder.getContext().getAuthentication();
            return null;
        }).when(filterChain).doFilter(request, response);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(capturada[0], "Authentication deveria existir durante a cadeia de filtros");
        assertEquals(usuario, capturada[0].getPrincipal());
        verify(filterChain, times(1)).doFilter(request, response);

        // Comportamento novo (correção de segurança): o contexto é limpo após o filtro,
        // evitando que a Authentication vaze entre requisições processadas pela mesma thread.
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "clearContext() deve ter limpado o contexto após o filtro");
    }

    @Test
    @DisplayName("Não deve autenticar quando o token for inválido")
    void naoDeveAutenticarComTokenInvalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token_invalido");
        when(tokenService.validarToken("token_invalido")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(usuarioRepository, never()).findById(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o ID do token não corresponder a nenhum usuário")
    void naoDeveAutenticarQuandoUsuarioNaoEncontrado() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
        when(tokenService.validarToken("token_valido")).thenReturn("user_fantasma");
        when(usuarioRepository.findById("user_fantasma")).thenReturn(java.util.Optional.empty());

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar um usuário desativado, mesmo com token ainda válido (ex: funcionário demitido)")
    void naoDeveAutenticarUsuarioDesativado() throws Exception {
        Usuario usuario = usuarioDeTeste();
        usuario.setAtivo(false);

        when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
        when(tokenService.validarToken("token_valido")).thenReturn("user_1");
        when(usuarioRepository.findById("user_1")).thenReturn(java.util.Optional.of(usuario));

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}