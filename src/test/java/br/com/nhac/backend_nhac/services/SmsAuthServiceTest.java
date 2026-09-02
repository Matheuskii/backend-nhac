package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.auth.dto.LoginResponseDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.ValidarCodigoSmsDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsAuthServiceTest {

    @Mock
    private VerificacaoTelefoneService verificacaoTelefoneService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private SmsAuthService smsAuthService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Deve fazer login de usuÃ¡rio existente e retornar isNovoUsuario=false")
    void deveFazerLoginUsuarioExistente() {
        ValidarCodigoSmsDTO dto = new ValidarCodigoSmsDTO("+5511999999999", "123456", null);

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId("user123");
        usuarioExistente.setNome("Matheus Alves");
        usuarioExistente.setTelefone("+5511999999999");

        doNothing().when(verificacaoTelefoneService).validarCodigo(dto);
        when(usuarioRepository.findByTelefone("+5511999999999")).thenReturn(Optional.of(usuarioExistente));
        when(tokenService.gerarToken(usuarioExistente)).thenReturn("token_jwt");

        LoginResponseDTO response = smsAuthService.autenticarComSms(dto);

        verify(verificacaoTelefoneService).validarCodigo(dto);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        
        assertEquals("token_jwt", response.token());
        assertEquals("user123", response.usuarioId());
        assertEquals("Matheus Alves", response.nome());
        assertFalse(response.isNovoUsuario());
    }

    @Test
    @DisplayName("Deve registrar novo usuÃ¡rio de forma invisÃ­vel e retornar isNovoUsuario=true")
    void deveRegistrarNovoUsuario() {
        ValidarCodigoSmsDTO dto = new ValidarCodigoSmsDTO("+5511888888888", "654321", null);

        doNothing().when(verificacaoTelefoneService).validarCodigo(dto);
        when(usuarioRepository.findByTelefone("+5511888888888")).thenReturn(Optional.empty());

        Usuario novoUsuarioSalvo = new Usuario();
        novoUsuarioSalvo.setId("novo_id");
        novoUsuarioSalvo.setNome("Novo Usuário");
        novoUsuarioSalvo.setTelefone("+5511888888888");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(novoUsuarioSalvo);
        when(tokenService.gerarToken(novoUsuarioSalvo)).thenReturn("novo_token_jwt");

        LoginResponseDTO response = smsAuthService.autenticarComSms(dto);

        verify(verificacaoTelefoneService).validarCodigo(dto);
        
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioSalvo = captor.getValue();
        assertEquals("Novo Usuário", usuarioSalvo.getNome());
        assertNull(usuarioSalvo.getEmail());
        assertEquals("+5511888888888", usuarioSalvo.getTelefone());
        assertTrue(usuarioSalvo.isTelefoneVerificado());

        assertEquals("novo_token_jwt", response.token());
        assertEquals("novo_id", response.usuarioId());
        assertEquals("Novo Usuário", response.nome());
        assertTrue(response.isNovoUsuario());
    }
}
