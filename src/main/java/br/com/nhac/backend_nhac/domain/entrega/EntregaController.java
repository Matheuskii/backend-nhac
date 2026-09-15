package br.com.nhac.backend_nhac.domain.entrega;

import br.com.nhac.backend_nhac.domain.entrega.dto.EntregaAtivaResponseDTO;
import br.com.nhac.backend_nhac.domain.entrega.dto.OfertaEntregaDTO;
import br.com.nhac.backend_nhac.domain.entrega.dto.RotaEntregaResponseDTO;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.PedidoRepository;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entregas")
@Tag(name = "Entregas e Despacho", description = "Endpoints de despacho, ofertas para motoboys e rotas no mapa")
public class EntregaController {

    private final DespachoService despachoService;
    private final RotaService rotaService;
    private final PedidoRepository pedidoRepository;

    public EntregaController(
            DespachoService despachoService,
            RotaService rotaService,
            PedidoRepository pedidoRepository
    ) {
        this.despachoService = despachoService;
        this.rotaService = rotaService;
        this.pedidoRepository = pedidoRepository;
    }

    @PostMapping("/despachar/{pedidoId}")
    @Operation(summary = "Aciona o despacho automático do pedido para entregadores próximos")
    public ResponseEntity<List<OfertaEntregaDTO>> despacharPedido(@PathVariable String pedidoId) {
        List<OfertaEntregaDTO> ofertas = despachoService.despacharPedido(pedidoId);
        return ResponseEntity.ok(ofertas);
    }

    @GetMapping("/ofertas/pendentes")
    @Operation(summary = "Lista ofertas de entrega pendentes para o entregador logado")
    public ResponseEntity<List<OfertaEntregaDTO>> listarOfertasPendentes(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return ResponseEntity.ok(despachoService.listarOfertasPendentes(usuarioLogado));
    }

    @PostMapping("/ofertas/{id}/aceitar")
    @Operation(summary = "Aceita uma oferta de entrega pendente")
    public ResponseEntity<EntregaAtivaResponseDTO> aceitarOferta(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return ResponseEntity.ok(despachoService.aceitarOferta(id, usuarioLogado));
    }

    @PostMapping("/ofertas/{id}/recusar")
    @Operation(summary = "Recusa uma oferta de entrega")
    public ResponseEntity<Void> recusarOferta(
            @PathVariable String id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        despachoService.recusarOferta(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ativa")
    @Operation(summary = "Retorna a entrega em andamento associada ao entregador logado")
    public ResponseEntity<EntregaAtivaResponseDTO> obterEntregaAtiva(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return ResponseEntity.ok(despachoService.obterEntregaAtiva(usuarioLogado));
    }

    @GetMapping("/{pedidoId}/rota")
    @Operation(summary = "Calcula e retorna a rota com Polyline no mapa entre a loja e o endereço de entrega")
    public ResponseEntity<RotaEntregaResponseDTO> obterRota(@PathVariable String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido " + pedidoId + " não encontrado."));
        return ResponseEntity.ok(rotaService.calcularRota(pedido));
    }
}
