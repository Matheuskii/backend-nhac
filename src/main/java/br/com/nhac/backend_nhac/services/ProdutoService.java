package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoResumoDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;


@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, LojaRepository lojaRepository) {
        this.produtoRepository = produtoRepository;
        this.lojaRepository = lojaRepository;
    }

    @Transactional
    public Produto cadastrarProduto(ProdutoCreateDTO dto) {
        Loja lojaDoProduto = lojaRepository.findById(dto.lojaId())
                .orElseThrow(() -> new IdNaoEncontradoException("A loja com o id: " + dto.lojaId() + " não foi encontrada."));

        if (!lojaDoProduto.isAberto()) {
            throw new RegraDeNegocioException("Não é possível cadastrar produtos em uma loja fechada.");
        }

        Produto novoProduto = dto.toEntity(lojaDoProduto, produtoRepository);
        return produtoRepository.save(novoProduto);
    }

    @Transactional(readOnly = true)
    public ProdutoResumoDTO buscarProdutoPorId(String produtoId) {
        Produto produto = produtoRepository.findByIdAndIsAtivoTrue(produtoId)
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "O produto com o id: " + produtoId + " não foi encontrado."));

        return new ProdutoResumoDTO(produto);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResumoDTO> listarProdutos(String lojaId, BigDecimal precoMaximo, String categoriaMenu, String nome, Pageable pageable) {
        Page<Produto> produtos = produtoRepository.findAllWithFilters(lojaId, categoriaMenu, nome, precoMaximo, pageable);
        return produtos.map(ProdutoResumoDTO::new);
    }
}
