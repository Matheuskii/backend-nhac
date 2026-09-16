package br.com.nhac.backend_nhac.domain.upload;

import br.com.nhac.backend_nhac.domain.upload.dto.UploadResponseDTO;
import br.com.nhac.backend_nhac.exceptions.ErroPadraoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/uploads")
@Tag(name = "Uploads", description = "Upload de imagens (loja e produtos) para o Firebase Storage")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Operation(summary = "Upload de imagem",
            description = "Recebe um arquivo de imagem (JPEG, PNG ou WEBP, até 5MB por padrão), sobe para o Firebase Storage "
                    + "e devolve a URL pública pronta para ser usada no campo imagemUrl de loja ou produto. "
                    + "Pensado para o painel web do lojista, que não tem SDK de storage próprio (diferente do app Flutter).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Upload realizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Arquivo ausente, formato não suportado, tamanho acima do limite ou pasta de destino inválida.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @PostMapping(value = "/imagem", consumes = "multipart/form-data")
    public ResponseEntity<UploadResponseDTO> uploadImagem(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "pasta", defaultValue = "produtos") String pasta) {

        String url = uploadService.enviarImagem(arquivo, pasta);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadResponseDTO(url));
    }
}
