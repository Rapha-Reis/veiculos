package com.exemplo.veiculos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) 
public class NegocioException extends RuntimeException {
    
    public NegocioException(String message) {
        super(message);
    }
}