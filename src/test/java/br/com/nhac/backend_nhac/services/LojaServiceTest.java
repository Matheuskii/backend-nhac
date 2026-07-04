package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.loja.DadosOperacionais;
import br.com.nhac.backend_nhac.domain.loja.EnderecoLoja;
import br.com.nhac.backend_nhac.domain.loja.GeoLocalizacao;
import br.com.nhac.backend_nhac.domain.loja.HorariosFuncionamento;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaDetalhesDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LojaServiceTest {

    @Mock
    private LojaRepository lojaRepository;

    @InjectMocks
    private LojaService lojaService;

    private Loja construirLojaCompleta(String id, boolean aberta) {
        return Loja.builder()
                .id(id)
                .nome("Sushi Ken")
                .descricao("O melhor sushi da região.")
                .categoria("Japonesa")
                .imagemUrl("http://imagem.com/banner.png")
                .isAberto(aberta)
                .dadosOperacionais(new DadosOperacionais(4.8f, new BigDecimal("5.99"), 30, 45, 150))
                .endereco(new EnderecoLoja("Rua das Flores", "123", "São Paulo", "SP", "01000-000"))
                .geoLocalizacao(new GeoLocalizacao(-23.5, -46.6, "hash123"))
                .horariosFuncionamento(new HorariosFuncionamento(
                        "18:00-23:00", "Fechado", "11:00-23:00", "11:00-23:00",
                        "11:00-23:00", "11:00-23:59", "11:00-23:59"))
                .build();
    }

    @Test
    @DisplayName("Deve retornar página de lojas abertas mapeadas para LojaResumoDTO")
    void deveObterLojasPaginadasComSucesso() {
        Loja loja = construirLojaCompleta("loja_1", true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loja> paginaDeLojas = new PageImpl<>(List.of(loja), pageable, 1);

        when(lojaRepository.findByIsAbertoTrue(any(Pageable.class))).thenReturn(paginaDeLojas);

        Page<LojaResumoDTO> resultado = lojaService.obterLojasPaginadas(0, 10);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("loja_1", resultado.getContent().get(0).id());
        assertEquals("Sushi Ken", resultado.getContent().get(0).nome());
        verify(lojaRepository, times(1)).findByIsAbertoTrue(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não houver lojas abertas")
    void deveRetornarPaginaVaziaQuandoNaoHouverLojas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loja> paginaVazia = new PageImpl<>(List.of(), pageable, 0);

        when(lojaRepository.findByIsAbertoTrue(any(Pageable.class))).thenReturn(paginaVazia);

        Page<LojaResumoDTO> resultado = lojaService.obterLojasPaginadas(0, 10);

        assertTrue(resultado.getContent().isEmpty());
    }

    @Test
    @DisplayName("Deve retornar os detalhes completos de uma loja aberta existente")
    void deveObterDetalhesDaLojaComSucesso() {
        Loja loja = construirLojaCompleta("loja_1", true);

        when(lojaRepository.findByIdAndIsAbertoTrue("loja_1")).thenReturn(Optional.of(loja));

        LojaDetalhesDTO resultado = lojaService.obterLojaId("loja_1");

        assertNotNull(resultado);
        assertEquals("loja_1", resultado.id());
        assertEquals("Sushi Ken", resultado.nome());
        assertEquals("Rua das Flores", resultado.endereco().rua());
    }

    @Test
    @DisplayName("Deve lançar IdNaoEncontradoException quando a loja não existir ou estiver fechada")
    void deveLancarExcecaoQuandoLojaNaoEncontradaOuFechada() {
        when(lojaRepository.findByIdAndIsAbertoTrue("loja_fantasma")).thenReturn(Optional.empty());

        Exception excecao = assertThrows(IdNaoEncontradoException.class,
                () -> lojaService.obterLojaId("loja_fantasma"));

        assertEquals("A loja com o id: loja_fantasma não foi encontrada.", excecao.getMessage());
    }
}
