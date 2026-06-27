package br.com.nhac.backend_nhac.domain.pedido;

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
public class EnderecoEntrega {

    @Column(name = "entrega_rua")
    private String rua;

    @Column(name = "entrega_numero")
    private String numero;

    @Column(name = "entrega_bairro")
    private String bairro;

    @Column(name = "entrega_cidade")
    private String cidade;

    @Column(name = "entrega_estado", length = 2)
    private String estado;

    @Column(name = "entrega_cep", length = 20)
    private String cep;

    @Column(name = "entrega_complemento")
    private String complemento;
}