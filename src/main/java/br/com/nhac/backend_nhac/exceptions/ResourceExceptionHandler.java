package br.com.nhac.backend_nhac.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErroPadraoDTO> rotaNaoEncontrada(org.springframework.web.servlet.resource.NoResourceFoundException e, HttpServletRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        ErroPadraoDTO erro = new ErroPadraoDTO(
                Instant.now(),
                status.value(),
                "Rota não encontrada",
                "O caminho da URL que tentaste acessar não existe neste servidor.",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(IdNaoEncontradoException.class)
    public ResponseEntity<ErroPadraoDTO> recursoNaoEncontrado(IdNaoEncontradoException e, HttpServletRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        ErroPadraoDTO erro = new ErroPadraoDTO(
                Instant.now(),
                status.value(),
                "Recurso não encontrado",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoDTO> erroGenerico(Exception e, HttpServletRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErroPadraoDTO erro = new ErroPadraoDTO(
                Instant.now(),
                status.value(),
                "Erro interno no servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                request.getRequestURI()
        );

        e.printStackTrace();

        return ResponseEntity.status(status).body(erro);
    }
}