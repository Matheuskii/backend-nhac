package br.com.nhac.backend_nhac.domain.usuario;

import br.com.nhac.backend_nhac.domain.auth.dto.LoginResponseDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.EnderecoUsuarioDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioAtualizarDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioResponseDTO;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import br.com.nhac.backend_nhac.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final br.com.nhac.backend_nhac.services.FavoritoService favoritoService;
    private final br.com.nhac.backend_nhac.services.PedidoService pedidoService;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository, TokenService tokenService, br.com.nhac.backend_nhac.services.FavoritoService favoritoService, br.com.nhac.backend_nhac.services.PedidoService pedidoService) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.favoritoService = favoritoService;
        this.pedidoService = pedidoService;
    }

    private void validarPropriedade(String idNaUrl, Usuario usuarioLogado) {
        if (!idNaUrl.equals(usuarioLogado.getId())) {
            throw new AcessoNegadoException("Acesso negado: não tem permissão para aceder ou modificar os dados de outro utilizador.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuario(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(id, usuarioLogado);
        return ResponseEntity.ok(usuarioService.buscarUsuario(id));
    }

    @PostMapping
    public ResponseEntity<Void> criarUsuario(@RequestBody @Valid UsuarioCreateDTO dto) {
        usuarioService.salvarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoginResponseDTO> atualizarDadosUsuario(
            @PathVariable String id,
            @RequestBody @Valid UsuarioAtualizarDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(id, usuarioLogado);
        usuarioService.atualizarUsuarioParcial(id, dados);
        Usuario usuarioAtualizado = usuarioRepository.findById(id).get();
        String novoToken = tokenService.gerarToken(usuarioAtualizado);

        return ResponseEntity.ok(new LoginResponseDTO(novoToken, usuarioAtualizado.getId(), usuarioAtualizado.getNome(), false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarUsuario(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        usuarioService.desativarUsuario(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/seguindo/{lojaId}")
    public ResponseEntity<Void> seguirLoja(
            @PathVariable String id,
            @PathVariable String lojaId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        validarPropriedade(id, usuarioLogado);
        try {
            br.com.nhac.backend_nhac.domain.favorito.dto.FavoritoCreateDTO dto = new br.com.nhac.backend_nhac.domain.favorito.dto.FavoritoCreateDTO(lojaId);
            favoritoService.favoritar(id, dto);
        } catch (br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException e) {
            // Ja segue, ignorar silenciosamente
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/seguindo/{lojaId}")
    public ResponseEntity<Void> pararDeSeguirLoja(
            @PathVariable String id,
            @PathVariable String lojaId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        validarPropriedade(id, usuarioLogado);
        try {
            favoritoService.removerFavorito(id, lojaId);
        } catch (Exception e) {}
        return ResponseEntity.ok().build();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Verificar se usuário segue loja", description = "Verifica se o usuário favoritou/segue uma determinada loja.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuário ou loja não encontrados.")
    })
    @GetMapping("/{id}/seguindo/{lojaId}")
    public ResponseEntity<Boolean> verificarSeguindo(
            @PathVariable String id,
            @PathVariable String lojaId,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        validarPropriedade(id, usuarioLogado);
        boolean segue = favoritoService.usuarioSegueLoja(id, lojaId);
        return ResponseEntity.ok(segue);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Listar pedidos do usuário", description = "Retorna uma lista paginada dos pedidos feitos pelo usuário.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso.")
    })
    @GetMapping("/{id}/pedidos")
    public ResponseEntity<org.springframework.data.domain.Page<br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO>> listarPedidos(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado,
            @org.springframework.data.web.PageableDefault(sort = "criadoEm", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {

        validarPropriedade(id, usuarioLogado);
        org.springframework.data.domain.Page<br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoDTO> page = pedidoService.listarMeusPedidos(id, pageable);
        return ResponseEntity.ok(page);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obter estatísticas do usuário", description = "Retorna os totais de pedidos, lojas favoritas e cupons resgatados do usuário.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso.")
    })
    @GetMapping("/{id}/estatisticas")
    public ResponseEntity<br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioEstatisticasDTO> obterEstatisticas(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        
        validarPropriedade(id, usuarioLogado);
        br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioEstatisticasDTO estatisticas = usuarioService.obterEstatisticas(id);
        return ResponseEntity.ok(estatisticas);
    }
}
