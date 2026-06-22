package br.com.nhac.backend_nhac.domain.loja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Objeto de transferência para criação ou atualização de uma loja")
public record LojaCreateDTO(

        @NotBlank(message = "O nome da loja é obrigatório.")
        @Size(min = 3, max = 100, message = "O nome da loja deve ter entre 3 e 100 caracteres.")
        @Schema(description = "Nome fantasia da loja", example = "Mercado Central")
        String nome,

        @Size(max = 2000, message = "A descrição não pode passar de 2000 caracteres.")
        @Schema(description = "Descrição detalhada da loja", example = "Filial focada em produtos orgânicos.")
        String descricao,

        @Size(max = 50)
        @Schema(description = "Categoria que a loja se encaixa", example = "Restaurantes")
        String categoria,

        @NotBlank(message = "A url do banner da loja é obrigatória.")
        @Size(max = 500, message = "A url do banner da loja não pode passar de 500 caracteres.")
        @Schema(description = "URL do banner da loja", example = "https://amazonaws.com/photo-1579202673506-ca3ce28943ef.jpg")
        String imagemUrl,

        @NotNull(message = "O status de abertura da loja deve ser informado.")
        @Schema(description = "Indica se a loja está aberta ou fechada no momento", example = "true")
        Boolean isAberto,

        @NotNull(message = "Os dados operacionais são obrigatórios.")
        @Valid
        DadosOperacionaisDTO dadosOperacionais,

        @NotNull(message = "O endereço é obrigatório.")
        @Valid
        EnderecoDTO endereco,

        @NotNull(message = "Os horários de funcionamento são obrigatórios.")
        @Valid
        HorariosDTO horarios
) {
    @Schema(description = "Dados de operação logística da loja")
    public record DadosOperacionaisDTO(
            @NotNull(message = "A taxa de entrega base é obrigatória.")
            @Schema(description = "Valor base cobrado pela entrega", example = "5.99")
            BigDecimal taxaEntregaBase,

            @Schema(description = "Tempo mínimo estimado para entrega em minutos", example = "30")
            int tempoEntregaMin,

            @Schema(description = "Tempo máximo estimado para entrega em minutos", example = "45")
            int tempoEntregaMax
    ) {
    }

    @Schema(description = "Endereço físico da loja")
    public record EnderecoDTO(
            @NotBlank(message = "A rua é obrigatória.")
            @Schema(description = "Nome da rua ou avenida", example = "Avenida Paulista")
            String rua,

            @NotBlank(message = "O número é obrigatório.")
            @Schema(description = "Número do estabelecimento", example = "1578")
            String numero,

            @NotBlank(message = "A cidade é obrigatória.")
            @Schema(description = "Cidade onde a loja está localizada", example = "São Paulo")
            String cidade,

            @NotBlank(message = "O estado é obrigatório.")
            @Schema(description = "Sigla do estado", example = "SP")
            String estado,

            @NotBlank(message = "O CEP é obrigatório.")
            @Schema(description = "Código postal no formato XXXXX-XXX", example = "01310-200")
            String cep
    ) {
    }

    @Schema(description = "Horários de funcionamento diários. Usar 'Fechado' para dias sem expediente.")
    public record HorariosDTO(
            @Schema(description = "Horário de Domingo", example = "18:00 - 23:00") String domingo,
            @Schema(description = "Horário de Segunda-feira", example = "Fechado") String segunda,
            @Schema(description = "Horário de Terça-feira", example = "11:00 - 15:00, 18:00 - 23:00") String terca,
            @Schema(description = "Horário de Quarta-feira", example = "11:00 - 15:00, 18:00 - 23:00") String quarta,
            @Schema(description = "Horário de Quinta-feira", example = "11:00 - 15:00, 18:00 - 23:00") String quinta,
            @Schema(description = "Horário de Sexta-feira", example = "11:00 - 23:59") String sexta,
            @Schema(description = "Horário de Sábado", example = "11:00 - 23:59") String sabado
    ) {
    }
}