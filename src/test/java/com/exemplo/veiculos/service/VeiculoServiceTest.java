package com.exemplo.veiculos.service;

import com.exemplo.veiculos.domain.Veiculo;
import com.exemplo.veiculos.dto.VeiculoRequestDTO;
import com.exemplo.veiculos.exception.NegocioException;
import com.exemplo.veiculos.repository.VeiculoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository repository;

    @Mock
    private CambioService cambioService;

    @InjectMocks
    private VeiculoService veiculoService;

    @Test
    void deveLancarNegocioExceptionAoSalvarComPlacaDuplicada() {
        var dto = new VeiculoRequestDTO("ABC1234", "Ford", "Ka", 2020, "Preto", BigDecimal.valueOf(40000));
        when(repository.findByPlaca("ABC1234")).thenReturn(Optional.of(new Veiculo()));

        assertThatThrownBy(() -> veiculoService.salvar(dto))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Já existe um veículo cadastrado com a placa");

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarExceptionAoBuscarIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);                
    }

    @Test
    void deveLancarExceptionAoAtualizarIdInexistente() {
        var dto = new VeiculoRequestDTO("XYZ8888", "BMW", "M3", 2023, "Azul", BigDecimal.valueOf(450000));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.atualizar(99L, dto))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
                
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtualizarVeiculoComSucesso() {
        
        Long idExistente = 1L;
        var dto = new VeiculoRequestDTO("XYZ8888", "BMW", "M3", 2023, "Azul", BigDecimal.valueOf(450000));
        
        var veiculoExistente = Veiculo.builder()
                .id(idExistente).placa("XYZ8888").marca("Toyota").modelo("Corolla").ano(2022).cor("Preto").preco(BigDecimal.valueOf(150000)).ativo(true).build();

        when(repository.findById(idExistente)).thenReturn(Optional.of(veiculoExistente));
        when(repository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        Veiculo veiculoAtualizado = veiculoService.atualizar(idExistente, dto);

        assertThat(veiculoAtualizado.getMarca()).isEqualTo("BMW");
        assertThat(veiculoAtualizado.getModelo()).isEqualTo("M3");
        assertThat(veiculoAtualizado.getCor()).isEqualTo("Azul");
        verify(repository, times(1)).save(veiculoExistente);
    }

    @Test
    void deveAtualizarParcialmenteComSucesso() {
        // Cenário: No PATCH, o DTO pode vir com campos nulos (ex: mudando apenas a cor e o preço)
        Long idExistente = 1L;
        var dtoPatch = new VeiculoRequestDTO(null, null, null, null, "Branco", BigDecimal.valueOf(160000));
        
        var veiculoExistente = Veiculo.builder()
                .id(idExistente).placa("XYZ8888").marca("Toyota").modelo("Corolla").ano(2022).cor("Preto").preco(BigDecimal.valueOf(150000)).ativo(true).build();

        when(repository.findById(idExistente)).thenReturn(Optional.of(veiculoExistente));
        when(repository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo veiculoPatch = veiculoService.atualizarParcial(idExistente, dtoPatch);

        assertThat(veiculoPatch.getCor()).isEqualTo("Branco"); // Mudou
        assertThat(veiculoPatch.getMarca()).isEqualTo("Toyota"); // Manteve o original
        assertThat(veiculoPatch.getModelo()).isEqualTo("Corolla"); // Manteve o original
        verify(repository, times(1)).save(veiculoExistente);
    }

    @Test
    void deveAtualizarParcialmenteTodosOsCamposPreenchidos() {
        // Cenário: DTO com todos os campos preenchidos para forçar a cobertura de todas as branches (ifs)
        Long idExistente = 1L;
        var dtoPatchCompleto = new VeiculoRequestDTO("ABC1234", "Honda", "Civic", 2025, "Cinza", BigDecimal.valueOf(180000));
        
        var veiculoExistente = Veiculo.builder()
                .id(idExistente).placa("XYZ8888").marca("Toyota").modelo("Corolla").ano(2022).cor("Preto").preco(BigDecimal.valueOf(150000)).ativo(true).build();

        when(repository.findById(idExistente)).thenReturn(Optional.of(veiculoExistente));
        when(repository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo veiculoResult = veiculoService.atualizarParcial(idExistente, dtoPatchCompleto);

        // Asserções: Garante que passou por dentro de cada IF modificando as propriedades
        assertThat(veiculoResult.getPlaca()).isEqualTo("ABC1234");
        assertThat(veiculoResult.getMarca()).isEqualTo("Honda");
        assertThat(veiculoResult.getModelo()).isEqualTo("Civic");
        assertThat(veiculoResult.getCor()).isEqualTo("Cinza");
        verify(repository, times(1)).save(veiculoExistente);
    }

    @Test
    void deveLancarExcecaoAoAtualizarParcialComIdInexistente() {
        // Cenário
        Long idInexistente = 99L;
        var dto = new VeiculoRequestDTO("ABC1234", "Honda", "Civic", 2025, "Cinza", BigDecimal.valueOf(180000));

        when(repository.findById(idInexistente)).thenReturn(Optional.empty());
        // Ajuste "RuntimeException.class" para a sua exceção customizada (ex: VeiculoNotFoundException.class)
        assertThatThrownBy(() -> veiculoService.atualizarParcial(idInexistente, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Veículo não encontrado"); // Ajuste a mensagem real do seu service

        verify(repository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComIdInexistente() {
        // Cenário
        Long idInexistente = 99L;
        var dto = new VeiculoRequestDTO("ABC1234", "Honda", "Civic", 2025, "Cinza", BigDecimal.valueOf(180000));

        when(repository.findById(idInexistente)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> veiculoService.atualizar(idInexistente, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Veículo não encontrado");

        verify(repository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveIgnorarCamposVaziosNoAtualizarParcial() {
        // Cenário: Strings vazias que não devem sobrescrever o valor se houver validação .isBlank() ou .isEmpty()
        Long idExistente = 1L;
        var dtoCamposVazios = new VeiculoRequestDTO("", "", "", null, "", null);
        
        var veiculoExistente = Veiculo.builder()
                .id(idExistente).placa("XYZ8888").marca("Toyota").modelo("Corolla").ano(2022).cor("Preto").preco(BigDecimal.valueOf(150000)).ativo(true).build();

        when(repository.findById(idExistente)).thenReturn(Optional.of(veiculoExistente));
        when(repository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo veiculoResult = veiculoService.atualizarParcial(idExistente, dtoCamposVazios);

        assertThat(veiculoResult.getPlaca()).isEqualTo("XYZ8888");
        assertThat(veiculoResult.getMarca()).isEqualTo("Toyota");
        verify(repository, times(1)).save(veiculoExistente);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComPlacaEmUsoPorOutroVeiculo() {
        Long idExistente = 1L;
        var dto = new VeiculoRequestDTO("OFT8X32", "Toyota", "Corolla", 2024, "Preto", BigDecimal.valueOf(150000));
        var veiculoExistente = Veiculo.builder().id(idExistente).placa("XYZ8888").build();

        when(repository.findById(idExistente)).thenReturn(Optional.of(veiculoExistente));
        // Simula que a placa enviada já pertence a outro ID
        when(repository.existsByPlacaAndIdNot("OFT8X32", idExistente)).thenReturn(true);
        assertThatThrownBy(() -> veiculoService.atualizar(idExistente, dto))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("já está em uso por outro veículo");

        verify(repository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarParcialComPlacaEmUsoPorOutroVeiculo() {
        Long idExistente = 1L;
        var dto = new VeiculoRequestDTO("OFT8X32", null, null, null, null, null);
        var veiculoExistente = Veiculo.builder().id(idExistente).placa("XYZ8888").build();

        when(repository.findById(idExistente)).thenReturn(Optional.of(veiculoExistente));
        when(repository.existsByPlacaAndIdNot("OFT8X32", idExistente)).thenReturn(true);
        assertThatThrownBy(() -> veiculoService.atualizarParcial(idExistente, dto))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("já está em uso");

        verify(repository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveAtualizarParcialApenasAlgunsCampos() {
        Long idExistente = 1L;
        var dto = new VeiculoRequestDTO("NOVA123", null, null, 2026, "Vermelho", null);
        
        var veiculoExistente = Veiculo.builder()
                .id(idExistente).placa("XYZ8888").marca("Toyota").modelo("Corolla").ano(2022).cor("Preto").preco(BigDecimal.valueOf(150000)).build();

        when(repository.findById(idExistente)).thenReturn(Optional.of(veiculoExistente));
        when(repository.existsByPlacaAndIdNot("NOVA123", idExistente)).thenReturn(false);
        when(repository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = veiculoService.atualizarParcial(idExistente, dto);

        assertThat(resultado.getPlaca()).isEqualTo("NOVA123");
        assertThat(resultado.getAno()).isEqualTo(2026);
        assertThat(resultado.getCor()).isEqualTo("Vermelho");
        assertThat(resultado.getMarca()).isEqualTo("Toyota"); // Manteve original
        assertThat(resultado.getModelo()).isEqualTo("Corolla"); // Manteve original
        verify(repository, times(1)).save(veiculoExistente);
    }

    @Test
    void deveAplicarSoftDeleteComSucesso() {
        Long idExistente = 1L;
        var veiculo = Veiculo.builder().id(idExistente).ativo(true).build();
        when(repository.findById(idExistente)).thenReturn(Optional.of(veiculo));
        when(repository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        veiculoService.deletar(idExistente);

        assertThat(veiculo.getAtivo()).isFalse();
        verify(repository, times(1)).save(veiculo);
        verify(repository, never()).delete(any(Veiculo.class));
    }

}