package br.com.nhac.backend_nhac.domain.entregador;

import br.com.nhac.backend_nhac.domain.entregador.dto.AtualizarLocalizacaoDTO;
import br.com.nhac.backend_nhac.domain.entregador.dto.AtualizarStatusDTO;
import br.com.nhac.backend_nhac.domain.entregador.dto.CadastroEntregadorDTO;
import br.com.nhac.backend_nhac.domain.entregador.dto.EntregadorResponseDTO;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntregadorServiceTest {

    @Mock
    private EntregadorRepository entregadorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EntregadorService entregadorService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("user_123");
        usuario.setNome("Motoboy Carlos");
        usuario.setEmail("carlos@nhac.com");
        usuario.setTelefone("11999998888");
        usuario.setPapel(Papel.CLIENTE);
    }

    @Test
    @DisplayName("Deve cadastrar novo entregador mantendo o papel principal intacto")
    void deveCadastrarEntregadorComSucesso() {
        CadastroEntregadorDTO dto = new CadastroEntregadorDTO("12345678900", "ABC1D23", TipoVeiculo.MOTO);

        when(entregadorRepository.existsByUsuarioId(usuario.getId())).thenReturn(false);
        when(entregadorRepository.save(any(Entregador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EntregadorResponseDTO resposta = entregadorService.cadastrar(dto, usuario);

        assertNotNull(resposta);
        assertEquals("ABC1D23", resposta.placaVeiculo());
        assertEquals(StatusOperacional.OFFLINE, resposta.statusOperacional());
        // O papel principal NÃO é mais sobrescrito para ENTREGADOR: quem
        // confirma que esta conta também é entregadora é o registro em
        // tb_entregadores (consultado por AutoridadesFactory).
        assertEquals(Papel.CLIENTE, usuario.getPapel(), "o cadastro de entregador não deve mais sobrescrever o papel");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(entregadorRepository, times(1)).save(any(Entregador.class));
    }

    @Test
    @DisplayName("Deve recusar cadastro de entregador para contas de loja (LOJISTA/FUNCIONARIO)")
    void deveRecusarCadastroDeEntregadorParaContaDeLoja() {
        CadastroEntregadorDTO dto = new CadastroEntregadorDTO("12345678900", "ABC1D23", TipoVeiculo.MOTO);
        when(entregadorRepository.existsByUsuarioId(usuario.getId())).thenReturn(false);

        for (Papel papelDeLoja : List.of(Papel.LOJISTA, Papel.FUNCIONARIO)) {
            usuario.setPapel(papelDeLoja);
            assertThrows(RegraDeNegocioException.class, () -> entregadorService.cadastrar(dto, usuario));
        }

        verify(entregadorRepository, never()).save(any(Entregador.class));
    }

    @Test
    @DisplayName("Deve lancar excecao se usuario ja for cadastrado como entregador")
    void deveLancarExcecaoAoCadastrarEntregadorDuplicado() {
        CadastroEntregadorDTO dto = new CadastroEntregadorDTO("12345678900", "ABC1D23", TipoVeiculo.MOTO);
        when(entregadorRepository.existsByUsuarioId(usuario.getId())).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> entregadorService.cadastrar(dto, usuario));
        verify(entregadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve alterar status operacional do entregador para ONLINE")
    void deveAtualizarStatusOperacional() {
        Entregador entregador = Entregador.builder()
                .id("ent_1")
                .usuario(usuario)
                .cnh("12345678900")
                .placaVeiculo("ABC1D23")
                .statusOperacional(StatusOperacional.OFFLINE)
                .ativo(true)
                .build();

        when(entregadorRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(entregador));
        when(entregadorRepository.save(any(Entregador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AtualizarStatusDTO dto = new AtualizarStatusDTO(StatusOperacional.ONLINE);
        EntregadorResponseDTO resposta = entregadorService.atualizarStatus(dto, usuario);

        assertEquals(StatusOperacional.ONLINE, resposta.statusOperacional());
    }

    @Test
    @DisplayName("Deve atualizar localizacao GPS do entregador")
    void deveAtualizarLocalizacao() {
        Entregador entregador = Entregador.builder()
                .id("ent_1")
                .usuario(usuario)
                .cnh("12345678900")
                .placaVeiculo("ABC1D23")
                .statusOperacional(StatusOperacional.ONLINE)
                .ativo(true)
                .build();

        when(entregadorRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(entregador));
        when(entregadorRepository.save(any(Entregador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AtualizarLocalizacaoDTO dto = new AtualizarLocalizacaoDTO(-23.55052, -46.63330);
        EntregadorResponseDTO resposta = entregadorService.atualizarLocalizacao(dto, usuario);

        assertEquals(-23.55052, resposta.latitudeAtual());
        assertEquals(-46.63330, resposta.longitudeAtual());
        assertNotNull(resposta.ultimaAtualizacaoLocalizacao());
    }

    @Test
    @DisplayName("Deve filtrar e ordenar entregadores proximos por distancia")
    void deveBuscarEntregadoresProximos() {
        // Ponto de referência: Marco Zero de São Paulo (-23.55052, -46.63330)
        double lojaLat = -23.55052;
        double lojaLng = -46.63330;

        // Entregador 1: ~1.1 km de distância (-23.56000, -46.63330)
        Entregador entPerto = Entregador.builder()
                .id("ent_perto")
                .usuario(usuario)
                .statusOperacional(StatusOperacional.ONLINE)
                .latitudeAtual(-23.56000)
                .longitudeAtual(-46.63330)
                .ativo(true)
                .build();

        // Entregador 2: ~15 km de distância (-23.68000, -46.63330)
        Entregador entLonge = Entregador.builder()
                .id("ent_longe")
                .usuario(usuario)
                .statusOperacional(StatusOperacional.ONLINE)
                .latitudeAtual(-23.68000)
                .longitudeAtual(-46.63330)
                .ativo(true)
                .build();

        when(entregadorRepository.findByStatusOperacionalAndAtivoTrue(StatusOperacional.ONLINE))
                .thenReturn(List.of(entLonge, entPerto));

        var resultado = entregadorService.buscarEntregadoresProximos(lojaLat, lojaLng, 5.0);

        assertEquals(1, resultado.size());
        assertEquals("ent_perto", resultado.get(0).entregador().getId());
        assertTrue(resultado.get(0).distanciaKm() < 2.0);
    }
}
