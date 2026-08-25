package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.avaliacao.Avaliacao;
import br.com.nhac.backend_nhac.domain.avaliacao.dto.AvaliacaoCreateDTO;
import br.com.nhac.backend_nhac.domain.avaliacao.dto.AvaliacaoResumoDTO;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import br.com.nhac.backend_nhac.domain.pedido.StatusPedido;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.repositories.AvaliacaoRepository;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.PedidoRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final PedidoRepository pedidoRepository;
    private final LojaRepository lojaRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, PedidoRepository pedidoRepository, LojaRepository lojaRepository, UsuarioRepository usuarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.pedidoRepository = pedidoRepository;
        this.lojaRepository = lojaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AvaliacaoResumoDTO criarAvaliacao(String usuarioId, AvaliacaoCreateDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IdNaoEncontradoException("Usuário não encontrado."));

        Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                .orElseThrow(() -> new IdNaoEncontradoException("O pedido com o id: " + dto.pedidoId() + " não foi encontrado."));

        if (!pedido.getUsuarioId().equals(usuarioId)) {
            throw new RegraDeNegocioException("Você só pode avaliar pedidos que pertencem a você.");
        }

        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw new RegraDeNegocioException("Apenas pedidos entregues podem ser avaliados.");
        }

        if (avaliacaoRepository.existsByPedidoId(pedido.getId())) {
            throw new RegraDeNegocioException("Este pedido já foi avaliado.");
        }

        Loja loja = pedido.getLoja();

        Avaliacao avaliacao = new Avaliacao(dto.nota(), dto.comentario(), usuario, loja, pedido);
        avaliacaoRepository.save(avaliacao);

        recalcularMediaLoja(loja, dto.nota());

        return new AvaliacaoResumoDTO(avaliacao);
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoResumoDTO> listarAvaliacoesPorLoja(String lojaId, Pageable pageable) {
        if (!lojaRepository.existsById(lojaId)) {
            throw new IdNaoEncontradoException("A loja com o id: " + lojaId + " não foi encontrada.");
        }
        return avaliacaoRepository.findByLojaId(lojaId, pageable).map(AvaliacaoResumoDTO::new);
    }

    private void recalcularMediaLoja(Loja loja, Integer novaNota) {
        br.com.nhac.backend_nhac.domain.loja.DadosOperacionais dados = loja.getDadosOperacionais();
        int totalAvaliacoes = dados.getTotalAvaliacoes();
        float mediaAtual = dados.getAvaliacaoMedia();

        float novaMedia = ((mediaAtual * totalAvaliacoes) + novaNota) / (totalAvaliacoes + 1);

        dados.setTotalAvaliacoes(totalAvaliacoes + 1);
        dados.setAvaliacaoMedia(Math.round(novaMedia * 10.0f) / 10.0f); 

        lojaRepository.save(loja);
    }
}
