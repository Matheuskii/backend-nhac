package br.com.nhac.backend_nhac.domain.pedido;


import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.ErroPadraoDTO;
import br.com.nhac.backend_nhac.services.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para o processamento de compras e gestão do carrinho")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }


    @Operation(summary = "Finalizar uma nova compra", description = "Recebe o carrinho de compras do Flutter, valida os itens, amarra à loja e gera um novo pedido no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso. Devolve o ID do pedido gerado."),

            @ApiResponse(responseCode = "400", description = "Erro de validação no DTO (ex: carrinho vazio, valores negativos, formato de CEP inválido).",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),

            @ApiResponse(responseCode = "404", description = "Loja não encontrada ou encontra-se fechada para novos pedidos.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),

            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @PostMapping
    public ResponseEntity<String> criarPedido(
            @RequestBody @Valid PedidoCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        String pedidoId = pedidoService.finalizarPedido(dto, usuarioLogado.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoId);
    }

}
