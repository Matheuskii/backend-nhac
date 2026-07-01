package br.com.nhac.backend_nhac.domain.usuario;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Usuario {

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
}

