package br.com.nhac.backend_nhac.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Objeto de resposta padrão para erros na API")
public record ErroPadraoDTO(
        @Schema(description = "Momento exato em que o erro ocorreu")
        Instant timestamp,

        @Schema(description = "Código HTTP do erro (ex: 404, 400)")
        Integer status,

        @Schema(description = "Título curto do erro (ex: Not Found, Bad Request)")
        String error,

        @Schema(description = "Mensagem detalhada para mostrar ao utilizador final no Flutter")
        String message,

        @Schema(description = "A rota da API que causou o problema")
        String path
) {}