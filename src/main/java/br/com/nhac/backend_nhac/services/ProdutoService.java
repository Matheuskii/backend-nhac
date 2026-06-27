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

    public Page<ProdutoResumoDTO> listarProdutoPorLoja(String lojaId, Pageable pageable){

        if(!lojaRepository.existsById(lojaId)){
            throw new IdNaoEncontradoException("A loja com o id: " + lojaId + " não foi encontrada.");
        }

        Page<Produto> produtosPaginados = produtoRepository.findByLojaId(lojaId, pageable);

        return produtosPaginados.map(ProdutoResumoDTO::new);
    }
}