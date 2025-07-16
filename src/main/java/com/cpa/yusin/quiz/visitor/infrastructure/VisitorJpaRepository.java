package com.cpa.yusin.quiz.visitor.infrastructure;

import com.cpa.yusin.quiz.visitor.controller.dto.DailyVisitorCountDto;
import com.cpa.yusin.quiz.visitor.domain.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitorJpaRepository extends JpaRepository<Visitor, Long>
{
    Optional<Visitor> findByIpAddressAndUserAgent(@Param("ipAddress") String ipAddress,
                                                  @Param("userAgent") String userAgent);

    long countByVisitedAt(@Param("visitedAt")LocalDate visitedAt);

    @Query("SELECT new com.cpa.yusin.quiz.visitor.controller.dto.DailyVisitorCountDto(" +
            "v.visitedAt, COUNT(v))" +
            "FROM Visitor v " +
            "WHERE v.visitedAt BETWEEN :start AND :end " +
            "GROUP BY v.visitedAt " +
            "ORDER BY v.visitedAt ASC")
    List<DailyVisitorCountDto> findDailyVisitorCounts(LocalDate start, LocalDate end);
}
