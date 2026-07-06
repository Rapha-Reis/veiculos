package com.exemplo.veiculos.service;

import com.exemplo.veiculos.domain.Veiculo;
import com.exemplo.veiculos.dto.VeiculoRequestDTO;
import com.exemplo.veiculos.exception.NegocioException;
import com.exemplo.veiculos.repository.VeiculoRepository;
import com.exemplo.veiculos.repository.VeiculoSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository repository;
    private final CambioService cambioService;

    @Transactional(readOnly = true)
    public Page<Veiculo> listar(String marca, Integer ano, String cor, BigDecimal minPreco, BigDecimal maxPreco, Pageable pageable) {
        Specification<Veiculo> spec = VeiculoSpecifications.comFiltros(marca, ano, cor, minPreco, maxPreco);
        return repository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Veiculo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado com o ID: " + id));
    }

    @Transactional
    public Veiculo salvar(VeiculoRequestDTO dto) {
        repository.findByPlaca(dto.placa()).ifPresent(v -> {
            throw new NegocioException("Já existe um veículo cadastrado com a placa: " + dto.placa());
        });

        // Converte o preço enviado em Reais para Dólar antes de salvar
        BigDecimal precoEmDolar = cambioService.converterParaDolar(dto.preco());

        var veiculo = Veiculo.builder()
                .placa(dto.placa()).marca(dto.marca()).modelo(dto.modelo())
                .ano(dto.ano()).cor(dto.cor()).preco(precoEmDolar).build();
        return repository.save(veiculo);
    }
    
    @Transactional
    public Veiculo atualizar(Long id, VeiculoRequestDTO dto) {
        var veiculo = buscarPorId(id);
        
        if (repository.existsByPlacaAndIdNot(dto.placa(), id)) {
            throw new NegocioException("A placa " + dto.placa() + " já está em uso por outro veículo.");
        }

        veiculo.setPlaca(dto.placa());
        veiculo.setMarca(dto.marca());
        veiculo.setModelo(dto.modelo());
        veiculo.setAno(dto.ano());
        veiculo.setCor(dto.cor());
        veiculo.setPreco(dto.preco());

        return repository.save(veiculo);
    }

    @Transactional
    public Veiculo atualizarParcial(Long id, VeiculoRequestDTO dto) {
        var veiculo = buscarPorId(id);

        // Valida null E se não está vazio/em branco
        if (dto.placa() != null && !dto.placa().isBlank()) {
            if (repository.existsByPlacaAndIdNot(dto.placa(), id)) {
                throw new NegocioException("A placa " + dto.placa() + " já está em uso.");
            }
            veiculo.setPlaca(dto.placa());
        }
        if (dto.marca() != null && !dto.marca().isBlank()) veiculo.setMarca(dto.marca());
        if (dto.modelo() != null && !dto.modelo().isBlank()) veiculo.setModelo(dto.modelo());
        if (dto.cor() != null && !dto.cor().isBlank()) veiculo.setCor(dto.cor());
        
        // Tipos numéricos continuam apenas com null check
        if (dto.ano() != null) veiculo.setAno(dto.ano());
        if (dto.preco() != null) veiculo.setPreco(dto.preco());

        return repository.save(veiculo);
    }

    @Transactional(readOnly = true)
    public List<com.exemplo.veiculos.dto.RelatorioMarcaDTO> obterRelatorioPorMarca() {
        return repository.obterRelatorioPorMarca();
    }

    @Transactional
    public void deletar(Long id) {
        var veiculo = buscarPorId(id);
        veiculo.setAtivo(false); 
        repository.save(veiculo); 
    }
}