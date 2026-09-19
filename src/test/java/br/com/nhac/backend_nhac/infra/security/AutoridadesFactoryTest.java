package br.com.nhac.backend_nhac.infra.security;

import br.com.nhac.backend_nhac.domain.entregador.EntregadorRepository;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre a regra que substituiu o antigo usuario.setPapel(ENTREGADOR):
 * ROLE_ENTREGADOR passa a ser somada às authorities a partir do vínculo
 * ativo em tb_entregadores, sem apagar o papel principal (CLIENTE).
 */
@ExtendWith(MockitoExtension.class)
class AutoridadesFactoryTest {

    @Mock
    private EntregadorRepository entregadorRepository;

    @InjectMocks
    private AutoridadesFactory autoridadesFactory;

    private Usuario usuarioComPapel(Papel papel) {
        Usuario usuario = new Usuario();
        usuario.setId("user_1");
        usuario.setPapel(papel);
        return usuario;
    }

    private Set<String> authoritiesDe(Usuario usuario) {
        return autoridadesFactory.montar(usuario).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Deve manter apenas o papel principal quando não houver entregador ativo vinculado")
    void deveManterApenasPapelPrincipalSemEntregadorAtivo() {
        Usuario cliente = usuarioComPapel(Papel.CLIENTE);
        when(entregadorRepository.existsByUsuarioIdAndAtivoTrue("user_1")).thenReturn(false);

        assertEquals(Set.of("ROLE_CLIENTE"), authoritiesDe(cliente));
    }

    @Test
    @DisplayName("Deve somar ROLE_ENTREGADOR quando houver entregador ativo vinculado")
    void deveSomarRoleEntregadorQuandoHouverEntregadorAtivo() {
        Usuario cliente = usuarioComPapel(Papel.CLIENTE);
        when(entregadorRepository.existsByUsuarioIdAndAtivoTrue("user_1")).thenReturn(true);

        assertEquals(Set.of("ROLE_CLIENTE", "ROLE_ENTREGADOR"), authoritiesDe(cliente));
    }

    @Test
    @DisplayName("Não deve duplicar ROLE_ENTREGADOR nem consultar o banco quando o papel já for ENTREGADOR")
    void naoDeveDuplicarRoleEntregadorQuandoPapelJaForEntregador() {
        Usuario entregador = usuarioComPapel(Papel.ENTREGADOR);

        List<String> roles = autoridadesFactory.montar(entregador).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertEquals(List.of("ROLE_ENTREGADOR"), roles);
        verify(entregadorRepository, never()).existsByUsuarioIdAndAtivoTrue("user_1");
    }
}