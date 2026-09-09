package br.com.nhac.backend_nhac.domain.produto.dto;

import br.com.nhac.backend_nhac.domain.produto.Produto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Produto na listagem do painel do lojista, incluindo inativos e estoque")
public record ProdutoLojistaDTO(
        @Schema(description = "ID do produto", example = "prod_0007")
        String id,

        @Schema(description = "Nome do produto", example = "Hossomaki de Salmão")
        String nome,

        @Schema(description = "Descrição detalhada")
        String descricao,

        @Schema(description = "Preço atual", example = "25.50")
        BigDecimal preco,

        @Schema(description = "Categoria do cardápio", example = "Sushi")
        String categoriaMenu,

        @Schema(description = "URL da imagem")
        String imagemUrl,

        @Schema(description = "Peso do produto", example = "200g")
        String peso,

        @Schema(description = "Percentual de desconto", example = "10")
        Integer percentualDesconto,

        @Schema(description = "Indica se o produto está ativo no cardápio público", example = "true")
        boolean ativo,

        @Schema(description = "Quantidade em estoque", example = "100")
        Integer estoque
) {
    public ProdutoLojistaDTO(Produto produto) {
        this(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoriaMenu(),
                produto.getImagemUrl(),
                produto.getPeso(),
                produto.getPercentualDesconto(),
                produto.isAtivo(),
                produto.getEstoque()
        );
    }
}
