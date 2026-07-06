package com.exemplo.veiculos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_veiculos", uniqueConstraints = {
    @UniqueConstraint(columnNames = "placa", name = "uk_veiculo_placa")
})
@SQLDelete(sql = "UPDATE tb_veiculos SET ativo = false WHERE id = ?")
@SQLRestriction("ativo = true")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 7, max = 7)
    @Column(nullable = false, length = 7)
    private String placa;

    @NotBlank
    @Column(nullable = false)
    private String marca;

    @NotBlank
    @Column(nullable = false)
    private String modelo;

    @NotNull @Min(1900)
    @Column(nullable = false)
    private Integer ano;

    @NotBlank
    @Column(nullable = false)
    private String cor;

    @NotNull @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;
}