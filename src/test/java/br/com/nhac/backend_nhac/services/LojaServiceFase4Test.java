package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaCreateDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LojaServiceFase4Test {

    @Mock
    private LojaRepository lojaRepository;

    @InjectMocks
    private LojaService lojaService;

    @Test
    @DisplayName("Deve criar loja e gerar ID customizado sequencialmente")
    void deveCriarLojaComIdCustomizado() {
        when(lojaRepository.count()).thenReturn(5L);
        
        Loja lojaMock = new Loja();
        lojaMock.setId("loja_0006");
        lojaMock.setNome("Nova Loja");
        lojaMock.setImagemUrl("img.jpg");
        br.com.nhac.backend_nhac.domain.loja.DadosOperacionais dadosMock = new br.com.nhac.backend_nhac.domain.loja.DadosOperacionais();
        dadosMock.setAvaliacaoMedia(0.0f);
        lojaMock.setDadosOperacionais(dadosMock);
        
        when(lojaRepository.save(any(Loja.class))).thenReturn(lojaMock);

        LojaCreateDTO.DadosOperacionaisDTO dadosOp = new LojaCreateDTO.DadosOperacionaisDTO(new BigDecimal("5.0"), 30, 45);
        LojaCreateDTO.EnderecoDTO endereco = new LojaCreateDTO.EnderecoDTO("Rua X", "123", "Cidade", "SP", "01234-567");
        LojaCreateDTO.HorariosDTO horarios = new LojaCreateDTO.HorariosDTO("F", "F", "F", "F", "F", "F", "F");

        LojaCreateDTO dto = new LojaCreateDTO(
            "Nova Loja", "Desc", "Categoria", "img.jpg", true,
            dadosOp, endereco, horarios
        );
        
        LojaResumoDTO resumo = lojaService.criarLoja(dto);

        assertEquals("loja_0006", resumo.id());
        assertEquals("Nova Loja", resumo.nome());
        
        verify(lojaRepository).save(argThat(loja -> "loja_0006".equals(loja.getId())));
    }
}
