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
public class Loja {

    @Id
    @Column(updatable = false, nullable = false, length = 50)
    private String id;
    private String nome;


    @Column(columnDefinition = "TEXT")
    private String descricao;


    private String categoria;


    @Column(columnDefinition = "TEXT")
    private  String imagemUrl;

    @Column(name = "is_aberto", nullable = false)
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
