package br.com.nhac.backend_nhac.domain.chat;

import br.com.nhac.backend_nhac.domain.chat.dto.ChatDTOs.ConversaResumoDTO;
import br.com.nhac.backend_nhac.domain.chat.dto.ChatDTOs.MensagemDTO;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.LojaAccessService;
import br.com.nhac.backend_nhac.domain.loja.LojaRepository;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.LojaNaoEncontradaException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ponto único de regra de negócio do chat (item 7 da spec, arquitetura
 * decidida como WebSocket em vez de polling). Nunca desenhado antes — este é
 * o primeiro corte, direto pra implementação, conforme combinado.
 *
 * Uma Conversa é sempre entre UM cliente e UMA loja (não por pedido — é um
 * canal contínuo, como a maioria dos apps de delivery faz). Do lado da loja,
 * "quem enviou" pode ser o dono ou qualquer funcionário — pro cliente, toda
 * mensagem do lado loja aparece igual (remetenteTipo=LOJA), só guardamos
 * remetenteUsuarioId internamente pra rastreabilidade.
 */
@Service
public class ChatService {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final LojaRepository lojaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LojaAccessService lojaAccessService;

    public ChatService(ConversaRepository conversaRepository, MensagemRepository mensagemRepository,
                        LojaRepository lojaRepository, UsuarioRepository usuarioRepository,
                        LojaAccessService lojaAccessService) {
        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.lojaRepository = lojaRepository;
        this.usuarioRepository = usuarioRepository;
        this.lojaAccessService = lojaAccessService;
    }

    // ---------- Lado CLIENTE ----------

    @Transactional
    public Conversa obterOuCriarConversa(String lojaId, String clienteId) {
        return conversaRepository.findByLojaIdAndClienteId(lojaId, clienteId)
                .orElseGet(() -> {
                    Loja loja = lojaRepository.findById(lojaId).orElseThrow(() -> new LojaNaoEncontradaException(lojaId));
                    Conversa nova = new Conversa("conv_" + UUID.randomUUID(), loja, clienteId);
                    return conversaRepository.save(nova);
                });
    }

    // ---------- Lado LOJA (lojista) ----------

    @Transactional(readOnly = true)
    public Page<ConversaResumoDTO> listarConversasDaLoja(Usuario usuarioLogado, Pageable pageable) {
        Loja loja = lojaAccessService.obterLojaAcessivel(usuarioLogado);
        Page<Conversa> pagina = conversaRepository.findByLojaIdOrderByUltimaMensagemEmDesc(loja.getId(), pageable);

        List<String> clienteIds = pagina.getContent().stream().map(Conversa::getClienteId).distinct().toList();
        Map<String, String> nomesPorClienteId = usuarioRepository.findAllById(clienteIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNome, (a, b) -> a));

        return pagina.map(conversa -> new ConversaResumoDTO(conversa, nomesPorClienteId.getOrDefault(conversa.getClienteId(), "Cliente")));
    }

    @Transactional(readOnly = true)
    public Page<MensagemDTO> listarMensagens(String conversaId, Usuario usuarioLogado, Pageable pageable) {
        Conversa conversa = buscarConversaAcessivelPelaLoja(conversaId, usuarioLogado);
        return mensagemRepository.findByConversaIdOrderByEnviadaEmDesc(conversa.getId(), pageable).map(MensagemDTO::new);
    }

    @Transactional
    public void marcarComoLidaPelaLoja(String conversaId, Usuario usuarioLogado) {
        Conversa conversa = buscarConversaAcessivelPelaLoja(conversaId, usuarioLogado);
        conversa.marcarComoLidaPelaLoja();
        conversaRepository.save(conversa);
    }

    // ---------- Envio (usado pelo controller WebSocket) ----------

    /**
     * Envia uma mensagem em nome de quem estiver autenticado na sessão WS.
     * Determina remetenteTipo pela relação do usuário com a conversa: cliente
     * dono da conversa manda como CLIENTE; dono/funcionário/admin da loja da
     * conversa manda como LOJA. Qualquer outro usuário toma AcessoNegadoException.
     */
    @Transactional
    public MensagemDTO enviarMensagem(String conversaId, Usuario remetente, String conteudo) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new IdNaoEncontradoException("Conversa não encontrada."));

        RemetenteTipo tipo = resolverTipoRemetente(conversa, remetente);

        Mensagem mensagem = new Mensagem("msg_" + UUID.randomUUID(), conversa, tipo, remetente.getId(), conteudo);
        mensagemRepository.save(mensagem);

        String preview = conteudo.length() > 120 ? conteudo.substring(0, 117) + "..." : conteudo;
        conversa.registrarNovaMensagem(tipo, preview);
        conversaRepository.save(conversa);

        return new MensagemDTO(mensagem);
    }

    private RemetenteTipo resolverTipoRemetente(Conversa conversa, Usuario usuario) {
        if (usuario.getId().equals(conversa.getClienteId())) {
            return RemetenteTipo.CLIENTE;
        }
        if (lojaAccessService.temAcessoALoja(usuario, conversa.getLoja().getId())) {
            return RemetenteTipo.LOJA;
        }
        throw new AcessoNegadoException("Acesso negado: você não faz parte desta conversa.");
    }

    private Conversa buscarConversaAcessivelPelaLoja(String conversaId, Usuario usuarioLogado) {
        Loja loja = lojaAccessService.obterLojaAcessivel(usuarioLogado);
        return conversaRepository.findByIdAndLojaId(conversaId, loja.getId())
                .orElseThrow(() -> new IdNaoEncontradoException("Conversa não encontrada."));
    }
}
