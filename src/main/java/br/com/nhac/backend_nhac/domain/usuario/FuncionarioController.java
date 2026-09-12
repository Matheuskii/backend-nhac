package br.com.nhac.backend_nhac.domain.usuario;

import br.com.nhac.backend_nhac.domain.usuario.dto.FuncionarioCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.FuncionarioResponseDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.FuncionarioUpdateDTO;
import br.com.nhac.backend_nhac.exceptions.ErroPadraoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lojista/funcionarios")
@Tag(name = "Funcionários", description = "CRUD de funcionários da loja. Funcionário é um Usuario com login próprio (papel=FUNCIONARIO) vinculado à loja do dono. Criar/editar/desativar é restrito ao dono da loja.")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @Operation(summary = "Listar funcionários", description = "Lista os funcionários da loja do usuário autenticado (dono ou funcionário podem ver a lista).")
    @GetMapping
    public ResponseEntity<Page<FuncionarioResponseDTO>> listar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(funcionarioService.listar(usuarioLogado, pageable));
    }

    @Operation(summary = "Cadastrar funcionário", description = "Cria a conta do funcionário com a senha definida pelo lojista. Apenas o dono da loja pode cadastrar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funcionário criado"),
            @ApiResponse(responseCode = "403", description = "Apenas o dono da loja pode cadastrar funcionários",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "400", description = "E-mail já em uso ou dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @PostMapping
    public ResponseEntity<FuncionarioResponseDTO> criar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestBody @Valid FuncionarioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioService.criar(dto, usuarioLogado));
    }

    @Operation(summary = "Editar funcionário", description = "Atualiza nome, telefone, cargo e foto. Não altera e-mail nem senha por esta rota.")
    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDTO> atualizar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PathVariable String id,
            @RequestBody @Valid FuncionarioUpdateDTO dto) {
        return ResponseEntity.ok(funcionarioService.atualizar(id, dto, usuarioLogado));
    }

    @Operation(summary = "Desativar funcionário", description = "Soft delete: marca a conta como inativa, o que já bloqueia login automaticamente.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PathVariable String id) {
        funcionarioService.desativar(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar funcionário", description = "Reverte a desativação, restaurando o login.")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<FuncionarioResponseDTO> reativar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PathVariable String id) {
        return ResponseEntity.ok(funcionarioService.reativar(id, usuarioLogado));
    }
}
