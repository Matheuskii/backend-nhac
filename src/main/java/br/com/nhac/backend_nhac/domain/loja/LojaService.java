package br.com.nhac.backend_nhac.domain.loja;


import br.com.nhac.backend_nhac.domain.loja.dto.LojaCreateDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaResumoDTO;
import br.com.nhac.backend_nhac.domain.loja.dto.LojaDetalhesDTO;
import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.domain.usuario.UsuarioRepository;
import br.com.nhac.backend_nhac.exceptions.AcessoNegadoException;
import br.com.nhac.backend_nhac.exceptions.IdNaoEncontradoException;
import br.com.nhac.backend_nhac.exceptions.RegraDeNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class LojaService {

    private final LojaRepository lojaRepository;
    private final UsuarioRepository usuarioRepository;


    public LojaService(LojaRepository lojaRepository, UsuarioRepository usuarioRepository){
        this.lojaRepository = lojaRepository;
        this.usuarioRepository = usuarioRepository;
    }


    public Page<LojaResumoDTO> obterLojasPaginadas(String nome, Double lat, Double lng, Double raio, int page, int size){
        Pageable paginacao = PageRequest.of(page, size);

        Page<Loja> lojas;
        if (nome == null && lat == null && lng == null) {
            lojas = lojaRepository.findByIsAbertoTrue(paginacao);
        } else {
            lojas = lojaRepository.buscarLojasComFiltros(nome, lat, lng, raio, paginacao);
        }

        return lojas.map(LojaResumoDTO::new);
    }

    public LojaDetalhesDTO obterLojaId(String id){

        Loja loja = lojaRepository.findByIdAndIsAbertoTrue(id)
                .orElseThrow(() -> new IdNaoEncontradoException("A loja com o id: " + id + " não foi encontrada."));

        return new LojaDetalhesDTO(loja);


    }
    @Transactional
    public LojaResumoDTO criarLoja(LojaCreateDTO dto, Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            throw new AcessoNegadoException("É necessário estar autenticado para cadastrar uma loja.");
        }

        // Verificar se o usuário já possui uma loja cadastrada (A1 - P0)
        if (lojaRepository.findByUsuarioId(usuarioLogado.getId()).isPresent()) {
            throw new RegraDeNegocioException("Você já possui uma loja cadastrada.");
        }

        long totalLojas = contarLojasCadastradas();
        String novoId = String.format("loja_%04d", totalLojas + 1);

        Loja novaLoja = dto.toEntity();
        novaLoja.setId(novoId);
        novaLoja.setUsuarioId(usuarioLogado.getId());

        if (usuarioLogado.getPapel() == Papel.CLIENTE) {
            usuarioLogado.setPapel(Papel.LOJISTA);
            usuarioRepository.save(usuarioLogado);
        }

        Loja lojaSalva = lojaRepository.save(novaLoja);
        return new LojaResumoDTO(lojaSalva);
    }

    public long contarLojasCadastradas() {
        return lojaRepository.count();
    }

    public br.com.nhac.backend_nhac.domain.loja.dto.CalcularFreteResponseDTO calcularFrete(String lojaId, br.com.nhac.backend_nhac.domain.loja.dto.CalcularFreteRequestDTO dto) {
        Loja loja = lojaRepository.findByIdAndIsAbertoTrue(lojaId)
                .orElseThrow(() -> new IdNaoEncontradoException("A loja com o id: " + lojaId + " não foi encontrada ou está fechada."));

        // Lógica simulada de frete: 
        // 5.00 fixos + 1.50 (simulando que está perto)
        java.math.BigDecimal frete = new java.math.BigDecimal("6.50");
        Integer tempo = 45;

        // Se a loja tiver uma taxa base configurada, a gente pode considerar somá-la
        if (loja.getDadosOperacionais() != null && loja.getDadosOperacionais().getTaxaEntregaBase() != null) {
            frete = loja.getDadosOperacionais().getTaxaEntregaBase().add(new java.math.BigDecimal("1.50"));
        }

        return new br.com.nhac.backend_nhac.domain.loja.dto.CalcularFreteResponseDTO(frete, tempo);
    }
}
