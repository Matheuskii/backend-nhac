package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class CampoObrigatorioFaltandoException extends NhacException {
    
    public CampoObrigatorioFaltandoException(String campo) {
        super("Campo obrigatório não preenchido: " + campo, 
              ErrorCode.CAMPO_OBRIGATORIO_FALTANDO, 
              Map.of("campo", campo));
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
