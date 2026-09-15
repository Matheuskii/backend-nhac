package br.com.nhac.backend_nhac.domain.pedido;

import br.com.nhac.backend_nhac.AbstractIntegrationTest;
import br.com.nhac.backend_nhac.domain.loja.*;
import br.com.nhac.backend_nhac.domain.pedido.dto.PedidoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.produto.ProdutoRepository;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class PedidoConcorrenciaIT extends AbstractIntegrationTest {
    @Autowired private PedidoService pedidoService;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private LojaRepository lojaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    private Usuario cliente;
    private Produto produto;
    private PedidoCreateDTO carrinho;

    @BeforeEach
    void prepararDados() {
        cliente = new Usuario();
        cliente.setId(UUID.randomUUID().toString());
        cliente.setNome("Cliente concorrencia");
        cliente.setTelefone("11999990000");
        cliente.setEmail("concorrencia@teste.com");
        cliente = usuarioRepository.saveAndFlush(cliente);
        Loja loja = new Loja();
        loja.setId(UUID.randomUUID().toString());
        loja.setNome("Loja concorrencia");
        loja.setAberto(true);
        loja.setEndereco(new EnderecoLoja("Rua", "1", "Cidade", "SP", "00000-000", "Bairro", null));
        DadosOperacionais dados = new DadosOperacionais();
        dados.setTaxaEntregaBase(BigDecimal.ZERO);
        loja.setDadosOperacionais(dados);
        loja = lojaRepository.saveAndFlush(loja);
        produto = new Produto();
        produto.setId(UUID.randomUUID().toString());
        produto.setLoja(loja);
        produto.setNome("Produto");
        produto.setCategoriaMenu("Lanches");
        produto.setPreco(BigDecimal.TEN);
        produto.setEstoque(20);
        produto = produtoRepository.saveAndFlush(produto);
        carrinho = new PedidoCreateDTO(loja.getId(), "DINHEIRO", null, null, null,
                new PedidoCreateDTO.EnderecoEntregaDTO("Rua", "1", "Bairro", "Cidade", "SP", "00000-000", null),
                null, List.of(new PedidoCreateDTO.ItemPedidoDTO(produto.getId(), "Produto", null, 1)));
    }

    @Test
    void comprasComLeiturasSimultaneasNaoPerdemBaixaDeEstoque() throws Exception {
        int confirmadas = executarConcorrentes(() -> pedidoService.finalizarPedido(carrinho, cliente, null),
                () -> pedidoService.finalizarPedido(carrinho, cliente, null));
        assertTrue(confirmadas >= 1);
        assertEquals(confirmadas, pedidoRepository.count());
        assertEquals(20 - confirmadas, produtoRepository.findById(produto.getId()).orElseThrow().getEstoque());
    }

    @Test
    void cancelamentosDePedidosDistintosNaoPerdemReposicao() throws Exception {
        String primeiroId = pedidoService.finalizarPedido(carrinho, cliente, null).dto().pedidoId();
        String segundoId = pedidoService.finalizarPedido(carrinho, cliente, null).dto().pedidoId();
        int cancelados = executarConcorrentes(() -> pedidoService.cancelarPedido(primeiroId, cliente.getId()),
                () -> pedidoService.cancelarPedido(segundoId, cliente.getId()));
        assertTrue(cancelados >= 1);
        assertEquals(18 + cancelados, produtoRepository.findById(produto.getId()).orElseThrow().getEstoque());
        assertEquals(cancelados, pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() == StatusPedido.CANCELADO).count());
    }

    private int executarConcorrentes(Runnable primeira, Runnable segunda) throws Exception {
        CyclicBarrier leituras = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> a = pool.submit(() -> executar(primeira, leituras));
            Future<Boolean> b = pool.submit(() -> executar(segunda, leituras));
            return (a.get(20, TimeUnit.SECONDS) ? 1 : 0) + (b.get(20, TimeUnit.SECONDS) ? 1 : 0);
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private boolean executar(Runnable operacao, CyclicBarrier leituras) {
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                produtoRepository.findById(produto.getId()).orElseThrow();
                aguardar(leituras);
                operacao.run();
            });
            return true;
        } catch (OptimisticLockingFailureException conflito) {
            return false;
        }
    }

    private static void aguardar(CyclicBarrier barreira) {
        try {
            barreira.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (BrokenBarrierException | TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }
}
