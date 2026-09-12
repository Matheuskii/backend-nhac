package br.com.nhac.backend_nhac.domain.usuario.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Edição de funcionário. Propositalmente NÃO tem e-mail nem senha — trocar
 * e-mail (identidade de login) e redefinir senha ficam fora desta rota; senha
 * já é resolvida pelo fluxo padrão de "esqueci minha senha" (o funcionário é
 * um Usuario comum, então isso já funciona sem nenhuma mudança extra).
 */
public record FuncionarioUpdateDTO(
        @NotBlank String nome,
        String telefone,
        @NotBlank String cargo,
        String imagemUrl
) {
}
