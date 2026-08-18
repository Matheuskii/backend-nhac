package br.com.nhac.backend_nhac.services;

import br.com.nhac.backend_nhac.domain.usuario.EnderecoUsuario;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.dto.EnderecoUsuarioDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioAtualizarDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioCreateDTO;
import br.com.nhac.backend_nhac.domain.usuario.dto.UsuarioResponseDTO;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.CredenciaisInvalidasException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import br.com.nhac.backend_nhac.repositories.EnderecoUsuarioRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // 🔴 NOVO IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EnderecoUsuarioRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EnderecoUsuarioRepository enderecoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO buscarUsuario(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("Usuário não encontrado."));
        return new UsuarioResponseDTO(usuario);
    }

    @Transactional
    public void salvarUsuario(UsuarioCreateDTO dto) {
        Usuario usuario = dto.toEntity();

        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarUsuarioParcial(String id, UsuarioAtualizarDTO dados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("Usuário não encontrado."));

        if(dados.nome() != null)
            usuario.setNome(dados.nome());
        if(dados.email() != null)
            usuario.setEmail(dados.email());
        if(dados.telefone() != null)
            usuario.setTelefone(dados.telefone());
        if(dados.imagemUrl() != null)
            usuario.setImagemUrl(dados.imagemUrl());

        usuarioRepository.save(usuario);
    }

    public List<EnderecoUsuarioDTO> listarEnderecos(String usuarioId) {
        return enderecoRepository.findByUsuarioId(usuarioId).stream()
                .map(end -> new EnderecoUsuarioDTO(
                        end.getId(), end.getRua(), end.getNumero(), end.getBairro(),
                        end.getCidade(), end.getEstado(), end.getCep(), end.getComplemento(), end.isPadrao()
                )).collect(Collectors.toList());
    }


    private void garantirUnicoEnderecoPadrao(String usuarioId, String enderecoIdApreservar) {
        List<EnderecoUsuario> enderecos = enderecoRepository.findByUsuarioId(usuarioId);
        for (EnderecoUsuario e : enderecos) {
            if (!e.getId().equals(enderecoIdApreservar) && e.isPadrao()) {
                e.setPadrao(false);
                enderecoRepository.save(e);
            }
        }
    }

    @Transactional
    public void adicionarEndereco(String usuarioId, EnderecoUsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IdNaoEncontradoException("Usuário não encontrado."));
        EnderecoUsuario endereco = dto.toEntity(usuario);

        if (endereco.isPadrao()) {
            garantirUnicoEnderecoPadrao(usuarioId, endereco.getId());
        }

        enderecoRepository.save(endereco);
    }

    @Transactional
    public void atualizarEndereco(String usuarioId, String enderecoId, EnderecoUsuarioDTO dto) {
        EnderecoUsuario endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Endereço não encontrado."));

        if (!endereco.getUsuario().getId().equals(usuarioId)) {
            throw new CredenciaisInvalidasException("Acesso negado a este endereço.");
        }

        endereco.setRua(dto.rua());
        endereco.setNumero(dto.numero());
        endereco.setBairro(dto.bairro());
        endereco.setCidade(dto.cidade());
        endereco.setEstado(dto.estado());
        endereco.setCep(dto.cep());
        endereco.setComplemento(dto.complemento());
        endereco.setPadrao(dto.isPadrao());

        if (endereco.isPadrao()) {
            garantirUnicoEnderecoPadrao(usuarioId, enderecoId);
        }

        enderecoRepository.save(endereco);
    }

    @Transactional
    public void removerEndereco(String usuarioId, String enderecoId) {
        EnderecoUsuario endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new IdNaoEncontradoException("Endereço não encontrado."));

        if (!endereco.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Acesso negado a este endereço.");
        }

        enderecoRepository.delete(endereco);
    }



    public void validarPropriedade(String idNaUrl, Usuario usuarioLogado) {
        if (!idNaUrl.equals(usuarioLogado.getId())) {
            throw new AcessoNegadoException("Acesso negado: não tem permissão para aceder ou modificar os dados de outro utilizador.");
        }
    }

    @Transactional
    public void desativarUsuario(String idNaUrl, Usuario usuarioLogado) {
        if (!usuarioLogado.getPapel().equals(br.com.nhac.backend_nhac.domain.usuario.Papel.ADMIN) && !idNaUrl.equals(usuarioLogado.getId())) {
            throw new AcessoNegadoException("Acesso negado: apenas administradores ou o próprio usuário podem desativar a conta.");
        }

        Usuario usuario = usuarioRepository.findById(idNaUrl)
                .orElseThrow(() -> new IdNaoEncontradoException("Usuário não encontrado."));

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarSenha(String telefone, String novaSenha) {
        Usuario usuario = usuarioRepository.findByTelefone(telefone)
                .orElseThrow(() -> new IdNaoEncontradoException("Nenhum usuário cadastrado com esse telefone."));

        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioException("Usuário inativo. Não é possível alterar a senha.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}