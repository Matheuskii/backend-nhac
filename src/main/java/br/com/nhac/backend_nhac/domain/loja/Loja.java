package br.com.nhac.backend_nhac.domain.loja;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;


@Entity
@Table(name = "tb_lojas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Representa uma loja dentro do sistema")
public class Loja {

    @Id
    @Column(updatable = false, nullable = false, length = 50)
    @Schema(
            description = "Identificador único da loja",
            example = "loja_japonesa_sushi_ken_002"
    )
    private String id;

    @NotBlank(message = "O nome da loja é obrigatório.")
    @Size(min = 3, max = 100,  message = "O nome da loja deve ter entre 3 e 100 caracteres.")
    @Schema(description = "Nome fantasia da loja", example = "Mercado Central")
    private String nome;


    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "A descrição não pode passar de 2000 caracteres.")
    @Schema(description = "Descrição detalhada da loja", example = "Filial focada em produtos orgânicos.")
    private String descricao;


    @Size(max = 50)
    @Schema(description = "Categoria que a loja se encaixa", example = "Restaurantes")
    private String categoria;


    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "A url do banner da loja")
    @NotBlank
    @Schema(description = "URL do banner da loja", example = " https://amazonaws.com/photo-1579202673506-ca3ce28943ef.jpg")
    private  String imagemUrl;

    @NotNull(message = "O status de abertura da loja deve ser informado.")
    @Column(name = "is_aberto", nullable = false)
    @Schema(description = "Indica se a loja está aberta ou fechada no momento", example = "true")
    private boolean isAberto;

    @Embedded
    private DadosOperacionais dadosOperacionais;

    @Embedded
    private Endereco endereco;

    @Embedded
    private GeoLocalizacao geoLocalizacao;

    @Embedded
    private HorariosFuncionamento horariosFuncionamento;

}
