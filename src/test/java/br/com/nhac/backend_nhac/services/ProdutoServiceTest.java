package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
}