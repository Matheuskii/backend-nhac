package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoLojistaDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.ErroPadraoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lojista")
@Tag(name = "Painel do Lojista", description = "Endpoints do painel para o dono da loja consultar produtos e pedidos recebidos")
public class LojistaController {

    private final LojistaService lojistaService;

    public LojistaController(LojistaService lojistaService) {
        this.lojistaService = lojistaService;
    }

    @Operation(summary = "Listar produtos da loja", description = "Lista os produtos das lojas do usuário autenticado, incluindo inativos. A loja é resolvida pelo token, sem lojaId na URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista paginada de produtos. Vazia se o usuário ainda não tiver loja."),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @GetMapping("/produtos")
    public ResponseEntity<Page<ProdutoLojistaDTO>> listarProdutos(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestParam(required = false) String categoriaMenu,
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(lojistaService.listarProdutos(usuarioLogado.getId(), categoriaMenu, nome, pageable));
    }

    @Operation(summary = "Listar pedidos recebidos", description = "Lista os pedidos recebidos pelas lojas do usuário autenticado. Sem status, retorna todos ordenados do mais recente para o mais antigo. O parâmetro status deve ser o valor do enum StatusPedido em maiúsculas (PENDENTE, PAGO, PREPARANDO, SAIU_ENTREGA, ENTREGUE, CANCELADO). Valores inválidos retornam 400.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista paginada de pedidos. Vazia se ainda não houver pedidos ou se o usuário não tiver loja."),
            @ApiResponse(responseCode = "400", description = "Valor de status inválido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @GetMapping("/pedidos")
    public ResponseEntity<Page<PedidoResumoDTO>> listarPedidos(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(lojistaService.listarPedidos(usuarioLogado.getId(), status, pageable));
    }
}
