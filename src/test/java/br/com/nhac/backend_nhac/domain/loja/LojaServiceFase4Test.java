package br.com.nhac.backend_nhac.domain.loja;

import br.com.nhac.backend_nhac.domain.loja.dto.LojaCreateDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
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

    @Mock
    private UsuarioRepository usuarioRepository;

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
        lojaMock.setUsuarioId("user_1");
        DadosOperacionais dadosMock = new DadosOperacionais();
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

        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setId("user_1");
        usuarioLogado.setPapel(Papel.CLIENTE);

        LojaResumoDTO resumo = lojaService.criarLoja(dto, usuarioLogado);

        assertEquals("loja_0006", resumo.id());
        assertEquals("Nova Loja", resumo.nome());
        assertEquals(Papel.LOJISTA, usuarioLogado.getPapel());

        verify(lojaRepository).save(argThat(loja ->
                "loja_0006".equals(loja.getId()) && "user_1".equals(loja.getUsuarioId())));
    }
}
