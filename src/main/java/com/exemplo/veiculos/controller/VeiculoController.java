package com.exemplo.veiculos.controller;

import com.exemplo.veiculos.domain.Veiculo;
import com.exemplo.veiculos.dto.RelatorioMarcaDTO;
import com.exemplo.veiculos.dto.VeiculoRequestDTO;
import com.exemplo.veiculos.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService service;

    @GetMapping
    public ResponseEntity<Page<Veiculo>> listar(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cor,
            @RequestParam(required = false) BigDecimal minPreco,
            @RequestParam(required = false) BigDecimal maxPreco,
            @PageableDefault(size = 10, sort = "marca") Pageable pageable) {
        
        Page<Veiculo> pagina = service.listar(marca, ano, cor, minPreco, maxPreco, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Veiculo> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/relatorios/por-marca")
    public ResponseEntity<List<RelatorioMarcaDTO>> obterRelatorioPorMarca() {
        return ResponseEntity.ok(service.obterRelatorioPorMarca()); // Adicione esse método repassando do repository
    }

    @PostMapping
    public ResponseEntity<Veiculo> criar(@RequestBody @Valid VeiculoRequestDTO dto) {
        Veiculo novoVeiculo = service.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoVeiculo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Veiculo> atualizar(@PathVariable Long id, @RequestBody @Valid VeiculoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Veiculo> atualizarParcial(@PathVariable Long id, @RequestBody VeiculoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizarParcial(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}