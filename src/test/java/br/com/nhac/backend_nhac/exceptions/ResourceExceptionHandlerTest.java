package br.com.nhac.backend_nhac.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ResourceExceptionHandlerTest {

    @Mock private HttpServletRequest request;

    private ResourceExceptionHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new ResourceExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/teste");
    }

    @Test
    @DisplayName("Deve tratar IdNaoEncontradoException devolvendo 404")
    void deveTratarIdNaoEncontradoException() {
        IdNaoEncontradoException excecao = new IdNaoEncontradoException("Recurso não encontrado.");

        ResponseEntity<ErroPadraoDTO> resposta = handler.entidadeNaoEncontrada(excecao, request);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertEquals("Recurso não encontrado.", resposta.getBody().message());
        assertEquals("/api/v1/teste", resposta.getBody().path());
    }

    @Test
    @DisplayName("Deve tratar RegraDeNegocioException devolvendo 400")
    void deveTratarRegraDeNegocioException() {
        RegraDeNegocioException excecao = new RegraDeNegocioException("Regra violada.");

        ResponseEntity<ErroPadraoDTO> resposta = handler.regraDeNegocio(excecao, request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("Regra violada.", resposta.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException devolvendo 400")
    void deveTratarIllegalArgumentException() {
        IllegalArgumentException excecao = new IllegalArgumentException("Argumento inválido.");

        ResponseEntity<ErroPadraoDTO> resposta = handler.regraDeNegocio(excecao, request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("Argumento inválido.", resposta.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException devolvendo 400 com mensagens concatenadas")
    void deveTratarMethodArgumentNotValidException() {
        MethodArgumentNotValidException excecao = mockErroDeValidacao();

        ResponseEntity<ErroPadraoDTO> resposta = handler.validacaoDeCampos(excecao, request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("nome: não pode ser vazio", resposta.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar exceções genéricas devolvendo 500")
    void deveTratarErroGenerico() {
        Exception excecao = new RuntimeException("Erro inesperado.");

        ResponseEntity<ErroPadraoDTO> resposta = handler.erroGenerico(excecao, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertEquals("Erro inesperado.", resposta.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar CredenciaisInvalidasException devolvendo 401")
    void deveTratarCredenciaisInvalidasException() {
        CredenciaisInvalidasException excecao = new CredenciaisInvalidasException("Credenciais inválidas.");

        ResponseEntity<ErroPadraoDTO> resposta = handler.credenciaisInvalidas(excecao, request);

        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
        assertEquals("Credenciais inválidas.", resposta.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar AcessoNegadoException devolvendo 403")
    void deveTratarAcessoNegadoException() {
        AcessoNegadoException excecao = new AcessoNegadoException("Acesso negado.");

        ResponseEntity<ErroPadraoDTO> resposta = handler.acessoNegado(excecao, request);

        assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
        assertEquals("Acesso negado.", resposta.getBody().message());
    }

    private MethodArgumentNotValidException mockErroDeValidacao() {
        MethodArgumentNotValidException excecao = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("dto", "nome", "não pode ser vazio");

        when(excecao.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        return excecao;
    }
}
