package com.exemplo.veiculos;

import com.exemplo.veiculos.dto.VeiculoRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VeiculoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fluxoCompletoDeGerenciamentoDeVeiculos() throws Exception {
        var novoVeiculo = new VeiculoRequestDTO("XYZ8888", "BMW", "M3", 2023, "Azul", BigDecimal.valueOf(450000));

        // 1. Criar Veículo como ADMIN
        mockMvc.perform(post("/veiculos")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoVeiculo)))
                .andExpect(status().isCreated());

        // 2. Listar e Filtrar como USER (Para pegar o ID gerado)
        String responseBody = mockMvc.perform(get("/veiculos")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .param("marca", "BMW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].placa").value("XYZ8888"))
                .andReturn().getResponse().getContentAsString();

        // Extrai o ID do primeiro elemento do array da paginação
        Integer idGerado = com.jayway.jsonpath.JsonPath.read(responseBody, "$.content[0].id");

        // 3. Detalhar Veículo por ID (Requisito Final do fluxo E2E)
        mockMvc.perform(get("/veiculos/" + idGerado)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("XYZ8888"))
                .andExpect(jsonPath("$.modelo").value("M3"));
    }   
}