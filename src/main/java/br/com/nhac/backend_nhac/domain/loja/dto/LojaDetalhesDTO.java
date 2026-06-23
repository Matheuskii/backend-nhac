package br.com.nhac.backend_nhac.domain.loja.dto;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Objeto completo com todos os dados e horários de uma loja específica")
public record LojaDetalhesDTO(

        @Schema(description = "Identificador único da loja", example = "loja_japonesa_001")
        String id,

        @Schema(description = "Nome fantasia da loja", example = "Sushi Ken")
        String nome,

        @Schema(description = "Descrição detalhada da loja", example = "O melhor sushi da região, com ingredientes frescos e selecionados.")
        String descricao,

        @Schema(description = "Categoria principal", example = "Japonesa")
        String categoria,

        @Schema(description = "URL do banner da loja no Firebase Storage", example = "https://firebasestorage.../banner.png")
        String imagemUrl,

        @Schema(description = "Métricas detalhadas de logística e avaliação")
        DadosOperacionaisDTO dadosOperacionais,

        @Schema(description = "Endereço físico completo da loja")
        EnderecoDTO endereco,

        @Schema(description = "Grade completa de horários de funcionamento")
        HorariosDTO horarios
) {
    @Schema(description = "Dados operacionais")
    public record DadosOperacionaisDTO(
            @Schema(description = "Média de avaliações", example = "4.8") float avaliacaoMedia,
            @Schema(description = "Valor base de entrega", example = "5.99") BigDecimal taxaEntregaBase,
            @Schema(description = "Tempo mínimo (min)", example = "30") int tempoEntregaMin,
            @Schema(description = "Tempo máximo (min)", example = "45") int tempoEntregaMax,
            @Schema(description = "Total de avaliações", example = "150") int totalAvaliacoes
    ) {}

    @Schema(description = "Endereço físico")
    public record EnderecoDTO(
            @Schema(description = "Rua", example = "Rua das Flores") String rua,
            @Schema(description = "Número", example = "123") String numero,
            @Schema(description = "Cidade", example = "São Paulo") String cidade,
            @Schema(description = "Estado", example = "SP") String estado,
            @Schema(description = "Código Postal", example = "01000-000") String cep
    ) {}

    @Schema(description = "Horários diários (Formato recomendado: HH:MM - HH:MM ou 'Fechado')")
    public record HorariosDTO(
            @Schema(example = "18:00 - 23:00") String domingo,
            @Schema(example = "Fechado") String segunda,
            @Schema(example = "11:00 - 15:00, 18:00 - 23:00") String terca,
            @Schema(example = "11:00 - 15:00, 18:00 - 23:00") String quarta,
            @Schema(example = "11:00 - 15:00, 18:00 - 23:00") String quinta,
            @Schema(example = "11:00 - 23:59") String sexta,
            @Schema(example = "11:00 - 23:59") String sabado
    ) {}

    public LojaDetalhesDTO(Loja loja) {
        this(loja.getId(),
                loja.getNome(),
                loja.getDescricao(),
                loja.getCategoria(),
                loja.getImagemUrl(),
                new LojaDetalhesDTO.DadosOperacionaisDTO(
                        loja.getDadosOperacionais().getAvaliacaoMedia(),
                        loja.getDadosOperacionais().getTaxaEntregaBase(),
                        loja.getDadosOperacionais().getTempoEntregaMin(),
                        loja.getDadosOperacionais().getTempoEntregaMax(),
                        loja.getDadosOperacionais().getTotalAvaliacoes()
                ),
                new LojaDetalhesDTO.EnderecoDTO(
                        loja.getEndereco().getRua(),
                        loja.getEndereco().getNumero(),
                        loja.getEndereco().getCidade(),
                        loja.getEndereco().getEstado(),
                        loja.getEndereco().getCep()
                ),
                new LojaDetalhesDTO.HorariosDTO(
                        loja.getHorariosFuncionamento().getDomingo(),
                        loja.getHorariosFuncionamento().getSegunda(),
                        loja.getHorariosFuncionamento().getTerca(),
                        loja.getHorariosFuncionamento().getQuarta(),
                        loja.getHorariosFuncionamento().getQuinta(),
                        loja.getHorariosFuncionamento().getSexta() ,
                        loja.getHorariosFuncionamento().getSabado()
                ));
    }
}