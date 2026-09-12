package br.com.nhac.backend_nhac.domain.lojista;

import br.com.nhac.backend_nhac.domain.lojista.dto.FuncionarioCreateDTO;
import br.com.nhac.backend_nhac.domain.lojista.dto.FuncionarioDTO;
import br.com.nhac.backend_nhac.domain.lojista.dto.FuncionarioUpdateDTO;
import br.com.nhac.backend_nhac.domain.lojista.dto.FinanceiroResumoDTO;
import br.com.nhac.backend_nhac.domain.lojista.dto.PainelResumoDTO;
import br.com.nhac.backend_nhac.domain.lojista.service.LojaAccessService;
import br.com.nhac.backend_nhac.domain.lojista.service.PainelFinanceiroService;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoDetalheLojistaDTO;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoLojistaDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoLojistaDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/lojista")
@Tag(name = "Painel do Lojista", description = "Endpoints do painel para o dono da loja consultar produtos e pedidos recebidos")
public class LojistaController {

    private final LojistaService lojistaService;
    private final LojaAccessService lojaAccessService;
    private final PainelFinanceiroService painelFinanceiroService;

    public LojistaController(LojistaService lojistaService, LojaAccessService lojaAccessService, PainelFinanceiroService painelFinanceiroService) {
        this.lojistaService = lojistaService;
        this.lojaAccessService = lojaAccessService;
        this.painelFinanceiroService = painelFinanceiroService;
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
        return ResponseEntity.ok(lojistaService.listarProdutos(usuarioLogado, categoriaMenu, nome, pageable));
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
    public ResponseEntity<Page<PedidoResumoLojistaDTO>> listarPedidos(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(lojistaService.listarPedidos(usuarioLogado.getId(), status, pageable));
    }

    @Operation(summary = "Detalhes de um pedido", description = "Retorna os detalhes completos de um pedido específico das lojas do usuário autenticado. ADMIN tem acesso a qualquer pedido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado e retornado com sucesso."),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado: o pedido não pertence às lojas do usuário logado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @GetMapping("/pedidos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOJISTA', 'FUNCIONARIO')")
    public ResponseEntity<PedidoDetalheLojistaDTO> buscarPedidoDetalhe(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        
        PedidoDetalheLojistaDTO response = lojistaService.buscarPedidoDetalhe(id, usuarioLogado);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar funcionários da loja", description = "Lista os funcionários ativos da loja do usuário autenticado (dono ou admin).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista paginada de funcionários."),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado: apenas dono ou admin podem listar funcionários.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @GetMapping("/funcionarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOJISTA')")
    public ResponseEntity<Page<FuncionarioDTO>> listarFuncionarios(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(lojistaService.listarFuncionarios(usuarioLogado, pageable));
    }

    @Operation(summary = "Criar funcionário", description = "Cria um novo funcionário vinculado à loja do usuário autenticado (apenas dono ou admin).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funcionário criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou limite de funcionários atingido.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado: apenas dono pode criar funcionários.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @PostMapping("/funcionarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOJISTA')")
    public ResponseEntity<FuncionarioDTO> criarFuncionario(
            @RequestBody @Valid FuncionarioCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado,
            UriComponentsBuilder uriBuilder) {
        Usuario funcionario = lojistaService.criarFuncionario(dto, usuarioLogado);
        FuncionarioDTO response = new FuncionarioDTO();
        response.setId(funcionario.getId());
        response.setNome(funcionario.getNome());
        response.setEmail(funcionario.getEmail());
        response.setTelefone(funcionario.getTelefone());
        response.setCargo(funcionario.getCargo());
        response.setAtivo(funcionario.isAtivo());
        if (funcionario.getLojaVinculada() != null) {
            response.setLojaId(funcionario.getLojaVinculada().getId());
            response.setLojaNome(funcionario.getLojaVinculada().getNome());
        }
        return ResponseEntity.created(uriBuilder.path("/api/v1/lojista/funcionarios/" + funcionario.getId()).build().toUri()).body(response);
    }

    @Operation(summary = "Atualizar funcionário", description = "Atualiza dados de um funcionário (nome, telefone, cargo). Email e senha não podem ser alterados por este endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado: apenas dono ou admin podem editar funcionários.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @PutMapping("/funcionarios/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOJISTA')")
    public ResponseEntity<FuncionarioDTO> atualizarFuncionario(
            @PathVariable String id,
            @RequestBody @Valid FuncionarioUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        Usuario funcionario = lojistaService.atualizarFuncionario(id, dto, usuarioLogado);
        FuncionarioDTO response = new FuncionarioDTO();
        response.setId(funcionario.getId());
        response.setNome(funcionario.getNome());
        response.setEmail(funcionario.getEmail());
        response.setTelefone(funcionario.getTelefone());
        response.setCargo(funcionario.getCargo());
        response.setAtivo(funcionario.isAtivo());
        if (funcionario.getLojaVinculada() != null) {
            response.setLojaId(funcionario.getLojaVinculada().getId());
            response.setLojaNome(funcionario.getLojaVinculada().getNome());
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ativar/Desativar funcionário", description = "Ativa ou desativa um funcionário (soft delete). Apenas dono ou admin podem executar esta ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do funcionário atualizado com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado: apenas dono ou admin podem gerenciar funcionários.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @PatchMapping("/funcionarios/{id}/ativar")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOJISTA')")
    public ResponseEntity<FuncionarioDTO> ativarOuDesativarFuncionario(
            @PathVariable String id,
            @RequestParam boolean ativo,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        Usuario funcionario = lojistaService.ativarOuDesativarFuncionario(id, ativo, usuarioLogado);
        FuncionarioDTO response = new FuncionarioDTO();
        response.setId(funcionario.getId());
        response.setNome(funcionario.getNome());
        response.setEmail(funcionario.getEmail());
        response.setTelefone(funcionario.getTelefone());
        response.setCargo(funcionario.getCargo());
        response.setAtivo(funcionario.isAtivo());
        if (funcionario.getLojaVinculada() != null) {
            response.setLojaId(funcionario.getLojaVinculada().getId());
            response.setLojaNome(funcionario.getLojaVinculada().getNome());
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir funcionário", description = "Desativa um funcionário (soft delete). Apenas dono ou admin podem executar esta ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário desativado com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado: apenas dono ou admin podem excluir funcionários.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado.",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @DeleteMapping("/funcionarios/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOJISTA')")
    public ResponseEntity<Void> excluirFuncionario(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        lojistaService.ativarOuDesativarFuncionario(id, false, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Resumo do Painel", description = "Retorna dados agregados para o painel do lojista: loja aberta, faturamento hoje, contagem de pedidos por status, faturamento últimos 7 dias e top 5 pedidos recentes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Painel retornado com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @GetMapping("/painel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOJISTA', 'FUNCIONARIO')")
    public ResponseEntity<PainelResumoDTO> obterPainel(@AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(painelFinanceiroService.obterPainelResumo(usuarioLogado));
    }

    @Operation(summary = "Resumo Financeiro", description = "Retorna dados financeiros detalhados para o período especificado (em dias). Apenas pedidos ENTREGUE são considerados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Financeiro retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Período inválido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @GetMapping("/financeiro")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOJISTA', 'FUNCIONARIO')")
    public ResponseEntity<FinanceiroResumoDTO> obterFinanceiro(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestParam(defaultValue = "30") int periodo) {
        if (periodo <= 0 || periodo > 365) {
            throw new br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException("Período deve estar entre 1 e 365 dias.");
        }
        return ResponseEntity.ok(painelFinanceiroService.obterFinanceiroResumo(usuarioLogado, periodo));
    }
}
