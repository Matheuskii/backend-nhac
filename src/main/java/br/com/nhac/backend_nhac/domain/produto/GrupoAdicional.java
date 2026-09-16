package br.com.nhac.backend_nhac.domain.produto;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Grupo de adicionais que pode ser aplicado a um produto.
 * Exemplo: "Escolha o molho", "Adicionais de carne".
 */
@Entity
@Table(name = "tb_grupo_adicional")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class GrupoAdicional {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private boolean obrigatorio = false;

    @Column(name = "minimo")
    private Integer minimo;

    @Column(name = "maximo")
    private Integer maximo;

    @OneToMany(mappedBy = "grupoAdicional", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemAdicional> itens = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
