package br.com.nhac.backend_nhac.util;

import net.datafaker.Faker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeradorCatalogoInsano {

    private static final Faker faker = new Faker(new Locale("pt", "BR"));

    private static final int QUANTIDADE_LOJAS = 1000;
    private static final int PRODUTOS_POR_LOJA = 10;
    private static final int TAMANHO_LOTE = 500;

    private static final String[] CATEGORIAS_LOJA = {"Lanches", "Pizza", "Japonesa", "Brasileira", "Italiana", "Saudável", "Doces"};
    private static final String[] CATEGORIAS_PRODUTO = {"Prato Principal", "Acompanhamento", "Bebidas", "Sobremesas", "Combos"};

    private static final String HR_PADRAO = "11:00 - 23:00";
    private static final String HR_FECHADO = "Fechado";

    public static void main(String[] args) {
        Path caminhoArquivo = Path.of("src/main/resources/db/migration/V998__populando_lojas_e_produtos.sql");

        System.out.println("🔥 Iniciando geração do Catálogo Insano...");
        long tempoInicio = System.currentTimeMillis();

        try {
            Files.createDirectories(caminhoArquivo.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(caminhoArquivo)) {
                List<String> lojasGeradas = gerarLojas(writer);
                System.out.println("✅ Lojas geradas com sucesso!");

                int totalProdutos = gerarProdutos(writer, lojasGeradas);
                System.out.println("✅ " + totalProdutos + " Produtos gerados e vinculados!");
            }
        } catch (IOException e) {
            System.err.println("❌ Erro fatal ao gerar o arquivo SQL: " + e.getMessage());
            e.printStackTrace();
        }

        long tempoFim = System.currentTimeMillis();
        System.out.println("🚀 Arquivo SQL gerado em " + (tempoFim - tempoInicio) + "ms!");
    }

    private static List<String> gerarLojas(BufferedWriter writer) throws IOException {
        List<String> lojasGeradas = new ArrayList<>(QUANTIDADE_LOJAS);

        String insertPrefix = "INSERT INTO tb_lojas (id, nome, descricao, categoria, imagem_url, is_aberto, " +
                "taxa_entrega_base, tempo_entrega_min, tempo_entrega_max, avaliacao_media, total_avaliacoes, " +
                "end_rua, end_numero, end_cidade, end_estado, end_cep, " +
                "geo_lat, geo_lng, geo_geohash, " +
                "horario_domingo, horario_segunda, horario_terca, horario_quarta, horario_quinta, horario_sexta, horario_sabado) VALUES \n";

        writer.write("-- ==========================================\n");
        writer.write("-- INSERÇÃO EM LOTE DE " + QUANTIDADE_LOJAS + " LOJAS\n");
        writer.write("-- ==========================================\n");

        StringBuilder lote = new StringBuilder();
        int count = 0;

        for (int i = 1; i <= QUANTIDADE_LOJAS; i++) {
            if (count == 0) lote.append(insertPrefix);

            String id = String.format("loja_%04d", i);
            lojasGeradas.add(id);

            lote.append(formatarValoresLoja(id)).append(",\n");
            count++;

            if (count == TAMANHO_LOTE || i == QUANTIDADE_LOJAS) {
                lote.setLength(lote.length() - 2);
                lote.append(";\n\n");
                writer.write(lote.toString());
                lote.setLength(0);
                count = 0;
            }
        }
        return lojasGeradas;
    }

    private static int gerarProdutos(BufferedWriter writer, List<String> lojasGeradas) throws IOException {
        String insertPrefix = "INSERT INTO tb_produtos (id, loja_id, nome, descricao, preco, categoria_menu, imagem_url, percentual_desconto, estoque) VALUES \n";

        writer.write("-- ==========================================\n");
        writer.write("-- 2. PRODUTOS (50 por Loja = " + (QUANTIDADE_LOJAS * PRODUTOS_POR_LOJA) + " no total)\n");
        writer.write("-- ==========================================\n");

        StringBuilder lote = new StringBuilder();
        int count = 0;
        int totalProdutos = 0;
        int totalItems = lojasGeradas.size() * PRODUTOS_POR_LOJA;

        for (String lojaId : lojasGeradas) {
            for (int p = 1; p <= PRODUTOS_POR_LOJA; p++) {
                if (count == 0) lote.append(insertPrefix);

                lote.append(formatarValoresProduto(lojaId, p)).append(",\n");
                count++;
                totalProdutos++;

                if (count == TAMANHO_LOTE || totalProdutos == totalItems) {
                    lote.setLength(lote.length() - 2);
                    lote.append(";\n\n");
                    writer.write(lote.toString());
                    lote.setLength(0);
                    count = 0;
                }
            }
        }
        return totalProdutos;
    }

    private static String formatarValoresLoja(String id) {
        String nome = sql(faker.restaurant().name());
        String descricao = sql(faker.restaurant().description());
        String categoria = sql(CATEGORIAS_LOJA[faker.random().nextInt(CATEGORIAS_LOJA.length)]);
        String imagemUrl = sql("https://picsum.photos/seed/" + id + "/400");
        boolean isAberto = faker.bool().bool();

        String taxaEntrega = String.format(Locale.US, "%.2f", faker.number().randomDouble(2, 2, 15));
        int tempoMin = faker.number().numberBetween(15, 30);
        int tempoMax = tempoMin + faker.number().numberBetween(10, 25);
        String avaliacaoMedia = String.format(Locale.US, "%.1f", faker.number().randomDouble(1, 3, 5));
        int totalAvaliacoes = faker.number().numberBetween(0, 2000);

        String rua = sql(faker.address().streetName());
        String numero = sql(faker.address().buildingNumber());
        String cidade = sql(faker.address().cityName());
        String estado = sql(faker.address().stateAbbr());
        String cep = sql(faker.address().zipCode().replaceAll("[^0-9]", ""));

        String lat = faker.address().latitude().replace(",", ".");
        String lng = faker.address().longitude().replace(",", ".");

        return String.format(
                "(%s, %s, %s, %s, %s, %b, %s, %d, %d, %s, %d, %s, %s, %s, %s, %s, %s, %s, NULL, %s, %s, %s, %s, %s, %s, %s)",
                sql(id), nome, descricao, categoria, imagemUrl, isAberto, taxaEntrega, tempoMin, tempoMax, avaliacaoMedia, totalAvaliacoes,
                rua, numero, cidade, estado, cep, lat, lng,
                sql(HR_PADRAO), sql(HR_FECHADO), sql(HR_PADRAO), sql(HR_PADRAO), sql(HR_PADRAO), sql(HR_PADRAO), sql(HR_PADRAO)
        );
    }

    private static String formatarValoresProduto(String lojaId, int p) {
        String prodId = String.format("prod_%s_%02d", lojaId, p);
        String nome = sql(faker.food().dish());
        String descricao = sql(faker.food().ingredient() + " com temperos especiais da casa.");
        String preco = String.format(Locale.US, "%.2f", faker.number().randomDouble(2, 10, 80));
        String catMenu = sql(CATEGORIAS_PRODUTO[faker.random().nextInt(CATEGORIAS_PRODUTO.length)]);
        String imagemUrl = sql("https://picsum.photos/seed/" + prodId + "/200");
        int desconto = faker.number().numberBetween(1, 10) > 8 ? faker.number().numberBetween(10, 30) : 0;
        int estoque = faker.number().numberBetween(10, 500);

        return String.format(
                "(%s, %s, %s, %s, %s, %s, %s, %d, %d)",
                sql(prodId), sql(lojaId), nome, descricao, preco, catMenu, imagemUrl, desconto, estoque
        );
    }


    private static String sql(String value) {
        if (value == null) return "NULL";
        return "'" + value.replace("'", "''") + "'";
    }
}