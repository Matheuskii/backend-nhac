package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.auth.CodigoVerificacao;
import br.com.nhac.backend_nhac.domain.auth.dto.EnviarCodigoSmsDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.ValidarCodigoSmsDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.repositories.CodigoVerificacaoRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacaoTelefoneServiceTest {

    @Mock
    private CodigoVerificacaoRepository codigoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private VerificacaoTelefoneService verificacaoTelefoneService;

    private EnviarCodigoSmsDTO enviarDto;
    private ValidarCodigoSmsDTO validarDto;
    private CodigoVerificacao codigoValido;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        enviarDto = new EnviarCodigoSmsDTO("11999999999");
        validarDto = new ValidarCodigoSmsDTO("11999999999", "123456", null);

        codigoValido = CodigoVerificacao.builder()
                .telefone("11999999999")
                .codigo("123456")
                .dataExpiracao(LocalDateTime.now().plusMinutes(5))
                .tentativas(0)
                .utilizado(false)
                .build();

        usuario = new Usuario();
        usuario.setTelefone("11999999999");
        usuario.setTelefoneVerificado(false);
    }

    @Test
    void enviarCodigo_Sucesso() {
        verificacaoTelefoneService.enviarCodigo(enviarDto);

        verify(codigoRepository).inativarCodigosAtivosPorTelefone("11999999999");

        ArgumentCaptor<CodigoVerificacao> codigoCaptor = ArgumentCaptor.forClass(CodigoVerificacao.class);
        verify(codigoRepository).save(codigoCaptor.capture());

        CodigoVerificacao salvo = codigoCaptor.getValue();
        assertEquals("11999999999", salvo.getTelefone());
        assertNotNull(salvo.getCodigo());
        assertEquals(6, salvo.getCodigo().length());
        assertFalse(salvo.isUtilizado());
        assertEquals(0, salvo.getTentativas());

        verify(smsService).enviarSms(eq("11999999999"), anyString());
    }

    @Test
    void validarCodigo_Sucesso() {
        when(codigoRepository.findTopByTelefoneAndUtilizadoFalseAndDataExpiracaoAfterOrderByCriadoEmDesc(
                eq("11999999999"), any(LocalDateTime.class))).thenReturn(Optional.of(codigoValido));
        
        when(usuarioRepository.findByTelefone("11999999999")).thenReturn(Optional.of(usuario));

        verificacaoTelefoneService.validarCodigo(validarDto);

        assertTrue(codigoValido.isUtilizado());
        verify(codigoRepository).save(codigoValido);
        
        assertTrue(usuario.isTelefoneVerificado());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void validarCodigo_Falha_NaoEncontradoOuExpirado() {
        when(codigoRepository.findTopByTelefoneAndUtilizadoFalseAndDataExpiracaoAfterOrderByCriadoEmDesc(
                eq("11999999999"), any(LocalDateTime.class))).thenReturn(Optional.empty());

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, () -> {
            verificacaoTelefoneService.validarCodigo(validarDto);
        });

        assertEquals("Código expirado ou não encontrado. Solicite um novo código.", exception.getMessage());
        verify(codigoRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void validarCodigo_Falha_CodigoInvalido() {
        codigoValido.setCodigo("654321"); // Different code

        when(codigoRepository.findTopByTelefoneAndUtilizadoFalseAndDataExpiracaoAfterOrderByCriadoEmDesc(
                eq("11999999999"), any(LocalDateTime.class))).thenReturn(Optional.of(codigoValido));

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, () -> {
            verificacaoTelefoneService.validarCodigo(validarDto);
        });

        assertEquals("Código de verificação inválido.", exception.getMessage());
        assertEquals(1, codigoValido.getTentativas());
        assertFalse(codigoValido.isUtilizado());
        verify(codigoRepository).save(codigoValido);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void validarCodigo_Falha_BloqueioPorTentativasExcedidas() {
        codigoValido.setTentativas(3); // Already at max

        when(codigoRepository.findTopByTelefoneAndUtilizadoFalseAndDataExpiracaoAfterOrderByCriadoEmDesc(
                eq("11999999999"), any(LocalDateTime.class))).thenReturn(Optional.of(codigoValido));

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, () -> {
            verificacaoTelefoneService.validarCodigo(validarDto);
        });

        assertEquals("Limite de tentativas excedido para este código. Solicite um novo.", exception.getMessage());
        assertTrue(codigoValido.isUtilizado()); // Should mark as utilized
        verify(codigoRepository).save(codigoValido);
        verify(usuarioRepository, never()).save(any());
    }
}
