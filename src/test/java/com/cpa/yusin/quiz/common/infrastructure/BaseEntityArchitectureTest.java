package com.cpa.yusin.quiz.common.infrastructure;

import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityArchitectureTest {

    @Test
    void everyJpaEntityShouldInheritBaseEntity() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        List<String> violations = new ArrayList<>();

        for (var component : scanner.findCandidateComponents("com.cpa.yusin.quiz")) {
            Class<?> entityClass = Class.forName(component.getBeanClassName());

            if (!BaseEntity.class.isAssignableFrom(entityClass)) {
                violations.add(entityClass.getName());
            }
        }

        assertThat(violations)
                .as("모든 JPA 엔티티는 감사 컬럼 일관성을 위해 BaseEntity를 상속해야 함")
                .isEmpty();
    }
}
