package br.com.nhac.backend_nhac.domain.produto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoDetalhesDTO(
        @Schema(description = "ID da loja à qual o produto pertence", example = "loja_japonesa_001")
        String lojaId,

        @Schema(description = "Nome do produto que vai aparecer no cardápio", example = "Hossomaki de Salmão")
        String nome,

        @Schema(description = "Descrição detalhada dos ingredientes", example = "Delicioso rolinho de arroz com salmão fresco e alga.")
        String descricao,

        @Schema(description = "Preço final do produto", example = "25.50")
        BigDecimal preco,

        @Schema(description = "Categoria para agrupar no menu do Flutter", example = "Sushi")
        String categoriaMenu,

        @Schema(description = "URL da imagem no Firebase Storage", example = "https://firebasestorage.googleapis.com/.../hossomaki.png")
        String imagemUrl,

        @Schema(description = "Peso ou porção para exibição", example = "200g")
        String peso,

        @Schema(description = "Percentual de desconto ativo (0 a 100)", example = "10")
        Integer percentualDesconto
) {
}
