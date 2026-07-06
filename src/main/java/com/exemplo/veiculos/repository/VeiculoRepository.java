package com.exemplo.veiculos.repository;

import com.exemplo.veiculos.domain.Veiculo;
import com.exemplo.veiculos.dto.RelatorioMarcaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long>, JpaSpecificationExecutor<Veiculo> {
    
    Optional<Veiculo> findByPlaca(String placa);
    
    boolean existsByPlacaAndIdNot(String placa, Long id);

    @Query("SELECT new com.exemplo.veiculos.dto.RelatorioMarcaDTO(v.marca, COUNT(v)) " +
           "FROM Veiculo v GROUP BY v.marca ORDER BY COUNT(v) DESC")
    List<RelatorioMarcaDTO> obterRelatorioPorMarca();
}