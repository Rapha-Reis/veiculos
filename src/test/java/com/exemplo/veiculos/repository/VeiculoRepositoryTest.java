package com.exemplo.veiculos.repository;

import com.exemplo.veiculos.domain.Veiculo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class VeiculoRepositoryTest {

    @Autowired
    private VeiculoRepository repository;

    @Test
    void deveFiltrarEFiltrarComTodasAsRamificacoesDoSpecification() {
        // Usamos UUID na placa para garantir que este teste gere dados únicos 
        // e nunca estoure Unique Constraint, independente do que já tem no banco do Docker
        String placaAtivo = "ATV" + UUID.randomUUID().toString().substring(0, 4);
        String placaInativo = "INA" + UUID.randomUUID().toString().substring(0, 4);

        var v1 = Veiculo.builder().placa(placaAtivo).marca("Audi").modelo("A3").ano(2022).cor("Preto").preco(BigDecimal.valueOf(30000)).ativo(true).build();
        var v2 = Veiculo.builder().placa(placaInativo).marca("Audi").modelo("A4").ano(2021).cor("Preto").preco(BigDecimal.valueOf(25000)).ativo(false).build(); 

        repository.save(v1);
        repository.save(v2);

        // Executa o filtro de cor "Preto" e faixa de preço
        Specification<Veiculo> spec = VeiculoSpecifications.comFiltros(null, null, "Preto", BigDecimal.valueOf(20000), BigDecimal.valueOf(35000));
        Page<Veiculo> result = repository.findAll(spec, PageRequest.of(0, 10));

        // 1. Extraímos apenas as placas do resultado obtido
        var placasResultantes = result.getContent().stream().map(Veiculo::getPlaca).toList();
        // 2. O carro ativo criado obrigatoriamente precisa estar no resultado
        assertThat(placasResultantes).contains(placaAtivo);
        // 3. O carro inativo criado não pode aparecer
        assertThat(placasResultantes).doesNotContain(placaInativo);
    }
}