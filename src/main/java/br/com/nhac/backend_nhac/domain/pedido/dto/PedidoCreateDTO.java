package br.com.nhac.backend_nhac.domain.pedido.dto;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.EnderecoEntrega;
import br.com.nhac.backend_nhac.domain.pedido.ItemPedido;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Objeto de transferência que representa o Carrinho de Compras finalizado pelo cliente no Flutter")
public record PedidoCreateDTO(



        @Schema(description = "ID da loja onde o pedido foi feito", example = "loja-001")
        @NotBlank(message = "O ID da loja é obrigatório.")
        String lojaId,


        @Schema(description = "Forma de pagamento escolhida (ex: PIX, CARTAO_CREDITO)", example = "PIX")
        @NotBlank(message = "A forma de pagamento é obrigatória.")
        String formaPagamento,

        @Schema(description = "Observação ou instrução especial do cliente", example = "Tirar a cebola do lanche.")
        @Size(max = 500, message = "A observação não pode ter mais de 500 caracteres.")
        String observacao,

        @Schema(description = "Valor em dinheiro para calcular o troco (apenas quando formaPagamento = Dinheiro)", example = "50.00")
        BigDecimal trocoPara,

        @Schema(description = "Endereço completo e exato para entrega")
        @NotNull(message = "O endereço de entrega é obrigatório.")
        @Valid
        EnderecoEntregaDTO enderecoEntrega,

        @Schema(description = "Lista de produtos comprados")
        @NotEmpty(message = "O pedido deve conter pelo menos um item. O carrinho não pode estar vazio.")
        @Valid
        List<ItemPedidoDTO> itens
) {

        public Pedido toEntity(Loja lojaDaBaseDeDados) {
                Pedido pedido = new Pedido();

                pedido.setId(UUID.randomUUID().toString());
                pedido.setLoja(lojaDaBaseDeDados);

                pedido.setFormaPagamento(this.formaPagamento());
                pedido.setObservacao(this.observacao());
                pedido.setTrocoPara(this.trocoPara());

                pedido.setStatus(StatusPedido.PENDENTE);
                pedido.setCriadoEm(Instant.now());

                EnderecoEntrega endereco = new EnderecoEntrega(
                        this.enderecoEntrega().rua(),
                        this.enderecoEntrega().numero(),
                        this.enderecoEntrega().bairro(),
                        this.enderecoEntrega().cidade(),
                        this.enderecoEntrega().estado(),
                        this.enderecoEntrega().cep(),
                        this.enderecoEntrega().complemento()
                );
                pedido.setEnderecoEntrega(endereco);

                return pedido;
        }


        @Schema(description = "Dados do endereço de entrega no momento da compra")
        public record EnderecoEntregaDTO(
                @NotBlank(message = "A rua de entrega é obrigatória.")
                String rua,

                @NotBlank(message = "O número de entrega é obrigatório.")
                String numero,

                @NotBlank(message = "O bairro é obrigatório.")
                String bairro,

                @NotBlank(message = "A cidade é obrigatória.")
                String cidade,

                @NotBlank(message = "O estado é obrigatório.")
                @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 caracteres (ex: SP).")
                String estado,

                @NotBlank(message = "O CEP é obrigatório.")
                @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato XXXXX-XXX.")
                String cep,

                String complemento
        ) {}


        @Schema(description = "Item individual que compõe o pedido (Snapshot para histórico)")
        public record ItemPedidoDTO(

                @NotBlank(message = "O ID do produto é obrigatório.")
                String produtoId,

                @NotBlank(message = "O nome do produto é obrigatório no item.")
                String nome,

                String imagemUrl,


                @NotNull(message = "A quantidade é obrigatória.")
                @Positive(message = "A quantidade deve ser maior que zero.")
                Integer quantidade
        ) {

                public ItemPedido toEntity(Produto produtoReferencia) {
                        ItemPedido item = new ItemPedido();

                        item.setId(UUID.randomUUID().toString());
                        item.setProduto(produtoReferencia);

                        item.setNome(this.nome());
                        item.setImagemUrl(this.imagemUrl());
                        item.setQuantidade(this.quantidade());

                        return item;
                }
        }
}