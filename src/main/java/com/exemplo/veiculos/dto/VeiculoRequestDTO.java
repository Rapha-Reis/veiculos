package com.exemplo.veiculos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record VeiculoRequestDTO(
    @NotBlank(message = "A placa é obrigatória.")
    @Size(min = 7, max = 7, message = "A placa deve ter exatamente 7 caracteres.")
    String placa,

    @NotBlank(message = "A marca é obrigatória.")
    String marca,

    @NotBlank(message = "O modelo é obrigatório.")
    String modelo,

    @NotNull(message = "O ano é obrigatório.")
    @Min(value = 1900, message = "O ano deve ser maior que 1900.")
    Integer ano,

    @NotBlank(message = "A cor é obrigatória.")
    String cor,

    @NotNull(message = "O preço é obrigatório.")
    @Positive(message = "O preço deve ser um valor positivo.")
    BigDecimal preco
) {}