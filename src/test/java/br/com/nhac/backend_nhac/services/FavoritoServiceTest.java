package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.repositories.FavoritoRepository;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LojaRepository lojaRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    @Test
    @DisplayName("Deve retornar true se usuario segue a loja")
    void deveRetornarTrueSeUsuarioSegueLoja() {
        when(usuarioRepository.existsById("usu_1")).thenReturn(true);
        when(lojaRepository.existsById("loja_1")).thenReturn(true);
        when(favoritoRepository.existsByUsuarioIdAndLojaId("usu_1", "loja_1")).thenReturn(true);

        boolean segue = favoritoService.usuarioSegueLoja("usu_1", "loja_1");

        assertTrue(segue);
    }

    @Test
    @DisplayName("Deve lancar excecao se usuario nao existir ao verificar se segue loja")
    void deveLancarExcecaoSeUsuarioNaoExistirAoVerificarSeguindo() {
        when(usuarioRepository.existsById("usu_invalido")).thenReturn(false);

        assertThrows(IdNaoEncontradoException.class, 
                () -> favoritoService.usuarioSegueLoja("usu_invalido", "loja_1"));
    }
    @Test
    @DisplayName("Deve favoritar uma loja com sucesso")
    void deveFavoritarLoja() {
        br.com.nhac.backend_nhac.domain.usuario.Usuario usuario = new br.com.nhac.backend_nhac.domain.usuario.Usuario();
        usuario.setId("usu_1");
        
        br.com.nhac.backend_nhac.domain.loja.Loja loja = new br.com.nhac.backend_nhac.domain.loja.Loja();
        loja.setId("loja_1");
        loja.setNome("Loja Teste");
        loja.setImagemUrl("imagem.png");

        when(usuarioRepository.findById("usu_1")).thenReturn(java.util.Optional.of(usuario));
        when(lojaRepository.findById("loja_1")).thenReturn(java.util.Optional.of(loja));
        when(favoritoRepository.existsByUsuarioIdAndLojaId("usu_1", "loja_1")).thenReturn(false);

        br.com.nhac.backend_nhac.domain.favorito.Favorito favoritoSalvo = new br.com.nhac.backend_nhac.domain.favorito.Favorito("fav_1", usuario, loja, java.time.Instant.now());
        when(favoritoRepository.save(any(br.com.nhac.backend_nhac.domain.favorito.Favorito.class))).thenReturn(favoritoSalvo);

        br.com.nhac.backend_nhac.domain.favorito.dto.FavoritoCreateDTO dto = new br.com.nhac.backend_nhac.domain.favorito.dto.FavoritoCreateDTO("loja_1");
        br.com.nhac.backend_nhac.domain.favorito.dto.FavoritoResponseDTO response = favoritoService.favoritar("usu_1", dto);

        assertNotNull(response);
        assertEquals("fav_1", response.id());
    }

    @Test
    @DisplayName("Deve remover favorito")
    void deveRemoverFavorito() {
        br.com.nhac.backend_nhac.domain.favorito.Favorito favorito = new br.com.nhac.backend_nhac.domain.favorito.Favorito();
        when(favoritoRepository.findByUsuarioIdAndLojaId("usu_1", "loja_1")).thenReturn(java.util.Optional.of(favorito));

        favoritoService.removerFavorito("usu_1", "loja_1");

        verify(favoritoRepository, times(1)).delete(favorito);
    }
}
