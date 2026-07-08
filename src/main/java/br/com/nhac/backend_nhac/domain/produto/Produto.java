package br.com.nhac.backend_nhac.domain.produto;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tb_produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Produto {

    @Id
    @Column(updatable = false, nullable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "categoria_menu", nullable = false, length = 50)
    private String categoriaMenu;

    @Column(name = "imagem_url", columnDefinition = "TEXT")
    private String imagemUrl;

    @Column(name = "is_ativo", nullable = false)
    private boolean isAtivo = true;

    @Column(name = "criado_em")
    private Instant criadoEm;

    @Column(length = 20)
    private String peso;

    @Column(name = "percentual_desconto")
    private Integer percentualDesconto;

    public Produto(ProdutoCreateDTO dto, Loja loja) {
        this.loja = loja;
        this.nome = dto.nome();
        this.descricao = dto.descricao();
        this.preco = dto.preco();
        this.categoriaMenu = dto.categoriaMenu();
        this.imagemUrl = dto.imagemUrl();
        this.peso = dto.peso();
        this.percentualDesconto = dto.percentualDesconto();
        this.isAtivo = true;
        this.criadoEm = Instant.now();
    }
}