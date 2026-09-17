package br.com.nhac.backend_nhac.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GeradorCatalogoInsano {

    private static final Random random = new Random(42);

    // ========== QUANTIDADES ==========
    // ATENÇÃO: tb_lojas.usuario_id tem UNIQUE (V027) -> 1 lojista por loja
    private static final int QUANTIDADE_LOJAS        = 1000;
    private static final int QUANTIDADE_LOJISTAS     = QUANTIDADE_LOJAS; // 1:1
    private static final int QUANTIDADE_CLIENTES     = 500;
    private static final int QUANTIDADE_FUNCIONARIOS = 300;
    private static final int PRODUTOS_POR_LOJA       = 15;
    private static final int TAMANHO_LOTE            = 500;

    // Hash bcrypt da senha "senha123" — só para DEV
    private static final String SENHA_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    // ========== CATEGORIAS ==========
    private static final String[] CATEGORIAS_LOJA = {
            "Lanches", "Pizza", "Japonesa", "Brasileira", "Italiana",
            "Saudável", "Doces", "Árabe", "Chinesa", "Mexicana",
            "Vegetariana", "Frutos do Mar", "Churrasco", "Açaí", "Cafeteria"
    };

    private static final String[] CATEGORIAS_PRODUTO = {
            "Prato Principal", "Acompanhamento", "Bebidas", "Sobremesas",
            "Combos", "Entradas", "Porções", "Saladas"
    };

    private static final String[] CARGOS_FUNCIONARIO = {
            "Gerente", "Atendente", "Cozinheiro", "Auxiliar de Cozinha",
            "Entregador", "Caixa", "Garçom", "Chapeiro"
    };

    // ========== NOMES ==========
    private static final String[] PREFIXOS_LOJA = {
            "Cantina", "Restaurante", "Sabor", "Delícias", "Tempero",
            "Casa", "Point", "Espaço", "Bistrô", "Boteco", "Empório",
            "Cozinha", "Grelhados", "Sabores", "Cantinho", "Esquina",
            "Sabor & Arte", "Quinta", "Mesa", "Tacho"
    };

    private static final String[] SUFIXOS_LOJA = {
            "do Chef", "da Vovó", "Premium", "Express", "Gourmet",
            "Tradicional", "Caseiro", "Artesanal", "da Praça", "do Bairro",
            "Paulista", "Carioca", "Mineiro", "Baiano", "Nordestino",
            "Brasileiro", "do Sabor", "da Família", "Original", "Top"
    };

    private static final String[] PRIMEIROS_NOMES = {
            "Ana", "Maria", "João", "Pedro", "Carlos", "Fernanda", "Juliana",
            "Rafael", "Lucas", "Beatriz", "Camila", "Bruno", "Diego", "Larissa",
            "Marcos", "Paulo", "Tatiane", "Vanessa", "Ricardo", "Roberta",
            "Gabriel", "Guilherme", "Thiago", "Amanda", "Patrícia", "Letícia",
            "Felipe", "Rodrigo", "Eduardo", "Vinícius", "Aline", "Bianca",
            "Jéssica", "Priscila", "Renata", "Simone", "Vera", "Cláudia",
            "Otávio", "Mateus", "Daniel", "Leonardo", "Fábio", "Igor"
    };

    private static final String[] SOBRENOMES = {
            "Silva", "Santos", "Oliveira", "Souza", "Costa", "Ferreira",
            "Almeida", "Lima", "Pereira", "Carvalho", "Gomes", "Ribeiro",
            "Martins", "Araújo", "Barbosa", "Rocha", "Dias", "Nascimento",
            "Moreira", "Nunes", "Mendes", "Cardoso", "Teixeira", "Correia",
            "Fernandes", "Vieira", "Monteiro", "Ramos", "Castro", "Pinto"
    };

    // ========== LOCALIZAÇÃO ==========
    private static final String[][] CIDADES_UF = {
            {"São Paulo", "SP"}, {"Rio de Janeiro", "RJ"}, {"Belo Horizonte", "MG"},
            {"Salvador", "BA"}, {"Brasília", "DF"}, {"Fortaleza", "CE"},
            {"Recife", "PE"}, {"Porto Alegre", "RS"}, {"Curitiba", "PR"},
            {"Manaus", "AM"}, {"Belém", "PA"}, {"Goiânia", "GO"},
            {"Campinas", "SP"}, {"São Luís", "MA"}, {"Maceió", "AL"},
            {"Natal", "RN"}, {"Campo Grande", "MS"}, {"Teresina", "PI"},
            {"João Pessoa", "PB"}, {"Florianópolis", "SC"}
    };

    private static final String[] RUAS_BR = {
            "Rua das Flores", "Avenida Brasil", "Rua São João", "Avenida Paulista",
            "Rua dos Ipês", "Avenida Atlântica", "Rua XV de Novembro",
            "Avenida Getúlio Vargas", "Rua Marechal Deodoro", "Rua da Praia",
            "Avenida Beira-Mar", "Rua Sete de Setembro", "Rua Tiradentes",
            "Avenida Rio Branco", "Rua Dom Pedro II", "Rua Santos Dumont",
            "Avenida Independência", "Rua Duque de Caxias", "Rua José Bonifácio",
            "Avenida das Palmeiras", "Rua dos Coqueiros", "Travessa da Paz",
            "Alameda dos Anjos", "Rua do Comércio", "Avenida Central",
            "Rua Coronel Silva", "Avenida das Nações", "Rua Bela Vista",
            "Rua Esperança", "Avenida dos Estados", "Rua Itororó",
            "Rua Aurora", "Rua Primavera", "Avenida Sumaré", "Rua Harmonia"
    };

    private static final String[] BAIRROS_BR = {
            "Centro", "Jardim América", "Vila Mariana", "Pinheiros", "Moema",
            "Copacabana", "Ipanema", "Botafogo", "Savassi", "Funcionários",
            "Boa Viagem", "Pituba", "Asa Sul", "Asa Norte", "Aldeota",
            "Meireles", "Batel", "Água Verde", "Praia da Costa", "Cambuí",
            "Setor Bueno", "Jardim dos Estados", "Trindade", "Ponta Verde",
            "Bessa", "Manaíra", "Centro Histórico", "Cidade Baixa", "Vila Madalena"
    };

    // ========== PRODUTOS ==========
    private static final String[] NOMES_PRODUTOS = {
            "Feijoada Completa", "Moqueca de Peixe", "Frango à Parmegiana",
            "Picanha na Brasa", "Strogonoff de Frango", "Lasanha à Bolonhesa",
            "Escondidinho de Carne", "Bobó de Camarão", "Baião de Dois",
            "Acarajé", "Vatapá", "Coxinha de Frango", "Pastel de Carne",
            "Pão de Queijo", "Brigadeiro Gourmet", "Pudim de Leite",
            "Açaí na Tigela", "Tapioca Recheada", "Cuscuz Nordestino",
            "Carne de Sol com Macaxeira", "Galinhada Goiana",
            "Arroz Carreteiro", "Virado à Paulista", "Tutu de Feijão",
            "Torresmo Crocante", "Kibe Assado", "Empada de Palmito",
            "Pão na Chapa", "Misto Quente", "X-Tudo", "X-Bacon",
            "X-Salada", "Cachorro-quente Completo", "Calabresa Acebolada",
            "Bolinho de Bacalhau", "Camarão Empanado", "Casquinha de Siri",
            "Nhoque ao Sugo", "Ravioli de Queijo", "Risoto de Camarão",
            "Salmão Grelhado", "Filé ao Molho Madeira", "Costela Bovina",
            "Hambúrguer Artesanal", "Cheeseburger Duplo", "Pizza Margherita",
            "Pizza Calabresa", "Pizza Portuguesa", "Pizza Quatro Queijos",
            "Sushi Combo", "Temaki de Salmão", "Hossomaki de Atum",
            "Yakisoba Tradicional", "Lamen Especial", "Frango Xadrez",
            "Rolinho Primavera", "Tacos Mexicanos", "Burrito de Carne",
            "Quesadilla de Queijo", "Kebab de Cordeiro", "Esfiha de Carne",
            "Shawarma de Frango", "Tabule", "Quibe Cru", "Falafel",
            "Pad Thai", "Curry de Frango", "Frango Tikka Masala",
            "Salada Caesar", "Salada Grega", "Wrap de Frango Grelhado",
            "Sanduíche Natural", "Panqueca de Frango", "Omelete Especial"
    };

    private static final String[] INGREDIENTES = {
            "arroz branco", "feijão preto", "farofa crocante", "couve refogada",
            "vinagrete fresco", "queijo mussarela", "queijo cheddar",
            "catupiry cremoso", "presunto defumado", "bacon crocante",
            "ovo frito", "cebola caramelizada", "alho dourado",
            "tomate fresco", "alface americana", "rúcula", "cenoura ralada",
            "carne bovina", "frango grelhado", "carne suína", "peixe fresco",
            "camarão", "atum fresco", "salmão", "legumes salteados",
            "batata rústica", "mandioca frita", "palmito", "milho verde",
            "ervilha", "azeite extra virgem", "ervas finas",
            "pimenta calabresa", "molho especial da casa", "molho barbecue",
            "molho branco", "molho de tomate caseiro", "parmesão ralado"
    };

    // ========== HORÁRIOS ==========
    private static final String[] HORARIOS_PADRAO = {
            "08:00-18:00", "09:00-19:00", "10:00-22:00", "11:00-23:00",
            "11:00-00:00", "12:00-22:00", "18:00-23:59"
    };
    private static final String HORARIO_FECHADO = "Fechado";

    // ==========================================================
    // MAIN
    // ==========================================================
    public static void main(String[] args) {
        Path caminhoArquivo = Path.of("src/main/resources/db/migration/V998__populando_catalogo_completo.sql");

        System.out.println("🔥 Iniciando geração do Catálogo Insano (100% PT-BR)...");
        long tempoInicio = System.currentTimeMillis();

        try {
            Files.createDirectories(caminhoArquivo.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(caminhoArquivo)) {
                writer.write("-- ==========================================\n");
                writer.write("-- SEED GERADO AUTOMATICAMENTE — CATÁLOGO PT-BR\n");
                writer.write("-- ==========================================\n\n");
                writer.write("BEGIN;\n\n");

                // 1) Lojistas (1 por loja por causa do UNIQUE em tb_lojas.usuario_id)
                List<String> lojistasIds = new ArrayList<>(QUANTIDADE_LOJISTAS);
                gerarLojistas(writer, lojistasIds);

                // 2) Clientes + Admin (sem loja_vinculada_id)
                gerarClientesEAdmin(writer);

                // 3) Lojas — vinculadas 1:1 com lojistas
                List<String> lojasGeradas = gerarLojas(writer, lojistasIds);

                // 4) Funcionários — precisam das lojas já existindo (FK loja_vinculada_id)
                gerarFuncionarios(writer, lojasGeradas);

                // 5) Produtos
                int totalProdutos = gerarProdutos(writer, lojasGeradas);

                writer.write("COMMIT;\n");

                System.out.println("✅ " + lojistasIds.size() + " lojistas, " + QUANTIDADE_CLIENTES + " clientes, 1 admin");
                System.out.println("✅ " + lojasGeradas.size() + " lojas");
                System.out.println("✅ " + QUANTIDADE_FUNCIONARIOS + " funcionários vinculados");
                System.out.println("✅ " + totalProdutos + " produtos");
            }
        } catch (IOException e) {
            System.err.println("❌ Erro fatal ao gerar o arquivo SQL: " + e.getMessage());
            e.printStackTrace();
        }

        long tempoFim = System.currentTimeMillis();
        System.out.println("🚀 Arquivo SQL gerado em " + (tempoFim - tempoInicio) + "ms!");
    }

    // ==========================================================
    // 1) LOJISTAS
    // ==========================================================
    private static void gerarLojistas(BufferedWriter writer, List<String> lojistasIds) throws IOException {
        String insertPrefix = "INSERT INTO tb_usuarios " +
                "(id, nome, email, telefone, imagem_url, senha, papel, telefone_verificado, email_verificado, ativo, loja_vinculada_id, cargo) VALUES \n";

        writer.write("-- ==========================================\n");
        writer.write("-- 1. LOJISTAS (" + QUANTIDADE_LOJISTAS + " — 1 para cada loja por causa do UNIQUE em tb_lojas.usuario_id)\n");
        writer.write("-- ==========================================\n");

        StringBuilder lote = new StringBuilder();
        int count = 0;

        for (int i = 1; i <= QUANTIDADE_LOJISTAS; i++) {
            String id = String.format("user_lojista_%04d", i);
            lojistasIds.add(id);

            if (count == 0) lote.append(insertPrefix);
            lote.append(formatarUsuario(id, "LOJISTA", i, "lojista", null, null)).append(",\n");
            count++;

            if (count == TAMANHO_LOTE || i == QUANTIDADE_LOJISTAS) {
                lote.setLength(lote.length() - 2);
                lote.append(";\n\n");
                writer.write(lote.toString());
                lote.setLength(0);
                count = 0;
            }
        }
    }

    // ==========================================================
    // 2) CLIENTES + ADMIN
    // ==========================================================
    private static void gerarClientesEAdmin(BufferedWriter writer) throws IOException {
        String insertPrefix = "INSERT INTO tb_usuarios " +
                "(id, nome, email, telefone, imagem_url, senha, papel, telefone_verificado, email_verificado, ativo, loja_vinculada_id, cargo) VALUES \n";

        writer.write("-- ==========================================\n");
        writer.write("-- 2. CLIENTES (" + QUANTIDADE_CLIENTES + ") + ADMIN\n");
        writer.write("-- ==========================================\n");

        StringBuilder lote = new StringBuilder();
        int count = 0;
        int total = QUANTIDADE_CLIENTES + 1;
        int gerados = 0;

        // Clientes
        for (int i = 1; i <= QUANTIDADE_CLIENTES; i++) {
            String id = String.format("user_cliente_%04d", i);
            if (count == 0) lote.append(insertPrefix);
            lote.append(formatarUsuario(id, "CLIENTE", i, "cliente", null, null)).append(",\n");
            count++; gerados++;

            if (count == TAMANHO_LOTE || gerados == total) {
                lote.setLength(lote.length() - 2);
                lote.append(";\n\n");
                writer.write(lote.toString());
                lote.setLength(0);
                count = 0;
            }
        }

        // Admin
        if (count == 0) lote.append(insertPrefix);
        lote.append(formatarUsuario("user_admin_0001", "ADMIN", 1, "admin", null, null)).append(",\n");
        count++;

        lote.setLength(lote.length() - 2);
        lote.append(";\n\n");
        writer.write(lote.toString());
        lote.setLength(0);
    }

    // ==========================================================
    // 3) LOJAS — cada uma vinculada ao lojista de mesmo índice
    // ==========================================================
    private static List<String> gerarLojas(BufferedWriter writer, List<String> lojistasIds) throws IOException {
        List<String> lojasGeradas = new ArrayList<>(QUANTIDADE_LOJAS);

        String insertPrefix = "INSERT INTO tb_lojas (" +
                "id, usuario_id, nome, descricao, categoria, imagem_url, is_aberto, " +
                "avaliacao_media, taxa_entrega_base, tempo_entrega_min, tempo_entrega_max, total_avaliacoes, " +
                "end_rua, end_numero, end_bairro, end_complemento, end_cidade, end_estado, end_cep, " +
                "geo_lat, geo_lng, geo_geohash, " +
                "horario_domingo, horario_segunda, horario_terca, horario_quarta, horario_quinta, horario_sexta, horario_sabado, " +
                "entrega_propria, retirada_no_local, raio_entrega_km, " +
                "aceita_dinheiro, aceita_credito, aceita_debito, aceita_pix, aceita_vale_refeicao, aceita_vale_alimentacao" +
                ") VALUES \n";

        writer.write("-- ==========================================\n");
        writer.write("-- 3. LOJAS (" + QUANTIDADE_LOJAS + ") — cada uma vinculada ao lojista de mesmo índice\n");
        writer.write("-- ==========================================\n");

        StringBuilder lote = new StringBuilder();
        int count = 0;

        for (int i = 1; i <= QUANTIDADE_LOJAS; i++) {
            String lojaId = String.format("loja_%04d", i);
            String lojistaId = lojistasIds.get(i - 1); // 1:1
            lojasGeradas.add(lojaId);

            if (count == 0) lote.append(insertPrefix);
            lote.append(formatarValoresLoja(lojaId, lojistaId)).append(",\n");
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

    // ==========================================================
    // 4) FUNCIONÁRIOS — vinculados a lojas já existentes
    // ==========================================================
    private static void gerarFuncionarios(BufferedWriter writer, List<String> lojasGeradas) throws IOException {
        String insertPrefix = "INSERT INTO tb_usuarios " +
                "(id, nome, email, telefone, imagem_url, senha, papel, telefone_verificado, email_verificado, ativo, loja_vinculada_id, cargo) VALUES \n";

        writer.write("-- ==========================================\n");
        writer.write("-- 4. FUNCIONÁRIOS (" + QUANTIDADE_FUNCIONARIOS + ") — vinculados a lojas existentes\n");
        writer.write("-- ==========================================\n");

        StringBuilder lote = new StringBuilder();
        int count = 0;

        for (int i = 1; i <= QUANTIDADE_FUNCIONARIOS; i++) {
            String id = String.format("user_func_%04d", i);
            String lojaVinculada = lojasGeradas.get(random.nextInt(lojasGeradas.size()));
            String cargo = CARGOS_FUNCIONARIO[random.nextInt(CARGOS_FUNCIONARIO.length)];

            if (count == 0) lote.append(insertPrefix);
            lote.append(formatarUsuario(id, "FUNCIONARIO", i, "func", lojaVinculada, cargo)).append(",\n");
            count++;

            if (count == TAMANHO_LOTE || i == QUANTIDADE_FUNCIONARIOS) {
                lote.setLength(lote.length() - 2);
                lote.append(";\n\n");
                writer.write(lote.toString());
                lote.setLength(0);
                count = 0;
            }
        }
    }

    // ==========================================================
    // 5) PRODUTOS
    // ==========================================================
    private static int gerarProdutos(BufferedWriter writer, List<String> lojasGeradas) throws IOException {
        String insertPrefix = "INSERT INTO tb_produtos " +
                "(id, loja_id, nome, descricao, preco, categoria_menu, imagem_url, is_ativo, peso, percentual_desconto, estoque) VALUES \n";

        writer.write("-- ==========================================\n");
        writer.write("-- 5. PRODUTOS (" + (QUANTIDADE_LOJAS * PRODUTOS_POR_LOJA) + ")\n");
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

    // ==========================================================
    // FORMATAÇÃO DE USUÁRIO
    // ==========================================================
    private static String formatarUsuario(String id, String papel, int index, String tipo,
                                          String lojaVinculada, String cargo) {
        String nome = nomePessoa();
        String email = gerarEmail(nome, tipo, index);
        String telefone = gerarTelefone();
        String imagem = "https://picsum.photos/seed/" + id + "/200";
       boolean telefoneVerificado = true;
        boolean emailVerificado = true;
        boolean ativo = true; // todos ativos por padrão

        String lojaVinculadaSql = (lojaVinculada == null) ? "NULL" : sql(lojaVinculada);
        String cargoSql = (cargo == null) ? "NULL" : sql(cargo);

        return String.format(
                "(%s, %s, %s, %s, %s, %s, %s, %b, %b, %b, %s, %s)",
                sql(id), sql(nome), sql(email), sql(telefone), sql(imagem),
                sql(SENHA_HASH), sql(papel),
                telefoneVerificado, emailVerificado, ativo,
                lojaVinculadaSql, cargoSql
        );
    }

    private static String nomePessoa() {
        return PRIMEIROS_NOMES[random.nextInt(PRIMEIROS_NOMES.length)]
                + " " + SOBRENOMES[random.nextInt(SOBRENOMES.length)];
    }

    private static String gerarEmail(String nome, String tipo, int index) {
        String base = nome.toLowerCase(Locale.ROOT)
                .replace("á", "a").replace("â", "a").replace("ã", "a")
                .replace("é", "e").replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o").replace("ô", "o").replace("õ", "o")
                .replace("ú", "u")
                .replace("ç", "c")
                .replaceAll("[^a-z ]", "")
                .trim()
                .replaceAll("\\s+", ".");
        return base + "." + tipo + index + "@nhac.com";
    }

    private static String gerarTelefone() {
        int[] ddds = {11, 21, 31, 41, 51, 61, 71, 81, 85};
        int ddd = ddds[random.nextInt(ddds.length)];
        int p1 = 90000 + random.nextInt(9999);
        int p2 = 1000 + random.nextInt(8999);
        return String.format("(%d) %d-%04d", ddd, p1, p2);
    }

    // ==========================================================
    // FORMATAÇÃO DE LOJA
    // ==========================================================
    private static String formatarValoresLoja(String id, String lojistaId) {
        String nome = nomeLoja();
        String descricao = descricaoLoja();
        String categoria = CATEGORIAS_LOJA[random.nextInt(CATEGORIAS_LOJA.length)];
        String imagemUrl = "https://picsum.photos/seed/" + id + "/400";
        boolean isAberto = random.nextInt(10) > 2;

        String avaliacaoMedia = String.format(Locale.US, "%.1f", 3.0 + random.nextDouble() * 2.0);
        String taxaEntrega = String.format(Locale.US, "%.2f", 2 + random.nextDouble() * 13);
        int tempoMin = 15 + random.nextInt(15);
        int tempoMax = tempoMin + 10 + random.nextInt(15);
        int totalAvaliacoes = random.nextInt(2000);

        String[] cidadeUf = CIDADES_UF[random.nextInt(CIDADES_UF.length)];
        String cidade = cidadeUf[0];
        String estado = cidadeUf[1];
        String rua = RUAS_BR[random.nextInt(RUAS_BR.length)];
        String numero = String.valueOf(1 + random.nextInt(2000));
        String bairro = BAIRROS_BR[random.nextInt(BAIRROS_BR.length)];
        String complemento = random.nextInt(3) == 0 ? "Sala " + (1 + random.nextInt(50)) : null;
        String cep = String.format("%05d-%03d", 1000 + random.nextInt(89999), random.nextInt(1000));

        double lat = -15.78 + (random.nextDouble() - 0.5) * 30;
        double lng = -47.93 + (random.nextDouble() - 0.5) * 30;
        String latStr = String.format(Locale.US, "%.8f", lat);
        String lngStr = String.format(Locale.US, "%.8f", lng);
        String geohash = null; // deixa NULL, o campo é opcional

        String[] horarios = new String[7];
        for (int i = 0; i < 7; i++) {
            horarios[i] = random.nextInt(10) > 8
                    ? HORARIO_FECHADO
                    : HORARIOS_PADRAO[random.nextInt(HORARIOS_PADRAO.length)];
        }

        boolean entregaPropria = random.nextInt(10) > 3;
        boolean retiradaNoLocal = random.nextInt(10) > 4;
        String raioEntrega = random.nextInt(3) == 0
                ? "NULL"
                : String.format(Locale.US, "%.2f", 2 + random.nextDouble() * 13);

        // Formas de pagamento — colunas reais da V029
        boolean aceitaDinheiro = random.nextInt(10) > 2;
        boolean aceitaCredito = random.nextInt(10) > 3;
        boolean aceitaDebito = random.nextInt(10) > 3;
        boolean aceitaPix = random.nextInt(10) > 1;
        boolean aceitaVr = random.nextInt(10) > 6;
        boolean aceitaVa = random.nextInt(10) > 7;

        return String.format(
                "(%s, %s, %s, %s, %s, %s, %b, " +
                        "%s, %s, %d, %d, %d, " +
                        "%s, %s, %s, %s, %s, %s, %s, " +
                        "%s, %s, %s, " +
                        "%s, %s, %s, %s, %s, %s, %s, " +
                        "%b, %b, %s, " +
                        "%b, %b, %b, %b, %b, %b)",
                sql(id), sql(lojistaId), sql(nome), sql(descricao), sql(categoria), sql(imagemUrl), isAberto,
                avaliacaoMedia, taxaEntrega, tempoMin, tempoMax, totalAvaliacoes,
                sql(rua), sql(numero), sql(bairro), sql(complemento), sql(cidade), sql(estado), sql(cep),
                latStr, lngStr, sql(geohash),
                sql(horarios[0]), sql(horarios[1]), sql(horarios[2]),
                sql(horarios[3]), sql(horarios[4]), sql(horarios[5]), sql(horarios[6]),
                entregaPropria, retiradaNoLocal, raioEntrega,
                aceitaDinheiro, aceitaCredito, aceitaDebito, aceitaPix, aceitaVr, aceitaVa
        );
    }

    private static String nomeLoja() {
        String prefixo = PREFIXOS_LOJA[random.nextInt(PREFIXOS_LOJA.length)];
        String sufixo = SUFIXOS_LOJA[random.nextInt(SUFIXOS_LOJA.length)];
        if (random.nextInt(3) == 0) {
            String nome = PRIMEIROS_NOMES[random.nextInt(PRIMEIROS_NOMES.length)];
            return prefixo + " " + nome + " " + sufixo;
        }
        return prefixo + " " + sufixo;
    }

    private static String descricaoLoja() {
        String[] templates = {
                "O melhor da culinária %s preparado com carinho para você.",
                "Sabores autênticos da cozinha %s com ingredientes frescos.",
                "Tradição e qualidade em cada prato %s que servimos.",
                "Deliciosas opções %s para todos os gostos.",
                "Cozinha %s com receitas exclusivas e ingredientes selecionados.",
                "Comida %s feita na hora, do jeitinho que você gosta."
        };
        String cat = CATEGORIAS_LOJA[random.nextInt(CATEGORIAS_LOJA.length)].toLowerCase(Locale.ROOT);
        String tpl = templates[random.nextInt(templates.length)];
        return String.format(tpl, cat);
    }

    // ==========================================================
    // FORMATAÇÃO DE PRODUTO
    // ==========================================================
    private static String formatarValoresProduto(String lojaId, int p) {
        String prodId = String.format("prod_%s_%02d", lojaId, p);
        String nome = NOMES_PRODUTOS[random.nextInt(NOMES_PRODUTOS.length)];
        String descricao = descricaoProduto(nome);
        String preco = String.format(Locale.US, "%.2f", 10 + random.nextDouble() * 70);
        String catMenu = CATEGORIAS_PRODUTO[random.nextInt(CATEGORIAS_PRODUTO.length)];
        String imagemUrl = "https://picsum.photos/seed/" + prodId + "/200";
        boolean isAtivo = random.nextInt(20) > 1;
        String peso = gerarPeso();
        int desconto = random.nextInt(10) > 8 ? 5 + random.nextInt(25) : 0;
        int estoque = 10 + random.nextInt(490);

        return String.format(
                "(%s, %s, %s, %s, %s, %s, %s, %b, %s, %d, %d)",
                sql(prodId), sql(lojaId), sql(nome), sql(descricao), preco,
                sql(catMenu), sql(imagemUrl), isAtivo, sql(peso), desconto, estoque
        );
    }

    private static String gerarPeso() {
        String[] unidades = {"g", "ml"};
        String unidade = unidades[random.nextInt(unidades.length)];
        int valor = (unidade.equals("g"))
                ? (100 + random.nextInt(9) * 50)
                : (200 + random.nextInt(7) * 100);
        return valor + unidade;
    }

    private static String descricaoProduto(String nomeBase) {
        String ing1 = INGREDIENTES[random.nextInt(INGREDIENTES.length)];
        String ing2 = INGREDIENTES[random.nextInt(INGREDIENTES.length)];
        String ing3 = INGREDIENTES[random.nextInt(INGREDIENTES.length)];

        while (ing2.equals(ing1)) ing2 = INGREDIENTES[random.nextInt(INGREDIENTES.length)];
        while (ing3.equals(ing1) || ing3.equals(ing2))
            ing3 = INGREDIENTES[random.nextInt(INGREDIENTES.length)];

        String[] templates = {
                "%s preparado com %s, %s e %s.",
                "Delicioso %s feito com %s, %s e %s.",
                "Nosso %s combina %s, %s e %s em uma explosão de sabor.",
                "%s artesanal com %s, %s e %s. Uma delícia!",
                "Experimente nosso %s, preparado com %s, %s e %s.",
                "%s da casa com %s, %s e %s."
        };
        return String.format(templates[random.nextInt(templates.length)], nomeBase, ing1, ing2, ing3);
    }

    // ==========================================================
    // UTIL
    // ==========================================================
    private static String sql(String value) {
        if (value == null) return "NULL";
        return "'" + value.replace("'", "''") + "'";
    }
}