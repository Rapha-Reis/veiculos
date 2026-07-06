package com.exemplo.veiculos.controller;

import com.exemplo.veiculos.dto.VeiculoRequestDTO;
import com.exemplo.veiculos.config.SecurityConfig;
import com.exemplo.veiculos.service.VeiculoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VeiculoController.class)
@Import(SecurityConfig.class)
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VeiculoService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void usuarioComPerfilUserNaoPodeCriarVeiculo() throws Exception {
        var dto = new VeiculoRequestDTO("AAA1111", "Audi", "A3", 2022, "Branco", BigDecimal.valueOf(150000));

        mockMvc.perform(post("/veiculos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden()); // 403 Forbidden
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void usuarioComPerfilAdminPodeCriarVeiculo() throws Exception {
        var dto = new VeiculoRequestDTO("AAA1111", "Audi", "A3", 2022, "Branco", BigDecimal.valueOf(150000));

        mockMvc.perform(post("/veiculos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void requisicaoSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isUnauthorized()); // 401 Unauthorized
    }
}