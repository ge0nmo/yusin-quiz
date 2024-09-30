package com.cpa.yusin.quiz.product.infrastructure;

import com.cpa.yusin.quiz.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long>
{
    @Query("SELECT p FROM Product p WHERE p.durationMonth = :month ")
    Optional<Product> findByDurationMonth(@Param("month") Integer month);

    boolean existsByDurationMonth(@Param("durationMonth") Integer durationMonth);

    boolean existsByDurationMonthAndIdNot(@Param("durationMonth") Integer durationMonth, @Param("id") Long id);
}
