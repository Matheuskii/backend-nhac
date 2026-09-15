package br.com.nhac.backend_nhac.domain.entrega;

import br.com.nhac.backend_nhac.domain.entrega.dto.PontoCoordenadaDTO;
import br.com.nhac.backend_nhac.domain.entrega.dto.RotaEntregaResponseDTO;
import br.com.nhac.backend_nhac.domain.loja.GeoLocalizacao;
import br.com.nhac.backend_nhac.domain.loja.Loja;
import br.com.nhac.backend_nhac.domain.pedido.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RotaServiceTest {

    private final RotaService rotaService = new RotaService();

    @Test
    @DisplayName("Deve codificar e decodificar Polyline corretamente")
    void deveCodificarEDecodificarPolyline() {
        List<PontoCoordenadaDTO> pontosOriginais = List.of(
                new PontoCoordenadaDTO(-23.55052, -46.63330),
                new PontoCoordenadaDTO(-23.56000, -46.64000)
        );

        String polyline = RotaService.codificarPolyline(pontosOriginais);
        assertNotNull(polyline);
        assertFalse(polyline.isBlank());

        List<PontoCoordenadaDTO> pontosDecodificados = RotaService.decodificarPolyline(polyline);
        assertEquals(2, pontosDecodificados.size());
        assertEquals(-23.55052, pontosDecodificados.get(0).latitude(), 0.0001);
        assertEquals(-46.63330, pontosDecodificados.get(0).longitude(), 0.0001);
        assertEquals(-23.56000, pontosDecodificados.get(1).latitude(), 0.0001);
        assertEquals(-46.64000, pontosDecodificados.get(1).longitude(), 0.0001);
    }

    @Test
    @DisplayName("Deve calcular rota de pedido com fallback resiliente")
    void deveCalcularRotaComFallback() {
        Loja loja = new Loja();
        loja.setNome("Hamburgueria Nhac");
        loja.setGeoLocalizacao(new GeoLocalizacao(-23.55052, -46.63330, "6fz2h3k"));

        Pedido pedido = new Pedido();
        pedido.setId("ped_rota_1");
        pedido.setLoja(loja);
        pedido.setEntregaLatitude(-23.56000);
        pedido.setEntregaLongitude(-46.64000);

        RotaEntregaResponseDTO rota = rotaService.calcularRota(pedido);

        assertNotNull(rota);
        assertEquals("ped_rota_1", rota.pedidoId());
        assertEquals("Hamburgueria Nhac", rota.lojaNome());
        assertEquals(-23.55052, rota.origem().latitude());
        assertEquals(-46.63330, rota.origem().longitude());
        assertEquals(-23.56000, rota.destino().latitude());
        assertEquals(-46.64000, rota.destino().longitude());
        assertTrue(rota.distanciaKm() > 0);
        assertTrue(rota.duracaoEstimadaMinutos() >= 1);
        assertNotNull(rota.polyline());
        assertFalse(rota.polyline().isBlank());
    }
}
