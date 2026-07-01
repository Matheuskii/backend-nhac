package br.com.nhac.backend_nhac.domain.loja;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoLoja {

    @Schema(description = "Nome da rua/avenida da loja", example = "Rua Felipe Schmidt")
    @Column(name = "end_rua", length = 150)
    private String rua;

    @Schema(description = "Número do estabelecimento", example = "300")
    @Column(name = "end_numero", length = 20)
    private String numero;

    @Schema(description = "Cidade onde a loja está localizada", example = "Florianópolis")
    @Column(name = "end_cidade", length = 100)
    private String cidade;

    @Schema(description = "Estado (UF)", example = "SC")
    @Column(name = "end_estado", length = 2)
    private String estado;

    @Schema(description = "CEP do endereço", example = "88010-400")
    @Column(name = "end_cep", length = 20)
    private String cep;
}