package br.com.nhac.backend_nhac.services;


import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaDetalhesDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LojaService {

    private final LojaRepository lojaRepository;


    public LojaService(LojaRepository lojaRepository){
        this.lojaRepository = lojaRepository;
    }


    // Função que devolve page de DTO
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
}
