package br.com.nhac.backend_nhac.domain.upload;

import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.infra.storage.FirebaseStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class UploadService {

    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private static final Map<String, String> EXTENSAO_POR_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final FirebaseStorageClient storageClient;

    @Value("${nhac.storage.max-tamanho-bytes:5242880}")
    private long tamanhoMaximoEmBytes;

    public UploadService(FirebaseStorageClient storageClient) {
        this.storageClient = storageClient;
    }

    private static final Set<String> PASTAS_PERMITIDAS = Set.of("lojas", "produtos");

    /**
     * Valida e envia uma imagem para o Firebase Storage.
     *
     * @param arquivo arquivo recebido no multipart/form-data
     * @param pasta   pasta de destino dentro do bucket, ex: "lojas" ou "produtos"
     * @return URL pública de download da imagem
     */
    public String enviarImagem(MultipartFile arquivo, String pasta) {
        if (pasta == null || !PASTAS_PERMITIDAS.contains(pasta)) {
            throw new RegraDeNegocioException("Pasta de destino inválida. Use 'lojas' ou 'produtos'.");
        }

        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraDeNegocioException("Nenhum arquivo foi enviado.");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            throw new RegraDeNegocioException(
                    "Formato de imagem não suportado. Envie um arquivo JPEG, PNG ou WEBP.");
        }

        if (arquivo.getSize() > tamanhoMaximoEmBytes) {
            throw new RegraDeNegocioException(
                    "Arquivo muito grande. O tamanho máximo permitido é " + (tamanhoMaximoEmBytes / (1024 * 1024)) + "MB.");
        }

        try {
            String extensao = EXTENSAO_POR_CONTENT_TYPE.get(contentType);
            return storageClient.upload(arquivo.getBytes(), pasta, extensao, contentType);
        } catch (IOException e) {
            throw new RegraDeNegocioException("Não foi possível ler o arquivo enviado. Tente novamente.");
        }
    }
}
