package com.cpa.yusin.quiz.visitor.infrastructure;

import com.cpa.yusin.quiz.visitor.controller.dto.DailyVisitorCountDto;
import com.cpa.yusin.quiz.visitor.domain.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitorJpaRepository extends JpaRepository<Visitor, Long>
{
    Optional<Visitor> findByIpAddressAndUserAgent(@Param("ipAddress") String ipAddress,
                                                  @Param("userAgent") String userAgent);

    long countByVisitedAt(@Param("visitedAt")LocalDate visitedAt);


    @Query(nativeQuery = true, value = """
        SELECT CAST(v.visited_at AS date) as date, COUNT(DISTINCT v.id) as count
            FROM visitor v 
            WHERE v.visited_at BETWEEN :start AND :end 
            GROUP BY CAST(v.visited_at AS date) 
            ORDER BY date
    """)
    List<Object[]> findDailyVisitorCounts(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    
}
