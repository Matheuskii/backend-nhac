package br.com.nhac.backend_nhac.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(IdNaoEncontradoException.class)
    public ResponseEntity<ErroPadraoDTO> entidadeNaoEncontrada(IdNaoEncontradoException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErroPadraoDTO erro = new ErroPadraoDTO(
                Instant.now(),
                status.value(),
                "Recurso Não Encontrado",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler({RegraDeNegocioException.class, IllegalArgumentException.class})
    public ResponseEntity<ErroPadraoDTO> regraDeNegocio(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErroPadraoDTO erro = new ErroPadraoDTO(
                Instant.now(),
                status.value(),
                "Violação de Regra de Negócio",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoDTO> validacaoDeCampos(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY; // 422

        String mensagensValidacao = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErroPadraoDTO erro = new ErroPadraoDTO(
                Instant.now(),
                status.value(),
                "Erro de Validação de Dados",
                mensagensValidacao,
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
                "Erro Interno do Servidor",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(erro);
    }
}