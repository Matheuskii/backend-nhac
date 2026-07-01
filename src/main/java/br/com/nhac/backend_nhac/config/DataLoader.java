package br.com.nhac.backend_nhac.config;

import br.com.nhac.backend_nhac.domain.loja.*;
import br.com.nhac.backend_nhac.domain.produto.Produto;
import br.com.nhac.backend_nhac.repositories.LojaRepository;
import br.com.nhac.backend_nhac.repositories.ProdutoRepository;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final LojaRepository lojaRepository;

    private final ProdutoRepository produtoRepository;

    public DataLoader(LojaRepository lojaRepository, ProdutoRepository produtoRepository) {
        this.lojaRepository = lojaRepository;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (lojaRepository.count() > 0) {
            log.info("Banco de dados já contém dados. DataLoader ignorado.");
            return;
        }

        log.info("Iniciando o cadastro de dados de teste (DataLoader)...");

        Loja lojaSushi = new Loja();
        lojaSushi.setId("loja-001");
        lojaSushi.setNome("Pizzaria Nostra");
        lojaSushi.setDescricao("A melhor pizza da cidade feita no forno a lenha, com ingredientes selecionados.");
        lojaSushi.setCategoria("Pizzaria");
        lojaSushi.setImagemUrl("https://exemplo.com/imagens/pizzaria.jpg");
        lojaSushi.setAberto(true);

        DadosOperacionais dados1 = new DadosOperacionais();
        dados1.setAvaliacaoMedia(4.8f);
        dados1.setTaxaEntregaBase(new BigDecimal("5.50"));
        dados1.setTempoEntregaMin(30);
        dados1.setTempoEntregaMax(45);
        dados1.setTotalAvaliacoes(320);
        lojaSushi.setDadosOperacionais(dados1);

        EnderecoLoja end1 = new EnderecoLoja();
        end1.setRua("Avenida das Pizzas");
        end1.setNumero("123");
        end1.setCidade("São Paulo");
        end1.setEstado("SP");
        end1.setCep("01000-000");
        lojaSushi.setEndereco(end1);

        GeoLocalizacao geo1 = new GeoLocalizacao();
        geo1.setGeoLat(-23.550520);
        geo1.setGeoLng(-46.633308);
        geo1.setGeoHash("6gyf4");
        lojaSushi.setGeoLocalizacao(geo1);

        HorariosFuncionamento hor1 = new HorariosFuncionamento();
        hor1.setDomingo("18:00-23:59");
        hor1.setSegunda("Fechado");
        hor1.setTerca("18:00-23:00");
        hor1.setQuarta("18:00-23:00");
        hor1.setQuinta("18:00-23:00");
        hor1.setSexta("18:00-23:59");
        hor1.setSabado("18:00-23:59");
        lojaSushi.setHorariosFuncionamento(hor1);


        Loja lojaHamburguer = new Loja();
        lojaHamburguer.setId("loja-002");
        lojaHamburguer.setNome("Hamburgueria do Zé");
        lojaHamburguer.setDescricao("Hambúrguer artesanal com blend especial de carnes e pão brioche.");
        lojaHamburguer.setCategoria("Lanches");
        lojaHamburguer.setImagemUrl("https://exemplo.com/imagens/hamburgueria.jpg");
        lojaHamburguer.setAberto(false);

        DadosOperacionais dados2 = new DadosOperacionais();
        dados2.setAvaliacaoMedia(4.5f);
        dados2.setTaxaEntregaBase(new BigDecimal("7.00"));
        dados2.setTempoEntregaMin(20);
        dados2.setTempoEntregaMax(40);
        dados2.setTotalAvaliacoes(150);
        lojaHamburguer.setDadosOperacionais(dados2);

        EnderecoLoja end2 = new EnderecoLoja();
        end2.setRua("Rua dos Lanches");
        end2.setNumero("45");
        end2.setCidade("Rio de Janeiro");
        end2.setEstado("RJ");
        end2.setCep("20000-123");
        lojaHamburguer.setEndereco(end2);

        GeoLocalizacao geo2 = new GeoLocalizacao();
        geo2.setGeoLat(-22.906847);
        geo2.setGeoLng(-43.172896);
        geo2.setGeoHash("7h2m1");
        lojaHamburguer.setGeoLocalizacao(geo2);

        HorariosFuncionamento hor2 = new HorariosFuncionamento();
        hor2.setDomingo("11:00-22:00");
        hor2.setSegunda("11:00-22:00");
        hor2.setTerca("11:00-22:00");
        hor2.setQuarta("11:00-22:00");
        hor2.setQuinta("11:00-22:00");
        hor2.setSexta("11:00-23:00");
        hor2.setSabado("11:00-23:00");
        lojaHamburguer.setHorariosFuncionamento(hor2);

        lojaRepository.saveAll(List.of(lojaSushi, lojaHamburguer));
        log.info("Lojas cadastradas com sucesso!");

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
        batata.setNome("Batata Frita Grande");
        batata.setDescricao("Porção generosa de batatas crocantes");
        batata.setPreco(new BigDecimal("15.00"));
        batata.setCategoriaMenu("Acompanhamentos");

        produtoRepository.saveAll(List.of(hossomaki, temaki, whopper, batata));
        log.info("Produtos cadastrados com sucesso! O DataLoader terminou o seu trabalho.");
    }
}
