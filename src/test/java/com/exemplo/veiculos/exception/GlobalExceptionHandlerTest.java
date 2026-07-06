package com.exemplo.veiculos.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = Mockito.mock(WebRequest.class);
        // Simula o retorno do path que o seu método buildResponse limpa (.replace("uri=", ""))
        when(request.getDescription(false)).thenReturn("uri=/veiculos");
    }

    @Test
    void deveTratarEntityNotFoundException() {
        // Cenário
        var excecao = new EntityNotFoundException("Veículo com ID 1 não encontrado");

        // Execução
        ResponseEntity<ErrorResponse> resposta = handler.handleNotFound(excecao, request);

        // Asserções
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().getStatus()).isEqualTo(404);
        assertThat(resposta.getBody().getError()).isEqualTo("Recurso não encontrado");
        assertThat(resposta.getBody().getMessage()).isEqualTo("Veículo com ID 1 não encontrado");
        assertThat(resposta.getBody().getPath()).isEqualTo("/veiculos");
    }

    @Test
    void deveTratarNegocioException() {
        // Cenário
        var excecao = new NegocioException("Placa já cadastrada no sistema");

        // Execução
        ResponseEntity<ErrorResponse> resposta = handler.handleNegocio(excecao, request);

        // Asserções
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().getStatus()).isEqualTo(409);
        assertThat(resposta.getBody().getError()).isEqualTo("Conflito de Regra de Negócio");
        assertThat(resposta.getBody().getMessage()).isEqualTo("Placa já cadastrada no sistema");
    }

    @Test
    void deveTratarMethodArgumentNotValidException() throws Exception {
        // Cenário complexo: precisamos mockar a estrutura de erro de validação do Spring
        var bindingResult = Mockito.mock(BindingResult.class);
        var fieldError = new FieldError("veiculoRequestDTO", "placa", "A placa é obrigatória");
        
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Cria a exceção injetando o bindingResult mockado
        var metodo = this.getClass().getDeclaredMethod("setUp"); // Apenas um método qualquer para satisfazer o construtor
        var parametro = new MethodParameter(metodo, -1);
        var excecao = new MethodArgumentNotValidException(parametro, bindingResult);

        // Execução
        ResponseEntity<ErrorResponse> resposta = handler.handleValidation(excecao, request);

        // Asserções
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().getStatus()).isEqualTo(400);
        assertThat(resposta.getBody().getError()).isEqualTo("Erro de validação");
        
        // Verifica se a lista de sub-erros de campos foi preenchida corretamente
        assertThat(resposta.getBody().getErrors()).hasSize(1);
        assertThat(resposta.getBody().getErrors().get(0).getField()).isEqualTo("placa");
        assertThat(resposta.getBody().getErrors().get(0).getMessage()).isEqualTo("A placa é obrigatória");
    }
}