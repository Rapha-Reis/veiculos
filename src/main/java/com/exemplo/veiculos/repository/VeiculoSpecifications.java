package com.exemplo.veiculos.repository;

import com.exemplo.veiculos.domain.Veiculo;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public class VeiculoSpecifications {

    public static Specification<Veiculo> comFiltros(String marca, Integer ano, String cor, BigDecimal minPreco, BigDecimal maxPreco) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (marca != null && !marca.isBlank()) {
                predicates.getExpressions().add(cb.equal(cb.lower(root.get("marca")), marca.toLowerCase()));
            }
            if (ano != null) {
                predicates.getExpressions().add(cb.equal(root.get("ano"), ano));
            }
            if (cor != null && !cor.isBlank()) {
                predicates.getExpressions().add(cb.equal(cb.lower(root.get("cor")), cor.toLowerCase()));
            }
            if (minPreco != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("preco"), minPreco));
            }
            if (maxPreco != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("preco"), maxPreco));
            }

            return predicates;
        };
    }
}