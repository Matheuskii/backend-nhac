package br.com.nhac.backend_nhac.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ResourceExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceExceptionHandler.class);

    @ExceptionHandler(NhacException.class)
    public ResponseEntity<ErroPadraoDTO> handleNhacException(NhacException e, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.error("Erro Nhac: {}, RequestId: {}", e.getErrorCode(), requestId, e);

        ErroPadraoDTO erro = new ErroPadraoDTO(
                requestId,
                Instant.now(),
                e.getHttpStatus().value(),
                e.getErrorCode() != null ? e.getErrorCode().getCode() : "ERRO_DESCONHECIDO",
                getFriendlyTitle(e.getHttpStatus()),
                e.getMessage(),
                e.getDetails(),
                request.getRequestURI(),
                Collections.emptyList()
        );

        return ResponseEntity.status(e.getHttpStatus()).body(erro);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroPadraoDTO> regraDeNegocio(IllegalArgumentException e, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logger.warn("IllegalArgumentException: {}, RequestId: {}", e.getMessage(), requestId);

        ErroPadraoDTO erro = new ErroPadraoDTO(
                requestId,
                Instant.now(),
                status.value(),
                ErrorCode.REGRA_DE_NEGOCIO.getCode(),
                "Violação de Regra de Negócio",
                e.getMessage(),
                Collections.emptyMap(),
                request.getRequestURI(),
                Collections.emptyList()
        );
        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoDTO> validacaoDeCampos(MethodArgumentNotValidException e, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logger.warn("Validation Error, RequestId: {}", requestId);

        String mensagensValidacao = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                    fieldError -> fieldError.getField(),
                    fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Campo inválido",
                    (msg1, msg2) -> msg1 + "; " + msg2
                ));

        ErroPadraoDTO erro = new ErroPadraoDTO(
                requestId,
                Instant.now(),
                status.value(),
                ErrorCode.VALIDACAO_FALHOU.getCode(),
                "Erro de Validação de Dados",
                "Alguns campos enviados são inválidos. Verifique os detalhes.",
                details,
                request.getRequestURI(),
                Collections.singletonList("Verifique os campos informados e tente novamente.")
        );
        return ResponseEntity.status(status).body(erro);
    }


    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErroPadraoDTO> noResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException e, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        HttpStatus status = HttpStatus.NOT_FOUND;
        logger.warn("Rota nǜo encontrada: {}, RequestId: {}", request.getRequestURI(), requestId);

        ErroPadraoDTO erro = new ErroPadraoDTO(
                requestId,
                Instant.now(),
                status.value(),
                "ROTA_NAO_ENCONTRADA",
                "Rota Nǜo Encontrada",
                "A rota solicitada nǜo existe no servidor.",
                Collections.emptyMap(),
                request.getRequestURI(),
                Collections.singletonList("Verifique a URL e o mǸtodo HTTP solicitados.")
        );
        return ResponseEntity.status(status).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoDTO> erroGenerico(Exception e, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logger.error("Erro Interno não tratado. RequestId: {}", requestId, e);

        ErroPadraoDTO erro = new ErroPadraoDTO(
                requestId,
                Instant.now(),
                status.value(),
                ErrorCode.ERRO_INTERNO_SERVIDOR.getCode(),
                "Erro Interno do Servidor",
                "Ocorreu um erro inesperado no servidor. Por favor, tente novamente mais tarde.",
                Collections.emptyMap(),
                request.getRequestURI(),
                Collections.emptyList()
        );
        return ResponseEntity.status(status).body(erro);
    }
    
    private String getFriendlyTitle(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Recurso Não Encontrado";
            case BAD_REQUEST -> "Requisição Inválida";
            case UNAUTHORIZED -> "Não Autorizado";
            case FORBIDDEN -> "Acesso Negado";
            case UNPROCESSABLE_ENTITY -> "Entidade Não Processável";
            case TOO_MANY_REQUESTS -> "Muitas Requisições";
            case PAYMENT_REQUIRED -> "Pagamento Recusado";
            default -> "Erro na Requisição";
        };
    }
}