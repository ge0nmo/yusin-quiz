package com.cpa.yusin.quiz.visitor.infrastructure;

import com.cpa.yusin.quiz.visitor.domain.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface VisitorJpaRepository extends JpaRepository<Visitor, Long>
{
    Optional<Visitor> findByIpAddressAndUserAgent(@Param("ipAddress") String ipAddress,
                                                  @Param("userAgent") String userAgent);

    long countByVisitedAt(@Param("visitedAt")LocalDate visitedAt);
}
