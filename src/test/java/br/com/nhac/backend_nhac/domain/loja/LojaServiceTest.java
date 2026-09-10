package br.com.nhac.backend_nhac.domain.loja;

import br.com.nhac.backend_nhac.domain.loja.DadosOperacionais;
import br.com.nhac.backend_nhac.domain.loja.EnderecoLoja;
import br.com.nhac.backend_nhac.domain.loja.GeoLocalizacao;
import br.com.nhac.backend_nhac.domain.loja.HorariosFuncionamento;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaCreateDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaDetalhesDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
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

    @Mock
    private UsuarioRepository usuarioRepository;

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
                .dadosOperacionais(new DadosOperacionais(4.8f, new BigDecimal("5.99"), 30, 45, 150, true, false, null))
                .endereco(new EnderecoLoja("Rua das Flores", "123", "São Paulo", "SP", "01000-000", "Centro", null))
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

        Page<LojaResumoDTO> resultado = lojaService.obterLojasPaginadas(null, null, null, null, 0, 10);

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

        Page<LojaResumoDTO> resultado = lojaService.obterLojasPaginadas(null, null, null, null, 0, 10);

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

    @Test
    @DisplayName("Deve vincular a loja ao usuário autenticado e promover CLIENTE para LOJISTA")
    void deveCriarLojaVinculadaEPromoverPapelParaLojista() {
        Usuario cliente = new Usuario();
        cliente.setId("user_lojista");
        cliente.setPapel(Papel.CLIENTE);

        when(lojaRepository.count()).thenReturn(0L);
        when(lojaRepository.save(any(Loja.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LojaResumoDTO resumo = lojaService.criarLoja(construirDtoCriacao(), cliente);

        assertEquals("loja_0001", resumo.id());
        assertEquals(Papel.LOJISTA, cliente.getPapel());
        verify(usuarioRepository).save(cliente);
        verify(lojaRepository).save(argThat(loja ->
                "user_lojista".equals(loja.getUsuarioId()) && "loja_0001".equals(loja.getId())));
    }

    @Test
    @DisplayName("Não deve rebaixar ADMIN ao criar loja")
    void deveManterPapelAdminAoCriarLoja() {
        Usuario admin = new Usuario();
        admin.setId("user_admin");
        admin.setPapel(Papel.ADMIN);

        when(lojaRepository.count()).thenReturn(1L);
        when(lojaRepository.save(any(Loja.class))).thenAnswer(invocation -> invocation.getArgument(0));

        lojaService.criarLoja(construirDtoCriacao(), admin);

        assertEquals(Papel.ADMIN, admin.getPapel());
        verify(usuarioRepository, never()).save(any());
        verify(lojaRepository).save(argThat(loja -> "user_admin".equals(loja.getUsuarioId())));
    }

    @Test
    @DisplayName("Deve recusar criação de loja sem usuário autenticado")
    void deveRecusarCriacaoSemUsuarioAutenticado() {
        assertThrows(AcessoNegadoException.class, () -> lojaService.criarLoja(construirDtoCriacao(), null));
        verify(lojaRepository, never()).save(any());
    }

    private LojaCreateDTO construirDtoCriacao() {
        LojaCreateDTO.DadosOperacionaisDTO dadosOp = new LojaCreateDTO.DadosOperacionaisDTO(new BigDecimal("5.0"), 30, 45, true, false, null);
        LojaCreateDTO.EnderecoDTO endereco = new LojaCreateDTO.EnderecoDTO("Rua X", "123", "Cidade", "SP", "01234-567", "Centro", null);
        LojaCreateDTO.HorariosDTO horarios = new LojaCreateDTO.HorariosDTO("F", "F", "F", "F", "F", "F", "F");
        return new LojaCreateDTO("Nova Loja", "Desc", "Categoria", "img.jpg", true, dadosOp, endereco, horarios);
    }
}
