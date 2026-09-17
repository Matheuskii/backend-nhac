package br.com.nhac.backend_nhac.domain.avaliacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para criação de uma nova avaliação")
public record AvaliacaoCreateDTO(

        @Schema(description = "ID do pedido concluído que está sendo avaliado", example = "pedido_123")
        @NotBlank(message = "O ID do pedido é obrigatório")
        String pedidoId,

        @Schema(description = "Nota dada pelo cliente (1 a 5)", example = "5")
        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota mínima é 1")
        @Max(value = 5, message = "A nota máxima é 5")
        Integer nota,

        @Schema(description = "Comentário opcional da avaliação", example = "Comida maravilhosa, chegou quentinha!")
        @Size(max = 500, message = "O comentário não pode ter mais que 500 caracteres")
        String comentario
) {
}
