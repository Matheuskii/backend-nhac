package br.com.nhac.backend_nhac.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Schema(description = "Objeto de resposta padrão para erros na API")
public record ErroPadraoDTO(
        @Schema(description = "ID único da requisição (para rastreamento nos logs)")
        String requestId,

        @Schema(description = "Momento exato em que o erro ocorreu")
        Instant timestamp,

        @Schema(description = "Código HTTP do erro (ex: 404, 400)")
        Integer status,

        @Schema(description = "Código do erro padronizado (ex: PRODUTO_NAO_ENCONTRADO)")
        String error,
        
        @Schema(description = "Título curto do erro")
        String title,

        @Schema(description = "Mensagem detalhada para mostrar ao utilizador final no Flutter")
        String message,
        
        @Schema(description = "Detalhes adicionais sobre o erro (ex: id do produto que faltou)")
        Map<String, Object> details,

        @Schema(description = "A rota da API que causou o problema")
        String path,
        
        @Schema(description = "Possíveis soluções ou dicas para o erro")
        List<String> suggestions
) {}