package br.com.nhac.backend_nhac.util;


import net.datafaker.Faker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeradorCatalogoInsano {

    private static final Faker faker = new Faker(new Locale("pt", "BR"));

    private static final int QUANTIDADE_LOJAS = 1000;
    private static final int PRODUTOS_POR_LOJA = 10;
    private static final int TAMANHO_LOTE = 500;

    public static void main(String[] args) {
        String caminhoArquivo = "src/main/resources/db/migration/V998__populando_lojas_e_produtos.sql";
        System.out.println("🔥 Iniciando geração do Catálogo Insano...");
        long tempoInicio = System.currentTimeMillis();

        String[] categoriasLoja = {"Lanches", "Pizza", "Japonesa", "Brasileira", "Italiana", "Saudável", "Doces"};
        String[] categoriasProduto = {"Prato Principal", "Acompanhamento", "Bebidas", "Sobremesas", "Combos"};

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoArquivo))) {

            List<String> lojasGeradas = new ArrayList<>();

            // ==========================================
            // 1. GERAR LOJAS (IDs: loja_0001, loja_0002...)
            // ==========================================
            writer.write("-- ==========================================\n");
            writer.write("-- INSERÇÃO EM LOTE DE " + QUANTIDADE_LOJAS + " LOJAS\n");
            writer.write("-- ==========================================\n");

            for (int i = 1; i <= QUANTIDADE_LOJAS; i += TAMANHO_LOTE) {
                StringBuilder loteLojas = new StringBuilder();

                // 🔴 COLUNAS ATUALIZADAS CONFORME O TEU BANCO DE DADOS
                loteLojas.append("INSERT INTO tb_lojas (id, nome, descricao, categoria, imagem_url, is_aberto, ")
                        .append("taxa_entrega_base, tempo_entrega_min, tempo_entrega_max, avaliacao_media, total_avaliacoes, ")
                        .append("end_rua, end_numero, end_cidade, end_estado, end_cep, ")
                        .append("geo_lat, geo_lng, geo_geohash, ")
                        .append("horario_domingo, horario_segunda, horario_terca, horario_quarta, horario_quinta, horario_sexta, horario_sabado) VALUES \n");

                int limite = Math.min(i + TAMANHO_LOTE - 1, QUANTIDADE_LOJAS);
                for (int j = i; j <= limite; j++) {

                    String id = String.format("loja_%04d", j);
                    lojasGeradas.add(id);

                    String nome = faker.restaurant().name().replace("'", "''");
                    String descricao = faker.restaurant().description().replace("'", "''");
                    String categoria = categoriasLoja[faker.random().nextInt(categoriasLoja.length)];
                    String imagemUrl = "https://picsum.photos/seed/" + id + "/400";
                    boolean isAberto = faker.bool().bool();

                    String taxaEntrega = String.format(Locale.US, "%.2f", faker.number().randomDouble(2, 2, 15));
                    int tempoMin = faker.number().numberBetween(15, 30);
                    int tempoMax = tempoMin + faker.number().numberBetween(10, 25);
                    String avaliacaoMedia = String.format(Locale.US, "%.1f", faker.number().randomDouble(1, 3, 5));
                    int totalAvaliacoes = faker.number().numberBetween(0, 2000);

                    String rua = faker.address().streetName().replace("'", "''");
                    String numero = faker.address().buildingNumber();
                    String cidade = faker.address().cityName().replace("'", "''");
                    String estado = faker.address().stateAbbr();
                    String cep = faker.address().zipCode().replaceAll("[^0-9]", "");

                    // 🔴 CORREÇÃO DO ERRO DO DATAFAKER AQUI
                    String lat = faker.address().latitude().replace(",", ".");
                    String lng = faker.address().longitude().replace(",", ".");

                    String hrPadrao = "11:00 - 23:00";
                    String hrFechado = "Fechado";

                    loteLojas.append(String.format(
                            // Formatação exata batendo com a ordem do INSERT acima
                            "('%s', '%s', '%s', '%s', '%s', %b, %s, %d, %d, %s, %d, '%s', '%s', '%s', '%s', '%s', %s, %s, NULL, '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                            id, nome, descricao, categoria, imagemUrl, isAberto, taxaEntrega, tempoMin, tempoMax, avaliacaoMedia, totalAvaliacoes,
                            rua, numero, cidade, estado, cep, lat, lng,
                            hrPadrao, hrFechado, hrPadrao, hrPadrao, hrPadrao, hrPadrao, hrPadrao
                    ));

                    if (j < limite) loteLojas.append(",\n");
                    else loteLojas.append(";\n\n");
                }
                writer.write(loteLojas.toString());
            }
            System.out.println("✅ Lojas geradas com sucesso!");

            // ==========================================
            // 2. GERAR PRODUTOS INTERLIGADOS
            // ==========================================
            writer.write("-- ==========================================\n");
            writer.write("-- INSERÇÃO EM LOTE DE PRODUTOS\n");
            writer.write("-- ==========================================\n");

            int countProdutosSalvos = 0;
            StringBuilder loteProdutos = new StringBuilder();
            boolean instrucaoAberta = false;

            for (String lojaId : lojasGeradas) {
                for (int p = 1; p <= PRODUTOS_POR_LOJA; p++) {

                    if (!instrucaoAberta) {
                        loteProdutos.append("INSERT INTO tb_produtos (id, loja_id, nome, descricao, preco, categoria_menu, imagem_url, percentual_desconto) VALUES \n");
                        instrucaoAberta = true;
                    }

                    String prodId = String.format("prod_%s_%02d", lojaId, p);
                    String nome = faker.food().dish().replace("'", "''");
                    String descricao = faker.food().ingredient().replace("'", "''") + " com temperos especiais da casa.";
                    String preco = String.format(Locale.US, "%.2f", faker.number().randomDouble(2, 10, 80));
                    String catMenu = categoriasProduto[faker.random().nextInt(categoriasProduto.length)];
                    String imagemUrl = "https://picsum.photos/seed/" + prodId + "/200";
                    int desconto = faker.number().numberBetween(1, 10) > 8 ? faker.number().numberBetween(10, 30) : 0;

                    loteProdutos.append(String.format(
                            "('%s', '%s', '%s', '%s', %s, '%s', '%s', %d)",
                            prodId, lojaId, nome, descricao, preco, catMenu, imagemUrl, desconto
                    ));

                    countProdutosSalvos++;

                    if (countProdutosSalvos % TAMANHO_LOTE == 0) {
                        loteProdutos.append(";\n\n");
                        writer.write(loteProdutos.toString());
                        loteProdutos.setLength(0);
                        instrucaoAberta = false;
                    } else {
                        loteProdutos.append(",\n");
                    }
                }
            }

            if (instrucaoAberta) {
                loteProdutos.setLength(loteProdutos.length() - 2);
                loteProdutos.append(";\n\n");
                writer.write(loteProdutos.toString());
            }

            System.out.println("✅ " + countProdutosSalvos + " Produtos gerados e vinculados!");

        } catch (IOException e) {
            e.printStackTrace();
        }

        long tempoFim = System.currentTimeMillis();
        System.out.println("🚀 Arquivo SQL gerado em " + (tempoFim - tempoInicio) + "ms!");
    }
}