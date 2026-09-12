package br.com.nhac.backend_nhac.domain.painel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FaturamentoDiaDTO(LocalDate data, BigDecimal valor) {
}
