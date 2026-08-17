package br.com.nhac.backend_nhac.domain.auth;

import br.com.nhac.backend_nhac.domain.auth.dto.EnviarCodigoSmsDTO;
import br.com.nhac.backend_nhac.domain.auth.dto.ValidarCodigoSmsDTO;
import br.com.nhac.backend_nhac.services.VerificacaoTelefoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/verificacao-telefone")
@Tag(name = "Verificação de Telefone", description = "Endpoints para envio e validação de códigos SMS (OTP)")
public class VerificacaoTelefoneController {

    private final VerificacaoTelefoneService verificacaoTelefoneService;

    public VerificacaoTelefoneController(VerificacaoTelefoneService verificacaoTelefoneService) {
        this.verificacaoTelefoneService = verificacaoTelefoneService;
    }

    @PostMapping("/enviar-codigo")
    @Operation(summary = "Envia um código OTP via SMS para o telefone informado")
    public ResponseEntity<Void> enviarCodigo(@RequestBody @Valid EnviarCodigoSmsDTO dto) {
        verificacaoTelefoneService.enviarCodigo(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validar-codigo")
    @Operation(summary = "Valida o código OTP informado. Se correto, marca o telefone do usuário como verificado")
    public ResponseEntity<Void> validarCodigo(@RequestBody @Valid ValidarCodigoSmsDTO dto) {
        verificacaoTelefoneService.validarCodigo(dto);
        return ResponseEntity.ok().build();
    }
}
