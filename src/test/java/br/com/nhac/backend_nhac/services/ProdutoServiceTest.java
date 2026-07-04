package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoResumoDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private LojaRepository lojaRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    @DisplayName("Deve cadastrar produto com sucesso quando a loja for encontrada no banco")
    void deveCadastrarProdutoComSucesso() {
        String lojaId = "loja_123";
        Loja lojaFalsa = new Loja();
        lojaFalsa.setId(lojaId);

        ProdutoCreateDTO dto = new ProdutoCreateDTO(
                lojaId, "Hossomaki", "Descrição", new BigDecimal("25.50"),
                "Sushi", "url", "200g", 10
        );

        Produto produtoSalvo = new Produto();
        produtoSalvo.setId("produto_gerado_123");
        produtoSalvo.setNome("Hossomaki");

        when(lojaRepository.findById(lojaId)).thenReturn(Optional.of(lojaFalsa));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoSalvo);

        Produto resultado = produtoService.cadastrarProduto(dto);

        assertNotNull(resultado);
        assertEquals("Hossomaki", resultado.getNome());

        verify(lojaRepository, times(1)).findById(lojaId);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve explodir exceção quando tentar cadastrar produto numa loja que não existe")
    void deveLancarExcecaoQuandoLojaNaoExistir() {
        String lojaFantasmaId = "loja_que_nao_existe_000";
        ProdutoCreateDTO dto = new ProdutoCreateDTO(
                lojaFantasmaId, "Hossomaki", "Descrição", new BigDecimal("25.50"),
                "Sushi", "url", "200g",  10
        );

        when(lojaRepository.findById(lojaFantasmaId)).thenReturn(Optional.empty());

        Exception excecao = assertThrows(IdNaoEncontradoException.class, () -> {
            produtoService.cadastrarProduto(dto);
        });

        assertEquals("A loja com o id: " + lojaFantasmaId + " não foi encontrada.", excecao.getMessage());
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    private Produto produtoDeTeste() {
        Loja loja = new Loja();
        loja.setId("loja_1");

        Produto produto = new Produto();
        produto.setId("produto_1");
        produto.setLoja(loja);
        produto.setNome("Hossomaki");
        produto.setDescricao("Descrição do produto");
        produto.setPreco(new BigDecimal("25.50"));
        produto.setCategoriaMenu("Sushi");
        produto.setImagemUrl("url");
        produto.setPeso("200g");
        produto.setPercentualDesconto(10);
        return produto;
    }

    @Test
    @DisplayName("Deve listar produtos filtrando por nome quando o parâmetro for informado")
    void deveListarProdutosFiltrandoPorNome() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> pagina = new PageImpl<>(List.of(produtoDeTeste()), pageable, 1);

        when(produtoRepository.findByNomeContainingIgnoreCase(eq("hosso"), any(Pageable.class))).thenReturn(pagina);

        Page<ProdutoResumoDTO> resultado = produtoService.listarProdutos(null, null, null, "hosso", pageable);

        assertEquals(1, resultado.getTotalElements());
        verify(produtoRepository, times(1)).findByNomeContainingIgnoreCase(eq("hosso"), any(Pageable.class));
        verify(produtoRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve listar produtos filtrando por preço máximo quando o parâmetro for informado")
    void deveListarProdutosFiltrandoPorPrecoMaximo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> pagina = new PageImpl<>(List.of(produtoDeTeste()), pageable, 1);
        BigDecimal precoMaximo = new BigDecimal("30.00");

        when(produtoRepository.findByPrecoLessThanEqual(eq(precoMaximo), any(Pageable.class))).thenReturn(pagina);

        Page<ProdutoResumoDTO> resultado = produtoService.listarProdutos(null, precoMaximo, null, null, pageable);

        assertEquals(1, resultado.getTotalElements());
        verify(produtoRepository, times(1)).findByPrecoLessThanEqual(eq(precoMaximo), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve listar produtos filtrando por categoria quando o parâmetro for informado")
    void deveListarProdutosFiltrandoPorCategoria() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> pagina = new PageImpl<>(List.of(produtoDeTeste()), pageable, 1);

        when(produtoRepository.findByCategoriaMenuIgnoreCase(eq("Sushi"), any(Pageable.class))).thenReturn(pagina);

        Page<ProdutoResumoDTO> resultado = produtoService.listarProdutos(null, null, "Sushi", null, pageable);

        assertEquals(1, resultado.getTotalElements());
        verify(produtoRepository, times(1)).findByCategoriaMenuIgnoreCase(eq("Sushi"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve listar produtos filtrando por loja quando o parâmetro for informado")
    void deveListarProdutosFiltrandoPorLoja() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> pagina = new PageImpl<>(List.of(produtoDeTeste()), pageable, 1);

        when(produtoRepository.findByLojaId(eq("loja_1"), any(Pageable.class))).thenReturn(pagina);

        Page<ProdutoResumoDTO> resultado = produtoService.listarProdutos("loja_1", null, null, null, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("loja_1", resultado.getContent().get(0).lojaId());
        assertEquals("Hossomaki", resultado.getContent().get(0).nome());
        verify(produtoRepository, times(1)).findByLojaId(eq("loja_1"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve listar todos os produtos quando nenhum filtro for informado")
    void deveListarTodosOsProdutosSemFiltros() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> pagina = new PageImpl<>(List.of(produtoDeTeste()), pageable, 1);

        when(produtoRepository.findAll(any(Pageable.class))).thenReturn(pagina);

        Page<ProdutoResumoDTO> resultado = produtoService.listarProdutos(null, null, null, null, pageable);

        assertEquals(1, resultado.getTotalElements());
        verify(produtoRepository, times(1)).findAll(any(Pageable.class));
    }
}