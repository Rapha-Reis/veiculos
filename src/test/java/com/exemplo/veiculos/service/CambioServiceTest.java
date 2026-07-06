package com.exemplo.veiculos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CambioServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CambioService cambioService;

    private Map<String, Object> mockAwesomeResponse;
    private Map<String, Object> mockFrankfurterResponse;

    @BeforeEach
    void setUp() {
        mockAwesomeResponse = new HashMap<>();
        Map<String, String> usdDetails = new HashMap<>();
        usdDetails.put("bid", "5.00");
        mockAwesomeResponse.put("USDBRL", usdDetails);

        mockFrankfurterResponse = new HashMap<>();
        Map<String, Double> ratesDetails = new HashMap<>();
        ratesDetails.put("BRL", 5.00);
        mockFrankfurterResponse.put("rates", ratesDetails);
    }

    @Test
    void deveObterCotacaoPelaAwesomeApiComSucesso() {
        when(restTemplate.getForObject(org.mockito.Mockito.contains("awesomeapi"), eq(Map.class)))
                .thenReturn(mockAwesomeResponse);

        BigDecimal cotacao = cambioService.obterCotacaoDolar();

        assertThat(cotacao).isEqualByComparingTo("5.00");
    }

    @Test
    void deveUsarFrankfurterComoFallbackQuandoAwesomeApiFalhar() {
        when(restTemplate.getForObject(org.mockito.Mockito.contains("awesomeapi"), eq(Map.class)))
                .thenThrow(new RuntimeException("AwesomeAPI fora do ar"));
                
        when(restTemplate.getForObject(org.mockito.Mockito.contains("frankfurter"), eq(Map.class)))
                .thenReturn(mockFrankfurterResponse);

        BigDecimal cotacao = cambioService.obterCotacaoDolar();

        assertThat(cotacao).isEqualByComparingTo("5.00");
    }

    @Test
    void deveLancarExcecaoQuandoAmbasAsApisFalharem() {
        when(restTemplate.getForObject(org.mockito.Mockito.contains("awesomeapi"), eq(Map.class)))
                .thenThrow(new RuntimeException("Erro Awesome"));
                
        when(restTemplate.getForObject(org.mockito.Mockito.contains("frankfurter"), eq(Map.class)))
                .thenThrow(new RuntimeException("Erro Frankfurter"));

        assertThatThrownBy(() -> cambioService.obterCotacaoDolar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não foi possível obter a cotação do dólar");
    }

    @Test
    void deveConverterParaDolarCorretamente() {
        // Mockando a Awesome para responder 5.00 no fluxo de conversão
        when(restTemplate.getForObject(org.mockito.Mockito.contains("awesomeapi"), eq(Map.class)))
                .thenReturn(mockAwesomeResponse);

        BigDecimal resultado = cambioService.converterParaDolar(BigDecimal.valueOf(100));

        assertThat(resultado).isEqualByComparingTo(BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP));
    }
}