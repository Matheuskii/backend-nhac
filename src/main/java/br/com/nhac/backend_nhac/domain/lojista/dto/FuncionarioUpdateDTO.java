package br.com.nhac.backend_nhac.domain.lojista.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuncionarioUpdateDTO {

    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Size(max = 50, message = "Cargo deve ter no máximo 50 caracteres")
    private String cargo;
}
