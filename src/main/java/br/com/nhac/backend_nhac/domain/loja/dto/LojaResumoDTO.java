package br.com.nhac.backend_nhac.domain.loja.dto;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Objeto simplificado devolvido na listagem de lojas para otimização de rede")
public record LojaResumoDTO(

        @Schema(description = "Identificador único da loja", example = "loja_japonesa_001")
        String id,

        @Schema(description = "Nome fantasia da loja", example = "Sushi Ken")
        String nome,

        @Schema(description = "Breve descrição ou slogan da loja", example = "O melhor sushi da região.")
        String descricao,

        @Schema(description = "Categoria principal para filtros", example = "Japonesa")
        String categoria,

        @Schema(description = "URL do banner da loja no Firebase Storage", example = "https://firebasestorage.../banner.png")
        String imagemUrl,

        @Schema(description = "Métricas principais da loja para exibição no card")
        DadosOperacionaisDTO dadosOperacionais
) {
    @Schema(description = "Dados de logística e avaliação resumidos")
    public record DadosOperacionaisDTO(
            @Schema(description = "Média das avaliações (0 a 5)", example = "4.8")
            float avaliacaoMedia,

            @Schema(description = "Taxa de entrega base", example = "5.99")
            BigDecimal taxaEntregaBase,

            @Schema(description = "Tempo mínimo estimado em minutos", example = "30")
            int tempoEntregaMin,

            @Schema(description = "Tempo máximo estimado em minutos", example = "45")
            int tempoEntregaMax,

            @Schema(description = "Quantidade total de avaliações recebidas", example = "150")
            int totalAvaliacoes
    ) {}

    public LojaResumoDTO(Loja loja){
        this( loja.getId(),
                loja.getNome(),
                loja.getDescricao(),
                loja.getCategoria(),
                loja.getImagemUrl(),
                new LojaResumoDTO.DadosOperacionaisDTO(
                        loja.getDadosOperacionais().getAvaliacaoMedia(),
                        loja.getDadosOperacionais().getTaxaEntregaBase(),
                        loja.getDadosOperacionais().getTempoEntregaMin(),
                        loja.getDadosOperacionais().getTempoEntregaMax(),
                        loja.getDadosOperacionais().getTotalAvaliacoes()
                ));
    }
}