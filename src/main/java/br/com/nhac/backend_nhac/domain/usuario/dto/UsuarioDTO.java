package br.com.nhac.backend_nhac.domain.usuario.dto;

import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;

@Schema(description = "Objeto de transferência de dados que representa o perfil do usuário")
public record UsuarioDTO(
        @Schema(description = "ID único do usuário (UID vindo da autenticação)", example = "usr_abc123xyz")
        @NotBlank(message = "O ID do usuário é obrigatório.")
        String id,

        @Schema(description = "Nome completo do usuário", example = "Matheus Alves")
        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @Schema(description = "Endereço de e-mail do usuário", example = "matheus@nhac.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        String email,

        @Schema(description = "Número de telefone ou celular", example = "11999998888")
        @NotBlank(message = "O telefone é obrigatório.")
        String telefone,

        @Schema(description = "URL da foto de perfil armazenada no servidor/nuvem", example = "https://images.nhac.com/profiles/usr_abc.jpg")
        String imagemUrl
) {

    public Usuario toEntity() {
        return new Usuario(
                this.id(),
                this.nome(),
                this.email(),
                this.telefone(),
                this.imagemUrl(),
                new ArrayList<>()
        );
    }
}