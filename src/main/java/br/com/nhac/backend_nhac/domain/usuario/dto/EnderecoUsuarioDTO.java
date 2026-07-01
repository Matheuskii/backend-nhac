package br.com.nhac.backend_nhac.domain.usuario.dto;

import br.com.nhac.backend_nhac.domain.usuario.EnderecoUsuario;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Objeto de transferência de dados que representa um endereço de entrega do usuário")
public record EnderecoUsuarioDTO(
        @Schema(description = "ID único do endereço", example = "end_987654")
        String id,

        @Schema(description = "Nome da rua, avenida ou logradouro", example = "Avenida Paulista")
        @NotBlank(message = "A rua é obrigatória.")
        String rua,

        @Schema(description = "Número do imóvel", example = "1000")
        @NotBlank(message = "O número é obrigatório.")
        String numero,

        @Schema(description = "Bairro da localização", example = "Bela Vista")
        @NotBlank(message = "O bairro é obrigatório.")
        String bairro,

        @Schema(description = "Cidade do endereço", example = "São Paulo")
        @NotBlank(message = "A cidade é obrigatória.")
        String cidade,

        @Schema(description = "Sigla do estado com 2 caracteres", example = "SP")
        @NotBlank(message = "O estado é obrigatório.")
        @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 caracteres (ex: SP).")
        String estado,

        @Schema(description = "CEP formatado", example = "01310-100")
        @NotBlank(message = "O CEP é obrigatório.")
        @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato XXXXX-XXX.")
        String cep,

        @Schema(description = "Complemento opcional do endereço", example = "Apto 42, Bloco B")
        String complemento,

        @Schema(description = "Define se este é o endereço padrão de entrega do usuário", example = "true")
        boolean isPadrao
) {

    public EnderecoUsuario toEntity(Usuario usuario) {
        String idGerado = (this.id() != null && !this.id().isBlank()) ? this.id() : UUID.randomUUID().toString();
        return new EnderecoUsuario(
                idGerado,
                usuario,
                this.rua(),
                this.numero(),
                this.bairro(),
                this.cidade(),
                this.estado(),
                this.cep(),
                this.complemento(),
                this.isPadrao()
        );
    }
}