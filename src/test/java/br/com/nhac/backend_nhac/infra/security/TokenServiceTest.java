package br.com.nhac.backend_nhac.infra.security;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "chave_secreta_de_teste_1234567890");
    }

    private Usuario usuarioDeTeste() {
        Usuario usuario = new Usuario();
        usuario.setId("user_1");
        usuario.setNome("Matheus Alves");
        usuario.setEmail("matheus@nhac.com");
        return usuario;
    }

    @Test
    @DisplayName("Deve gerar um token JWT não nulo e não vazio para um usuário válido")
    void deveGerarTokenComSucesso() {
        String token = tokenService.gerarToken(usuarioDeTeste());

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Deve validar um token gerado e devolver o e-mail (subject) correto")
    void deveValidarTokenERecuperarEmail() {
        String token = tokenService.gerarToken(usuarioDeTeste());

        String emailExtraido = tokenService.validarToken(token);

        assertEquals("matheus@nhac.com", emailExtraido);
    }

    @Test
    @DisplayName("Deve retornar null ao validar um token inválido ou corrompido")
    void deveRetornarNullParaTokenInvalido() {
        String resultado = tokenService.validarToken("token.completamente.invalido");

        assertNull(resultado);
    }

    @Test
    @DisplayName("Deve retornar null ao validar um token assinado com outra chave secreta")
    void deveRetornarNullParaTokenAssinadoComOutraChave() {
        String token = tokenService.gerarToken(usuarioDeTeste());

        TokenService outroServico = new TokenService();
        ReflectionTestUtils.setField(outroServico, "secret", "outra_chave_completamente_diferente");

        assertNull(outroServico.validarToken(token));
    }
}
