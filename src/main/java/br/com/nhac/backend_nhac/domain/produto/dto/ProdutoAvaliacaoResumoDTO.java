package br.com.nhac.backend_nhac.domain.produto.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo de avaliações de um produto")
public record ProdutoAvaliacaoResumoDTO(
        @Schema(description = "Total de avaliações do produto", example = "10")
        Long totalAvaliacoes,

        @Schema(description = "Média de notas do produto", example = "4.5")
        Double mediaNotas
) {
}
