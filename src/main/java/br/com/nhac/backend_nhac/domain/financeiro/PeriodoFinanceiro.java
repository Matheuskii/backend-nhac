package br.com.nhac.backend_nhac.domain.financeiro;

/**
 * Pergunta em aberto #9 da spec: filtro "personalizado" (intervalo livre)
 * ainda não foi incluído aqui — só os três períodos fixos que a tela já usa.
 * Se for aprovado, dá pra adicionar como um quarto valor com inicio/fim
 * vindos de query params, sem quebrar os outros três.
 */
public enum PeriodoFinanceiro {
    HOJE,
    SETE_DIAS,
    TRINTA_DIAS
}
