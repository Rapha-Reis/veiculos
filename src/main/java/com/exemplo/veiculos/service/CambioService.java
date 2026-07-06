package com.exemplo.veiculos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CambioService {

    private final RestTemplate restTemplate;
    
    private static final String AWESOME_API = "https://economia.awesomeapi.com.br/json/last/USD-BRL";
    private static final String FRANKFURTER_API = "https://api.frankfurter.app/latest?from=USD&to=BRL";

    @Cacheable(value = "cotacaoDolar", key = "'atual'")
    public BigDecimal obterCotacaoDolar() {
        log.info("Buscando cotação do dólar em tempo real...");
        try {
            return buscarAwesomeApi();
        } catch (Exception e) {
            log.warn("AwesomeAPI falhou, tentando Fallback (Frankfurter)... Reason: {}", e.getMessage());
            try {
                return buscarFrankfurterApi();
            } catch (Exception ex) {
                log.error("Ambas as APIs de câmbio falharam.");
                throw new IllegalStateException("Não foi possível obter a cotação do dólar para conversão.");
            }
        }
    }

    private BigDecimal buscarAwesomeApi() {
        Map response = restTemplate.getForObject(AWESOME_API, Map.class);
        Map<String, String> usdbrl = (Map<String, String>) response.get("USDBRL");
        return new BigDecimal(usdbrl.get("bid"));
    }

    private BigDecimal buscarFrankfurterApi() {
        Map response = restTemplate.getForObject(FRANKFURTER_API, Map.class);
        Map<String, Double> rates = (Map<String, Double>) response.get("rates");
        return BigDecimal.valueOf(rates.get("BRL"));
    }

    public BigDecimal converterParaDolar(BigDecimal precoEmBrl) {
        BigDecimal cotacao = obterCotacaoDolar();
        return precoEmBrl.divide(cotacao, 2, RoundingMode.HALF_UP);
    }
}