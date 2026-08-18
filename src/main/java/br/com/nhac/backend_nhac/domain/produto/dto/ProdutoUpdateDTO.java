package br.com.nhac.backend_nhac.domain.produto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoUpdateDTO(

        @Schema(description = "Nome do produto que vai aparecer no cardápio", example = "Hossomaki de Salmão Atualizado")
        @NotBlank(message = "O nome do produto não pode estar vazio.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @Schema(description = "Descrição detalhada dos ingredientes", example = "Delicioso rolinho de arroz com salmão fresco e alga, agora com mais sabor.")
        String descricao,

        @Schema(description = "Preço final do produto", example = "28.50")
        @NotNull(message = "O preço é obrigatório.")
        @PositiveOrZero(message = "O preço não pode ser negativo.")
        BigDecimal preco,

        @Schema(description = "Categoria para agrupar no menu do Flutter", example = "Sushi")
        @NotBlank(message = "A categoria do menu é obrigatória.")
        String categoriaMenu,

        @Schema(description = "URL da imagem no Firebase Storage", example = "https://firebasestorage.googleapis.com/.../hossomaki2.png")
        String imagemUrl,

        @Schema(description = "Peso ou porção para exibição", example = "250g")
        String peso,

        @Schema(description = "Percentual de desconto ativo (0 a 100)", example = "15")
        Integer percentualDesconto,

        @Schema(description = "Indica se o produto está ativo e disponível para venda", example = "true")
        @NotNull(message = "O status de atividade (isAtivo) é obrigatório.")
        Boolean isAtivo
) {
}
