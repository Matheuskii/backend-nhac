package br.com.nhac.backend_nhac.domain.chat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.nhac.backend_nhac.domain.chat.dto.ChatDTOs.ConversaResumoDTO;
import br.com.nhac.backend_nhac.domain.chat.dto.ChatDTOs.MensagemDTO;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.LojaAccessService;
import br.com.nhac.backend_nhac.domain.loja.LojaRepository;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.LojaNaoEncontradaException;

/**
 * Ponto único de regra de negócio do chat (item 7 da spec, arquitetura
 * decidida como WebSocket em vez de polling).
 *
 * Uma Conversa é sempre entre UM cliente e UMA loja (não por pedido — é um
 * canal contínuo, como a maioria dos apps de delivery faz). Do lado da loja,
 * "quem enviou" pode ser o dono ou qualquer funcionário — pro cliente, toda
 * mensagem do lado loja aparece igual (remetenteTipo=LOJA), só guardamos
 * remetenteUsuarioId internamente pra rastreabilidade.
 */
@Service
public class ChatService {

    private static final int PREVIEW_MAX_CHARS = 120;

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
    public Conversa obterOuCriarConversa(String lojaId, Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            throw new AcessoNegadoException("É necessário estar autenticado para abrir uma conversa.");
        }
        // Apenas CLIENTE inicia conversa. Um LOJISTA usando essa rota abriria
        // conversas "cliente=lojista" que poluem a lista e permitem spam.
        if (usuarioLogado.getPapel() != Papel.CLIENTE) {
            throw new AcessoNegadoException("Apenas clientes podem iniciar conversas com lojas.");
        }

        String clienteId = usuarioLogado.getId();
        return conversaRepository.findByLojaIdAndClienteId(lojaId, clienteId)
                .orElseGet(() -> {
                    try {
                        Loja loja = lojaRepository.findById(lojaId)
                                .orElseThrow(() -> new LojaNaoEncontradaException(lojaId));
                        Conversa nova = new Conversa("conv_" + UUID.randomUUID(), loja, clienteId);
                        return conversaRepository.saveAndFlush(nova);
                    } catch (DataIntegrityViolationException e) {
                        // Corrida: outra requisição criou entre o find e o save.
                        // O UNIQUE (loja_id, cliente_id) garante consistência — só recupera.
                        return conversaRepository.findByLojaIdAndClienteId(lojaId, clienteId)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Conversa deveria existir após violação de unique", e));
                    }
                });
    }

    // ---------- Lado LOJA (lojista / funcionário) ----------

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

    // ---------- Lado CLIENTE (histórico + marcar como lida) ----------

    /**
     * Marca a conversa como lida pelo lado do cliente. Corresponde ao
     * marcarComoLidaPelaLoja, mas do outro lado — sem endpoint próprio o
     * contador do cliente só cresce.
     */
    @Transactional
    public void marcarComoLidaPeloCliente(String conversaId, Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            throw new AcessoNegadoException("É necessário estar autenticado.");
        }
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new IdNaoEncontradoException("Conversa não encontrada."));
        if (!usuarioLogado.getId().equals(conversa.getClienteId())) {
            throw new AcessoNegadoException("Você não é o cliente desta conversa.");
        }
        conversa.marcarComoLidaPeloCliente();
        conversaRepository.save(conversa);
    }

    // ---------- Consulta usada pelo interceptor WebSocket ----------

    /**
     * Verifica se o usuário autenticado pode acessar a conversa, tanto pra
     * SUBSCRIBE no /topic/conversas/{id} quanto pra enviar mensagem.
     *
     * Acesso permitido:
     *  - CLIENTE dono da conversa (usuario.getId() == conversa.clienteId)
     *  - Dono ou funcionário da loja da conversa (via LojaAccessService)
     *  - ADMIN (bypass via LojaAccessService.temAcessoALoja)
     */
    @Transactional(readOnly = true)
    public boolean podeAcessarConversa(String conversaId, Usuario usuario) {
        if (usuario == null) return false;
        return conversaRepository.findById(conversaId)
                .map(c -> usuario.getId().equals(c.getClienteId())
                        || lojaAccessService.temAcessoALoja(usuario, c.getLoja().getId()))
                .orElse(false);
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

        conversa.registrarNovaMensagem(tipo, truncarPreview(conteudo));
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

    /**
     * Corta a string em até PREVIEW_MAX_CHARS caracteres "visíveis",
     * respeitando code points (emoji não é cortado no meio de um surrogate pair).
     */
    private String truncarPreview(String texto) {
        if (texto == null) return null;
        int total = texto.codePointCount(0, texto.length());
        if (total <= PREVIEW_MAX_CHARS) {
            return texto;
        }
        // Reserva 3 chars pro "..."
        int limite = texto.offsetByCodePoints(0, PREVIEW_MAX_CHARS - 3);
        return texto.substring(0, limite) + "...";
    }
}