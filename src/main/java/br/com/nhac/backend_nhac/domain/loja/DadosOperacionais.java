package br.com.nhac.backend_nhac.domain.loja;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DadosOperacionais {

    @Schema(description = "Avaliação média da loja pelos clientes", example = "4.6")
    @Column(name = "avaliacao_media")
    private float avaliacaoMedia;

    @Schema(description = "Valor base cobrado para a taxa de entrega", example = "5.99")
    @Column(name = "taxa_entrega_base")
    private BigDecimal taxaEntregaBase;

    @Schema(description = "Tempo mínimo estimado para entrega (em minutos)", example = "20")
    @Column(name = "tempo_entrega_min")
    private int tempoEntregaMin;

    @Schema(description = "Tempo máximo estimado para entrega (em minutos)", example = "40")
    @Column(name = "tempo_entrega_max")
    private int tempoEntregaMax;

    @Schema(description = "Número total de avaliações que a loja já recebeu", example = "456")
    @Column(name = "total_avaliacoes")
    private int totalAvaliacoes;

}