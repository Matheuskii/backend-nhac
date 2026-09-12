package br.com.nhac.backend_nhac.domain.lojista.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuncionarioDTO {
    private String id;
    private String nome;
    private String email;
    private String telefone;
    private String cargo;
    private boolean ativo;
    private String lojaId;
    private String lojaNome;

    public FuncionarioDTO() {}
}
