package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoResumoLojistaDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Usado por LojistaController (listagem), PainelService e FinanceiroService
 * (pedidos recentes) — todos precisam do mesmo mapeamento Pedido -> resumo
 * com nome do cliente, buscando os nomes em lote em vez de 1 SELECT por pedido.
 */
@Component
public class PedidoResumoLojistaMapper {

    private final UsuarioRepository usuarioRepository;

    public PedidoResumoLojistaMapper(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<PedidoResumoLojistaDTO> mapear(List<Pedido> pedidos) {
        List<String> usuarioIds = pedidos.stream().map(Pedido::getUsuarioId).distinct().toList();
        Map<String, String> nomesPorUsuarioId = usuarioRepository.findAllById(usuarioIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNome, (a, b) -> a));

        return pedidos.stream()
                .map(pedido -> new PedidoResumoLojistaDTO(pedido, nomesPorUsuarioId.getOrDefault(pedido.getUsuarioId(), "Cliente")))
                .toList();
    }
}
