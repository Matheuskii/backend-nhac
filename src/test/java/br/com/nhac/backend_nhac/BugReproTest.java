package br.com.nhac.backend_nhac;

import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import br.com.nhac.backend_nhac.repositories.AvaliacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
public class BugReproTest {

    @Autowired
    ProdutoRepository produtoRepository;

    @Autowired
    AvaliacaoRepository avaliacaoRepository;

    @Test
    void testGetResumo() {
        try {
            System.out.println("TESTING PRODUTO AVALIACAO...");
            var result = produtoRepository.getResumoAvaliacoesPorProdutoId("prod_loja_0001_02");
            System.out.println("RESULT: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void testGetAvaliacoes() {
        try {
            System.out.println("TESTING AVALIACAO POR LOJA...");
            var page = avaliacaoRepository.findByLojaId("loja_0001", PageRequest.of(0, 10));
            for(var a : page.getContent()) {
                System.out.println(new br.com.nhac.backend_nhac.domain.avaliacao.dto.AvaliacaoResumoDTO(a));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
