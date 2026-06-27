package br.com.nhac.backend_nhac.domain.produto;

import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoResumoDTO;
import br.com.nhac.backend_nhac.exceptions.ErroPadraoDTO;
import br.com.nhac.backend_nhac.services.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/produtos")
@Tag(name = "Produtos", description = "Endpoints para gerenciamento do cardápio das lojas")
public class ProdutoController {

    private final ProdutoService produtoService;

    @Autowired
    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Operation(summary = "Cadastrar novo produto", description = "Cria um novo item no cardápio vinculado a uma loja existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso no MariaDB."),

            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados (ex: preço negativo, nome vazio).",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),

            @ApiResponse(responseCode = "404", description = "A loja especificada no lojaId não foi encontrada.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),

            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @PostMapping
    public ResponseEntity<String> cadastrarProduto(@Valid @RequestBody ProdutoCreateDTO dto) {

        produtoService.cadastrarProduto(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body("O produto chegou no Controller com sucesso!");
    }

    @Operation(summary = "Listar produtos de uma loja", description = "Traz o cardápio completo de uma loja filtrado por ID, de forma paginada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem de produtos retornada com sucesso."),

            @ApiResponse(responseCode = "404", description = "A loja especificada pelo ID não existe.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),

            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ProdutoResumoDTO>> listarPorLoja(
            @RequestParam String lojaId,
            @PageableDefault(size = 10, sort = {"categoriaMenu", "nome"}) Pageable pageable) {

        Page<ProdutoResumoDTO> cardapio = produtoService.listarProdutoPorLoja(lojaId, pageable);

        return ResponseEntity.ok(cardapio);
    }
}