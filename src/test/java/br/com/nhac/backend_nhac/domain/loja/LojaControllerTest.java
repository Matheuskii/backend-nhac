package br.com.nhac.backend_nhac.domain.loja;

import br.com.nhac.backend_nhac.domain.loja.dto.LojaDetalhesDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.services.LojaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LojaController.class)
@AutoConfigureMockMvc(addFilters = false)
class LojaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LojaService lojaService;

    @MockitoBean
    private br.com.nhac.backend_nhac.infra.security.TokenService tokenService;

    @MockitoBean
    private br.com.nhac.backend_nhac.repositories.UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve retornar 200 com a página de lojas abertas")
    void deveListarLojasComSucesso() throws Exception {
        LojaResumoDTO.DadosOperacionaisDTO dadosOp =
                new LojaResumoDTO.DadosOperacionaisDTO(4.8f, new BigDecimal("5.99"), 30, 45, 150);
        LojaResumoDTO resumo = new LojaResumoDTO("loja_1", "Sushi Ken", "Descrição", "Japonesa", "url", dadosOp);
        Page<LojaResumoDTO> pagina = new PageImpl<>(List.of(resumo), PageRequest.of(0, 10), 1);

        when(lojaService.obterLojasPaginadas(anyInt(), anyInt())).thenReturn(pagina);

        mockMvc.perform(get("/api/v1/lojas").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("loja_1"))
                .andExpect(jsonPath("$.content[0].nome").value("Sushi Ken"));
    }

    @Test
    @DisplayName("Deve retornar 200 com os detalhes de uma loja existente")
    void deveRetornarDetalhesDaLojaComSucesso() throws Exception {
        LojaDetalhesDTO.DadosOperacionaisDTO dadosOp =
                new LojaDetalhesDTO.DadosOperacionaisDTO(4.8f, new BigDecimal("5.99"), 30, 45, 150);
        LojaDetalhesDTO.EnderecoDTO endereco =
                new LojaDetalhesDTO.EnderecoDTO("Rua das Flores", "123", "São Paulo", "SP", "01000-000");
        LojaDetalhesDTO.HorariosDTO horarios = new LojaDetalhesDTO.HorariosDTO(
                "18:00-23:00", "Fechado", "11:00-23:00", "11:00-23:00", "11:00-23:00", "11:00-23:59", "11:00-23:59");
        LojaDetalhesDTO detalhes = new LojaDetalhesDTO(
                "loja_1", "Sushi Ken", "Descrição completa", "Japonesa", "url", dadosOp, endereco, horarios);

        when(lojaService.obterLojaId("loja_1")).thenReturn(detalhes);

        mockMvc.perform(get("/api/v1/lojas/loja_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("loja_1"))
                .andExpect(jsonPath("$.endereco.rua").value("Rua das Flores"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando a loja não for encontrada")
    void deveRetornar404QuandoLojaNaoEncontrada() throws Exception {
        when(lojaService.obterLojaId("loja_fantasma"))
                .thenThrow(new IdNaoEncontradoException("A loja com o id: loja_fantasma não foi encontrada."));

        mockMvc.perform(get("/api/v1/lojas/loja_fantasma"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("A loja com o id: loja_fantasma não foi encontrada."));
    }
}
