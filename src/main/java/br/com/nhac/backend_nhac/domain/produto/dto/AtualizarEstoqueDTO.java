package br.com.nhac.backend_nhac.domain.produto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Payload para atualização rápida da quantidade em estoque de um produto (ex: reposição). Define o valor absoluto do estoque.")
public record AtualizarEstoqueDTO(
        @Schema(description = "Nova quantidade em estoque (valor absoluto, não incremento)", example = "50")
        @NotNull(message = "O estoque é obrigatório.")
        @PositiveOrZero(message = "O estoque não pode ser negativo.")
        Integer estoque
) {
}
