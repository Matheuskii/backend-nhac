package br.com.nhac.backend_nhac.domain.favorito;

import br.com.nhac.backend_nhac.domain.favorito.dto.FavoritoCreateDTO;
import br.com.nhac.backend_nhac.domain.favorito.dto.FavoritoResponseDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.services.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favoritos")
@Tag(name = "Favoritos", description = "Gerenciamento de lojas favoritas do usuário")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @Operation(summary = "Favoritar uma loja")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loja favoritada com sucesso.")
    })
    @PostMapping
    public ResponseEntity<FavoritoResponseDTO> favoritar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestBody @Valid FavoritoCreateDTO dto) {
        
        FavoritoResponseDTO response = favoritoService.favoritar(usuarioLogado.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar lojas favoritas do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
    })
    @GetMapping
    public ResponseEntity<Page<FavoritoResponseDTO>> listarFavoritos(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PageableDefault(sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FavoritoResponseDTO> page = favoritoService.listarFavoritos(usuarioLogado.getId(), pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Remover loja dos favoritos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Favorito removido com sucesso.")
    })
    @DeleteMapping("/{lojaId}")
    public ResponseEntity<Void> removerFavorito(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PathVariable String lojaId) {
        
        favoritoService.removerFavorito(usuarioLogado.getId(), lojaId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Contar seguidores de uma loja")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem retornada com sucesso.")
    })
    @GetMapping("/lojas/{lojaId}/contagem")
    public ResponseEntity<Long> contarSeguidores(@PathVariable String lojaId) {
        long contagem = favoritoService.contarSeguidores(lojaId);
        return ResponseEntity.ok(contagem);
    }
}
