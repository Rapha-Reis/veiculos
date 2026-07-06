package com.exemplo.veiculos.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(NegocioException.class) // Exceção customizada para 409 / regras de negócio
    public ResponseEntity<ErrorResponse> handleNegocio(NegocioException ex, WebRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflito de Regra de Negócio", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> ErrorResponse.ValidationError.builder().field(f.getField()).message(f.getDefaultMessage()).build())
                .toList();

        var error = buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação", "Dados inválidos enviados no payload", request).getBody();
        error = ErrorResponse.builder().timestamp(error.getTimestamp()).status(error.getStatus()).error(error.getError())
                .message(error.getMessage()).path(error.getPath()).errors(fieldErrors).build();

        return ResponseEntity.badRequest().body(error);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String msg, WebRequest request) {
        var body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(msg)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(status).body(body);
    }
}