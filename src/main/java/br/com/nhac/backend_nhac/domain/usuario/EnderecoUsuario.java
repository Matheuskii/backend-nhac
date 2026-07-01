package br.com.nhac.backend_nhac.domain.usuario;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_endereco_usuarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class EnderecoUsuario {

    @Id
    @Column(updatable = false, length = 50, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String rua;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(nullable = false, length = 9)
    private String cep;

    @Column(length = 150)
    private String complemento;

    @Column(name = "is_padrao", nullable = false)
    private boolean isPadrao = false;

}
