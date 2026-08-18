package br.com.nhac.backend_nhac.domain.usuario;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "tb_usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

    @Id
    @Column(updatable = false, nullable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "imagem_url", columnDefinition = "TEXT")
    private String imagemUrl;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnderecoUsuario> enderecos = new ArrayList<>();

    @Column(length = 255)
    private String senha;


    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    @Column(name = "telefone_verificado", nullable = false)
    private boolean telefoneVerificado = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Papel papel = Papel.CLIENTE;

    @Column(nullable = false)
    private boolean ativo = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.papel.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo;
    }
}

