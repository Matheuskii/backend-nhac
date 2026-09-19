package br.com.nhac.backend_nhac.infra.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import br.com.nhac.backend_nhac.domain.entregador.EntregadorRepository;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;

/**
 * Monta as authorities de um usuário autenticado combinando o papel
 * principal (Usuario.papel) com o vínculo de entregador (tb_entregadores).
 *
 * Por quê isto existe: uma conta CLIENTE pode também ser entregadora sem
 * deixar de ser CLIENTE (decisão de produto — uma pessoa continua
 * comprando no app normalmente depois de virar motoboy). Usuario.papel
 * continua sendo um valor único (CLIENTE, LOJISTA, FUNCIONARIO, ADMIN,
 * ENTREGADOR só existe historicamente no enum, mas nunca mais é atribuído a
 * ninguém — ver EntregadorService.cadastrar()), então ROLE_ENTREGADOR não
 * pode vir só de lá.
 *
 * Em vez disso, checamos aqui — uma vez por autenticação — se existe um
 * Entregador ativo vinculado ao usuário, e somamos ROLE_ENTREGADOR quando
 * existir. Isso mantém todo @PreAuthorize/hasAnyRole('ENTREGADOR', ...) já
 * escrito no projeto (EntregaController, EntregadorController,
 * ConversaEntregadorController) funcionando sem precisar tocar em nenhum
 * deles.
 *
 * Usado nos dois únicos lugares do projeto que constroem uma Authentication
 * manualmente: SecurityFilter (requisições REST) e
 * StompAuthChannelInterceptor (CONNECT do WebSocket).
 */
@Component
public class AutoridadesFactory {

    private final EntregadorRepository entregadorRepository;

    public AutoridadesFactory(EntregadorRepository entregadorRepository) {
        this.entregadorRepository = entregadorRepository;
    }

    public Collection<? extends GrantedAuthority> montar(Usuario usuario) {
        Collection<GrantedAuthority> authorities = new ArrayList<>(usuario.getAuthorities());

        boolean jaTemRoleEntregador = authorities.stream()
                .anyMatch(a -> "ROLE_ENTREGADOR".equals(a.getAuthority()));

        if (!jaTemRoleEntregador && entregadorRepository.existsByUsuarioIdAndAtivoTrue(usuario.getId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ENTREGADOR"));
        }

        return authorities;
    }
}
