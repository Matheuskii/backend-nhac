package br.com.nhac.backend_nhac.config;

import br.com.nhac.backend_nhac.domain.loja.*;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.domain.usuario.Usuario;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import br.com.nhac.backend_nhac.repositories.UsuarioRepository;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final LojaRepository lojaRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    public DataLoader(LojaRepository lojaRepository,
                      ProdutoRepository produtoRepository,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder) {
        this.lojaRepository = lojaRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {


        if (usuarioRepository.count() == 0) {
            log.info("Banco de dados sem usuários. Cadastrando usuários padrão para teste...");

            Usuario cliente = new Usuario();
            cliente.setId("firebase_user_matheus123");
            cliente.setNome("Matheus Alves Cliente");
            cliente.setEmail("matheus@nhac.com");
            cliente.setTelefone("11999998888");
            cliente.setImagemUrl("https://picsum.photos/seed/matheus/200");
            cliente.setSenha(passwordEncoder.encode("senha123"));
            cliente.setEnderecos(new ArrayList<>());
            usuarioRepository.save(cliente);

            Usuario motoca = new Usuario();
            motoca.setId("firebase_user_carlos456");
            motoca.setNome("Carlos Motoca");
            motoca.setEmail("carlos@nhac.com");
            motoca.setTelefone("11988887777");
            motoca.setImagemUrl("https://picsum.photos/seed/carlos/200");
            motoca.setSenha(passwordEncoder.encode("motoca123"));
            motoca.setEnderecos(new ArrayList<>());
            usuarioRepository.save(motoca);

            log.info("Usuários de teste criados com sucesso! (Logins: matheus@nhac.com / carlos@nhac.com)");
        }


        if (lojaRepository.count() > 0) {
            log.info("Banco de dados já contém lojas cadastrados. Seção de Lojas do DataLoader ignorada.");
            return;
        }

        log.info("Iniciando o cadastro de lojas e produtos de teste...");

        Loja lojaSushi = new Loja();
        lojaSushi.setId("loja_sushi_001");
        lojaSushi.setNome("Nhac Sushi Premium");
        lojaSushi.setDescricao("O melhor da culinária japonesa tradicional e contemporânea");
        lojaSushi.setCategoria("Japonesa");
        lojaSushi.setImagemUrl("https://picsum.photos/seed/sushi/400/400");
        lojaSushi.setAberto(true);

        DadosOperacionais dadosSushi = new DadosOperacionais();
        dadosSushi.setAvaliacaoMedia(4.8f);
        dadosSushi.setTotalAvaliacoes(154);
        lojaSushi.setDadosOperacionais(dadosSushi);

        EnderecoLoja endSushi = new EnderecoLoja();
        endSushi.setRua("Alameda dos Autores");
        endSushi.setNumero("100");
        endSushi.setCidade("São Paulo");
        endSushi.setEstado("SP");
        endSushi.setCep("01000-000");
        lojaSushi.setEndereco(endSushi);

        Loja lojaHamburguer = new Loja();
        lojaHamburguer.setId("loja_burger_002");
        lojaHamburguer.setNome("Nhac Burger & Fries");
        lojaHamburguer.setDescricao("Hambúrgueres artesanais grelhados no fogo como você nunca viu");
        lojaHamburguer.setCategoria("Lanches");
        lojaHamburguer.setImagemUrl("https://picsum.photos/seed/burger/400/400");
        lojaHamburguer.setAberto(true);

        DadosOperacionais dadosBurger = new DadosOperacionais();
        dadosBurger.setAvaliacaoMedia(4.6f);
        dadosBurger.setTotalAvaliacoes(98);
        lojaHamburguer.setDadosOperacionais(dadosBurger);

        EnderecoLoja endBurger = new EnderecoLoja();
        endBurger.setRua("Avenida dos Sabores");
        endBurger.setNumero("550");
        endBurger.setCidade("São Paulo");
        endBurger.setEstado("SP");
        endBurger.setCep("02000-000");
        lojaHamburguer.setEndereco(endBurger);

        lojaRepository.saveAll(List.of(lojaSushi, lojaHamburguer));

        Produto hossomaki = new Produto();
        hossomaki.setId("prod_sushi_001");
        hossomaki.setLoja(lojaSushi);
        hossomaki.setNome("Hossomaki de Salmão");
        hossomaki.setDescricao("Rolinho clássico de salmão");
        hossomaki.setPreco(new BigDecimal("25.50"));
        hossomaki.setCategoriaMenu("Sushi");

        Produto temaki = new Produto();
        temaki.setId("prod_sushi_002");
        temaki.setLoja(lojaSushi);
        temaki.setNome("Temaki de Atum");
        temaki.setDescricao("Cone recheado com atum fresco");
        temaki.setPreco(new BigDecimal("32.00"));
        temaki.setCategoriaMenu("Temaki");

        Produto whopper = new Produto();
        whopper.setId("prod_burger_001");
        whopper.setLoja(lojaHamburguer);
        whopper.setNome("Whopper Duplo");
        whopper.setDescricao("Dois hambúrgueres bovinos, queijo e bacon");
        whopper.setPreco(new BigDecimal("45.90"));
        whopper.setCategoriaMenu("Lanches");

        Produto batata = new Produto();
        batata.setId("prod_burger_002");
        batata.setLoja(lojaHamburguer);
        batata.setNome("Batata Frita Tradicional");
        batata.setDescricao("Batatas crocantes com sal e alecrim");
        batata.setPreco(new BigDecimal("18.00"));
        batata.setCategoriaMenu("Acompanhamentos");

        produtoRepository.saveAll(List.of(hossomaki, temaki, whopper, batata));

        log.info("Cadastro de dados de teste (DataLoader) finalizado com sucesso!");
    }
}