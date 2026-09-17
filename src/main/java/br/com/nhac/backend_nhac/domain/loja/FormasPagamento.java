package br.com.nhac.backend_nhac.domain.loja;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Formas de pagamento aceitas pela loja.
 * Embeddable para ser usado na entidade Loja.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormasPagamento {

    @Column(name = "aceita_dinheiro", nullable = false)
    private Boolean aceitaDinheiro = true;

    @Column(name = "aceita_credito", nullable = false)
    private Boolean aceitaCredito = true;

    @Column(name = "aceita_debito", nullable = false)
    private Boolean aceitaDebito = true;

    @Column(name = "aceita_pix", nullable = false)
    private Boolean aceitaPix = true;

    @Column(name = "aceita_vale_refeicao", nullable = false)
    private Boolean aceitaValeRefeicao = false;

    @Column(name = "aceita_vale_alimentacao", nullable = false)
    private Boolean aceitaValeAlimentacao = false;
}
