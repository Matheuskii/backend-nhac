package br.com.nhac.backend_nhac.domain.produto;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoCreateDTO;
import br.com.nhac.backend_nhac.domain.produto.dto.ProdutoResumoDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.domain.loja.LojaRepository;
import br.com.nhac.backend_nhac.domain.produto.ProdutoRepository;
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
    public Produto cadastrarProduto(ProdutoCreateDTO dto, Usuario usuarioLogado) {
        // ADMIN tem bypass na checagem de ownership, mas ainda precisa de uma loja associada
        boolean isAdmin = usuarioLogado.getPapel().name().equals("ADMIN");
        
        Loja lojaDoUsuario = lojaRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new RegraDeNegocioException("É preciso ter uma loja cadastrada antes de adicionar produtos."));

        if (!lojaDoUsuario.isAberto() && !isAdmin) {
            throw new RegraDeNegocioException("Não é possível cadastrar produtos em uma loja fechada.");
        }

        Produto novoProduto = dto.toEntity(lojaDoUsuario, produtoRepository);
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

    @Transactional
    public ProdutoResumoDTO atualizarProduto(String id, br.com.nhac.backend_nhac.domain.produto.dto.ProdutoUpdateDTO dto, Usuario usuarioLogado) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("O produto com o id: " + id + " não foi encontrado."));

        // ADMIN tem bypass na checagem de ownership
        boolean isAdmin = usuarioLogado.getPapel().name().equals("ADMIN");
        if (!isAdmin && !produto.getLoja().getUsuarioId().equals(usuarioLogado.getId())) {
            throw new AcessoNegadoException("Acesso negado: você não tem permissão para editar este produto.");
        }

        if (!produto.getLoja().isAberto() && !isAdmin) {
            throw new RegraDeNegocioException("Não é possível editar produtos de uma loja fechada.");
        }

        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setCategoriaMenu(dto.categoriaMenu());
        produto.setImagemUrl(dto.imagemUrl());
        produto.setPeso(dto.peso());
        produto.setPercentualDesconto(dto.percentualDesconto());
        produto.setAtivo(dto.isAtivo());

        produtoRepository.save(produto);

        return new ProdutoResumoDTO(produto);
    }

    @Transactional
    public void desativarProduto(String id, Usuario usuarioLogado) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("O produto com o id: " + id + " não foi encontrado."));

        // ADMIN tem bypass na checagem de ownership
        boolean isAdmin = usuarioLogado.getPapel().name().equals("ADMIN");
        if (!isAdmin && !produto.getLoja().getUsuarioId().equals(usuarioLogado.getId())) {
            throw new AcessoNegadoException("Acesso negado: você não tem permissão para desativar este produto.");
        }

        if (!produto.getLoja().isAberto() && !isAdmin) {
            throw new RegraDeNegocioException("Não é possível desativar produtos de uma loja fechada.");
        }

        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public br.com.nhac.backend_nhac.domain.produto.dto.ProdutoAvaliacaoResumoDTO buscarResumoAvaliacoes(String produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new IdNaoEncontradoException("O produto com o id: " + produtoId + " nǜo foi encontrado.");
        }
        return produtoRepository.getResumoAvaliacoesPorProdutoId(produtoId);
    }
}
