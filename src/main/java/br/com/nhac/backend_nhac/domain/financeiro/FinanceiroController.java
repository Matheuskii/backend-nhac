package br.com.nhac.backend_nhac.domain.financeiro;

import br.com.nhac.backend_nhac.domain.financeiro.dto.FinanceiroDTOs.FinanceiroDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lojista/financeiro")
@Tag(name = "Painel do Lojista - Financeiro", description = "Agregações financeiras por período (item 4.1 da spec). Tudo calculado sobre tb_pedidos/tb_itens_pedido, sem tabela nova.")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @Operation(summary = "Resumo financeiro por período", description = "periodo: HOJE, SETE_DIAS ou TRINTA_DIAS (padrão SETE_DIAS). Filtro 'personalizado' ainda não incluído — ver pergunta em aberto #9 da spec.")
    @GetMapping
    public ResponseEntity<FinanceiroDTO> obterFinanceiro(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestParam(required = false, defaultValue = "SETE_DIAS") PeriodoFinanceiro periodo) {
        return ResponseEntity.ok(financeiroService.obterFinanceiro(usuarioLogado, periodo));
    }
}
