package br.com.nhac.backend_nhac.domain.loja;

import br.com.nhac.backend_nhac.domain.loja.dto.LojaDetalhesDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.exceptions.ErroPadraoDTO;
import br.com.nhac.backend_nhac.services.LojaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lojas")
@Tag(name = "Lojas", description = "Endpoints para listagem e consulta do catálogo de restaurantes") // Título no Swagger
public class LojaController {

    private final LojaService lojaService;

    public LojaController(LojaService lojaService) {
        this.lojaService = lojaService;
    }

    @Operation(summary = "Listar lojas abertas", description = "Devolve uma lista paginada de todas as lojas que estão atualmente abertas, utilizando um DTO resumido para otimização de rede.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lojas encontradas e paginadas com sucesso")
    })
    @GetMapping
    public ResponseEntity<Page<LojaResumoDTO>> listarLojas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(lojaService.obterLojasPaginadas(page, size));
    }

    @Operation(summary = "Detalhes de uma loja", description = "Busca todos os dados aninhados de uma loja específica através do seu ID, incluindo horários e endereço completo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loja encontrada com sucesso"),

            @ApiResponse(responseCode = "404", description = "Loja não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<LojaDetalhesDTO> obterLojaPorId(@PathVariable String id) {
        return ResponseEntity.ok(lojaService.obterLojaId(id));
    }
}