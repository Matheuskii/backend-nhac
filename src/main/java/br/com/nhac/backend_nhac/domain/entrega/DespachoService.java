package br.com.nhac.backend_nhac.domain.entrega;

import br.com.nhac.backend_nhac.domain.entrega.dto.EntregaAtivaResponseDTO;
import br.com.nhac.backend_nhac.domain.entrega.dto.OfertaEntregaDTO;
import br.com.nhac.backend_nhac.domain.entregador.Entregador;
import br.com.nhac.backend_nhac.domain.entregador.EntregadorRepository;
import br.com.nhac.backend_nhac.domain.entregador.EntregadorService;
import br.com.nhac.backend_nhac.domain.entregador.StatusOperacional;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.PedidoRepository;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DespachoService {

    private static final Logger log = LoggerFactory.getLogger(DespachoService.class);

    private final PedidoRepository pedidoRepository;
    private final OfertaEntregaRepository ofertaEntregaRepository;
    private final EntregadorService entregadorService;
    private final EntregadorRepository entregadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DespachoService(
            PedidoRepository pedidoRepository,
            OfertaEntregaRepository ofertaEntregaRepository,
            EntregadorService entregadorService,
            EntregadorRepository entregadorRepository,
            UsuarioRepository usuarioRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.pedidoRepository = pedidoRepository;
        this.ofertaEntregaRepository = ofertaEntregaRepository;
        this.entregadorService = entregadorService;
        this.entregadorRepository = entregadorRepository;
        this.usuarioRepository = usuarioRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public List<OfertaEntregaDTO> despacharPedido(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Pedido " + pedidoId + " não encontrado para despacho."));

        if (pedido.getEntregador() != null) {
            throw new RegraDeNegocioException("Este pedido já possui um entregador vinculado.");
        }

        if (pedido.getLoja() == null || pedido.getLoja().getGeoLocalizacao() == null) {
            throw new RegraDeNegocioException("A loja do pedido não possui coordenadas GPS configuradas.");
        }

        double lojaLat = pedido.getLoja().getGeoLocalizacao().getGeoLat();
        double lojaLng = pedido.getLoja().getGeoLocalizacao().getGeoLng();

        // Busca entregadores online em um raio de até 7 km
        var entregadoresProximos = entregadorService.buscarEntregadoresProximos(lojaLat, lojaLng, 7.0);

        if (entregadoresProximos.isEmpty()) {
            log.warn("Nenhum entregador online disponível próximo à loja {} para o pedido {}.", pedido.getLoja().getNome(), pedidoId);
            return List.of();
        }

        List<OfertaEntregaDTO> ofertasCriadas = new ArrayList<>();
        Instant agora = Instant.now();
        Instant expiraEm = agora.plusSeconds(45); // 45 segundos para resposta

        for (var item : entregadoresProximos) {
            Entregador entregador = item.entregador();

            OfertaEntrega oferta = OfertaEntrega.builder()
                    .id(UUID.randomUUID().toString())
                    .pedido(pedido)
                    .entregador(entregador)
                    .status(StatusOferta.PENDENTE)
                    .criadoEm(agora)
                    .expiraEm(expiraEm)
                    .build();

            ofertaEntregaRepository.save(oferta);
            OfertaEntregaDTO dto = new OfertaEntregaDTO(oferta);
            ofertasCriadas.add(dto);

            // Dispara notificação WebSocket em tempo real para o canal específico do motoboy
            try {
                messagingTemplate.convertAndSend("/topic/entregador/" + entregador.getId() + "/ofertas", dto);
            } catch (Exception e) {
                log.error("Erro ao enviar WebSocket de oferta para o entregador {}: {}", entregador.getId(), e.getMessage());
            }
        }

        return ofertasCriadas;
    }

    @Transactional
    public EntregaAtivaResponseDTO aceitarOferta(String ofertaId, Usuario usuarioLogado) {
        Entregador entregador = entregadorService.buscarPorUsuario(usuarioLogado);

        OfertaEntrega oferta = ofertaEntregaRepository.findByIdAndEntregadorId(ofertaId, entregador.getId())
                .orElseThrow(() -> new IdNaoEncontradoException("Oferta não encontrada para este entregador."));

        if (oferta.getStatus() != StatusOferta.PENDENTE) {
            throw new RegraDeNegocioException("Esta oferta já foi respondida ou processada anteriormente.");
        }

        if (oferta.isExpirada()) {
            oferta.setStatus(StatusOferta.EXPIRADA);
            ofertaEntregaRepository.save(oferta);
            throw new RegraDeNegocioException("Esta oferta expirou.");
        }

        Pedido pedido = oferta.getPedido();
        if (pedido.getEntregador() != null) {
            oferta.setStatus(StatusOferta.EXPIRADA);
            ofertaEntregaRepository.save(oferta);
            throw new RegraDeNegocioException("Outro entregador já aceitou esta corrida antes de você.");
        }

        // Vincula entregador ao pedido e altera status
        pedido.setEntregador(entregador);
        if (pedido.getStatus() == StatusPedido.PREPARANDO) {
            pedido.alterarStatus(StatusPedido.SAIU_ENTREGA);
        }
        pedidoRepository.save(pedido);

        // Marca oferta atual como ACEITA
        oferta.setStatus(StatusOferta.ACEITA);
        ofertaEntregaRepository.save(oferta);

        // Atualiza status operacional do entregador para EM_ENTREGA
        entregador.setStatusOperacional(StatusOperacional.EM_ENTREGA);
        entregadorRepository.save(entregador);

        // Expira as demais ofertas pendentes concorrentes deste pedido
        List<OfertaEntrega> concorrentes = ofertaEntregaRepository.findByPedidoIdAndStatus(pedido.getId(), StatusOferta.PENDENTE);
        for (OfertaEntrega conc : concorrentes) {
            if (!conc.getId().equals(oferta.getId())) {
                conc.setStatus(StatusOferta.EXPIRADA);
                ofertaEntregaRepository.save(conc);
            }
        }

        // Notifica via WebSocket que o pedido foi assumido
        try {
            messagingTemplate.convertAndSend("/topic/pedidos/" + pedido.getId() + "/status", pedido.getStatus().name());
        } catch (Exception e) {
            log.warn("Falha ao emitir WebSocket de atualização do pedido {}: {}", pedido.getId(), e.getMessage());
        }

        Usuario cliente = usuarioRepository.findById(pedido.getUsuarioId()).orElse(null);
        String clienteNome = cliente != null ? cliente.getNome() : "Cliente";
        String clienteTelefone = cliente != null ? cliente.getTelefone() : null;

        return new EntregaAtivaResponseDTO(pedido, clienteNome, clienteTelefone);
    }

    @Transactional
    public void recusarOferta(String ofertaId, Usuario usuarioLogado) {
        Entregador entregador = entregadorService.buscarPorUsuario(usuarioLogado);

        OfertaEntrega oferta = ofertaEntregaRepository.findByIdAndEntregadorId(ofertaId, entregador.getId())
                .orElseThrow(() -> new IdNaoEncontradoException("Oferta não encontrada."));

        if (oferta.getStatus() == StatusOferta.PENDENTE) {
            oferta.setStatus(StatusOferta.RECUSADA);
            ofertaEntregaRepository.save(oferta);
        }
    }

    @Transactional(readOnly = true)
    public List<OfertaEntregaDTO> listarOfertasPendentes(Usuario usuarioLogado) {
        Entregador entregador = entregadorService.buscarPorUsuario(usuarioLogado);
        List<OfertaEntrega> pendentes = ofertaEntregaRepository.findByEntregadorIdAndStatus(entregador.getId(), StatusOferta.PENDENTE);

        return pendentes.stream()
                .filter(o -> !o.isExpirada())
                .map(OfertaEntregaDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public EntregaAtivaResponseDTO obterEntregaAtiva(Usuario usuarioLogado) {
        Entregador entregador = entregadorService.buscarPorUsuario(usuarioLogado);

        Pedido pedidoAtivo = pedidoRepository.findFirstByEntregadorIdAndStatusIn(
                entregador.getId(),
                List.of(StatusPedido.PREPARANDO, StatusPedido.SAIU_ENTREGA)
        ).orElseThrow(() -> new IdNaoEncontradoException("Você não possui nenhuma entrega ativa no momento."));

        Usuario cliente = usuarioRepository.findById(pedidoAtivo.getUsuarioId()).orElse(null);
        String clienteNome = cliente != null ? cliente.getNome() : "Cliente";
        String clienteTelefone = cliente != null ? cliente.getTelefone() : null;

        return new EntregaAtivaResponseDTO(pedidoAtivo, clienteNome, clienteTelefone);
    }
}
