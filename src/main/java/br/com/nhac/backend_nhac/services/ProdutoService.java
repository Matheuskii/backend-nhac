package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoResumoDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, LojaRepository lojaRepository) {
        this.produtoRepository = produtoRepository;
        this.lojaRepository = lojaRepository;
    }

    public Produto cadastrarProduto(ProdutoCreateDTO dto) {


        Loja lojaDoProduto = lojaRepository.findById(dto.lojaId()).orElseThrow(() -> new IdNaoEncontradoException("A loja com o id: " + dto.lojaId() + " não foi encontrada."));


        Produto novoProduto = dto.toEntity(lojaDoProduto);

        return produtoRepository.save(novoProduto);
    }

    public Page<ProdutoResumoDTO> listarProdutos(String lojaId, BigDecimal precoMaximo, String categoriaMenu, String nome, Pageable pageable) {
        Page<Produto> produtos;

        if (nome != null && !nome.isBlank()) {
            produtos = produtoRepository.findByNomeContainingIgnoreCase(nome, pageable);

        } else if (precoMaximo != null) {
            produtos = produtoRepository.findByPrecoLessThanEqual(precoMaximo, pageable);

        } else if (categoriaMenu != null && !categoriaMenu.isBlank()) {
            produtos = produtoRepository.findByCategoriaMenuIgnoreCase(categoriaMenu, pageable);

        } else if (lojaId != null && !lojaId.isBlank()) {
            produtos = produtoRepository.findByLojaId(lojaId, pageable);

        } else {
            produtos = produtoRepository.findAll(pageable);
        }

        return produtos.map(produto -> new ProdutoResumoDTO(
                produto.getId(),
                produto.getLoja().getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoriaMenu(),
                produto.getImagemUrl(),
                produto.getPeso(),
                produto.getPercentualDesconto()));
}}
