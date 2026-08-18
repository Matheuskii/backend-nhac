package br.com.nhac.backend_nhac.domain.avaliacao.dto;

import br.com.nhac.backend_nhac.domain.avaliacao.Avaliacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO de visualização de uma avaliação")
public record AvaliacaoResumoDTO(

        @Schema(description = "ID da avaliação", example = "aval_123")
        String id,

        @Schema(description = "Nome do usuário que fez a avaliação", example = "Maria Silva")
        String nomeUsuario,

        @Schema(description = "Nota dada pelo cliente", example = "5")
        Integer nota,

        @Schema(description = "Comentário da avaliação", example = "Comida maravilhosa!")
        String comentario,

        @Schema(description = "Data e hora em que a avaliação foi criada")
        LocalDateTime dataCriacao
) {
    public AvaliacaoResumoDTO(Avaliacao avaliacao) {
        this(
                avaliacao.getId(),
                avaliacao.getUsuario() != null ? avaliacao.getUsuario().getNome() : "Usuário Anônimo",
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataCriacao()
        );
    }
}
