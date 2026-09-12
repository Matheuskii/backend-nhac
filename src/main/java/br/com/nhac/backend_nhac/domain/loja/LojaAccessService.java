package br.com.nhac.backend_nhac.domain.loja;

import br.com.nhac.backend_nhac.domain.usuario.Papel;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.exceptions.LojaNaoEncontradaException;
import org.springframework.stereotype.Service;

/**
 * Único ponto de resolução de "qual loja este usuário logado pode gerenciar".
 * Antes desta classe, cada service comparava produto.getLoja().getUsuarioId()
 * diretamente com o id do usuário logado — o que só funciona para o DONO da
 * loja. Com a introdução de funcionários (Papel.FUNCIONARIO), passamos a ter
 * dois jeitos de estar autorizado a mexer numa loja:
 *   - ser o dono (tb_lojas.usuario_id == usuario.id)
 *   - ser funcionário vinculado a ela (usuario.lojaVinculadaId == loja.id)
 *
 * ADMIN não passa por aqui: cada service continua tratando o bypass de ADMIN
 * separadamente, como já fazia antes desta mudança.
 */
@Service
public class LojaAccessService {

    private final LojaRepository lojaRepository;

    public LojaAccessService(LojaRepository lojaRepository) {
        this.lojaRepository = lojaRepository;
    }

    /**
     * Retorna a loja que o usuário logado (dono ou funcionário) pode gerenciar.
     * Lança LojaNaoEncontradaException se ele não tiver loja associada de
     * nenhuma das duas formas (ex.: CLIENTE, ou FUNCIONARIO órfão).
     */
    public Loja obterLojaAcessivel(Usuario usuarioLogado) {
        if (usuarioLogado.getPapel() == Papel.FUNCIONARIO) {
            if (usuarioLogado.getLojaVinculadaId() == null) {
                throw new LojaNaoEncontradaException();
            }
            return lojaRepository.findById(usuarioLogado.getLojaVinculadaId())
                    .orElseThrow(LojaNaoEncontradaException::new);
        }

        return lojaRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(LojaNaoEncontradaException::new);
    }

    /**
     * Confere se o usuário logado (dono, funcionário, ou ADMIN) tem acesso à
     * loja informada. Uso típico: checagem de ownership em atualizarProduto,
     * desativarProduto, atualizarStatus de pedido, etc.
     */
    public boolean temAcessoALoja(Usuario usuarioLogado, String lojaId) {
        if (usuarioLogado.getPapel() == Papel.ADMIN) {
            return true;
        }
        try {
            return obterLojaAcessivel(usuarioLogado).getId().equals(lojaId);
        } catch (LojaNaoEncontradaException e) {
            return false;
        }
    }
}
