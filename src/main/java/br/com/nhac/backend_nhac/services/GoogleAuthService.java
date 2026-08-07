package br.com.nhac.backend_nhac.services;


import br.com.nhac.backend_nhac.domain.auth.dto.LoginResponseDTO;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.CredenciaisInvalidasException;
import br.com.nhac.backend_nhac.infra.security.TokenService;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;

    @Value("${google.client.id}")
    private String googleClientId;

    public GoogleAuthService(UsuarioRepository usuarioRepository, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public LoginResponseDTO autenticarComGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String nome = (String) payload.get("name");
                String imagemUrl = (String) payload.get("picture");

                Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                        .orElseGet(() -> registrarNovoUsuarioGoogle(email, nome, imagemUrl));

                String tokenJwt = tokenService.gerarToken(usuario);

                return new LoginResponseDTO(tokenJwt, usuario.getId(), usuario.getNome());

            } else {
                throw new CredenciaisInvalidasException("Token do Google inválido ou expirado");
            }
        } catch (Exception e) {
            throw new CredenciaisInvalidasException("Erro ao validar token social:" + e.getMessage());
        }
    }


        private Usuario registrarNovoUsuarioGoogle(String email, String nome, String imagemUrl) {
            Usuario novoUsuario = new Usuario();
            novoUsuario.setId(UUID.randomUUID().toString());
            novoUsuario.setEmail(email);
            novoUsuario.setNome(nome != null ? nome : "Usuário Nhac");
            novoUsuario.setImagemUrl(imagemUrl);
            novoUsuario.setEnderecos(new ArrayList<>());

            novoUsuario.setTelefone("00000000000");

            novoUsuario.setSenha(null);

            return usuarioRepository.save(novoUsuario);

    }
}
