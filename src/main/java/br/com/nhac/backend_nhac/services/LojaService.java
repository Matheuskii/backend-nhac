package br.com.nhac.backend_nhac.services;


import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaCreateDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaDetalhesDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class LojaService {

    private final LojaRepository lojaRepository;


    public LojaService(LojaRepository lojaRepository){
        this.lojaRepository = lojaRepository;
    }


    public Page<LojaResumoDTO> obterLojasPaginadas(int page, int size){
        Pageable paginacao = PageRequest.of(page, size);


        Page<Loja> lojas = lojaRepository.findByIsAbertoTrue(paginacao);

        return lojas.map(LojaResumoDTO::new);
    }

    public LojaDetalhesDTO obterLojaId(String id){

        Loja loja = lojaRepository.findByIdAndIsAbertoTrue(id)
                .orElseThrow(() -> new IdNaoEncontradoException("A loja com o id: " + id + " não foi encontrada."));

        return new LojaDetalhesDTO(loja);


    }
    @Transactional
    public String criarLoja(LojaCreateDTO dto) {
        long totalLojas = contarLojasCadastradas();
        String novoId = String.format("loja_%04d", totalLojas + 1);
        
        Loja novaLoja = dto.toEntity();
        novaLoja.setId(novoId);
        lojaRepository.save(novaLoja);
        return novoId;
    }

    public long contarLojasCadastradas() {
        return lojaRepository.count();
    }
}
