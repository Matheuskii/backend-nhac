package br.com.nhac.backend_nhac.domain.produto.dto;

import br.com.nhac.backend_nhac.domain.produto.Produto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Objeto simplificado devolvido na listagem do cardápio")
public record ProdutoResumoDTO(

        @Schema(description = "ID do produto", example = "prod_123")
        String id,

        @Schema(description = "ID da Loja", example = "loja-123")
        String lojaId,

        @Schema(description = "Nome da loja. Sempre presente, independente da loja estar aberta ou fechada no momento — usado para exibir 'Vendido por' na tela do produto sem depender de GET /lojas/{id}, que só retorna lojas abertas.", example = "Sushi Ken")
        String lojaNome,

        @Schema(description = "Nome do produto", example = "Hossomaki de Salmão")
        String nome,

        @Schema(description = "Descrição detalhada", example = "Rolinho de arroz e alga com salmão.")
        String descricao,

        @Schema(description = "Preço atual", example = "25.50")
        BigDecimal preco,

        @Schema(description = "Categoria para as abas do Flutter", example = "Sushi")
        String categoriaMenu,

        @Schema(description = "URL da imagem", example = """
                https://firebasestorage...""")
        String imagemUrl,

        @Schema(description = "peso do produto em g ou em kg", example = "23g")
        String peso,

        @Schema(description = "Percentual de desconto", example = "0")
        Integer percentualDesconto
) {
    public ProdutoResumoDTO(Produto produto) {
        this(
                produto.getId(),
                produto.getLoja().getId(),
                produto.getLoja().getNome(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoriaMenu(),
                produto.getImagemUrl(),
                produto.getPeso(),
                produto.getPercentualDesconto()
        );
    }
}