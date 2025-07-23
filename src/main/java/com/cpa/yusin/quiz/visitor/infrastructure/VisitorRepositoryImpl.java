package com.cpa.yusin.quiz.visitor.infrastructure;

import com.cpa.yusin.quiz.visitor.controller.dto.DailyVisitorCountDto;
import com.cpa.yusin.quiz.visitor.domain.Visitor;
import com.cpa.yusin.quiz.visitor.service.port.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class VisitorRepositoryImpl implements VisitorRepository
{
    private final VisitorJpaRepository visitorJpaRepository;

    @Override
    public Visitor save(Visitor visitor)
    {
        return visitorJpaRepository.save(visitor);
    }

    @Override
    public List<Visitor> saveAll(List<Visitor> visitors)
    {
        return visitorJpaRepository.saveAll(visitors);
    }

    @Override
    public Optional<Visitor> findById(long id)
    {
        return visitorJpaRepository.findById(id);
    }

    @Override
    public Optional<Visitor> findByIpAddressAndUserAgent(String ipAddress, String userAgent)
    {
        return visitorJpaRepository.findByIpAddressAndUserAgent(ipAddress, userAgent);
    }

    @Override
    public long countByVisitedAt(LocalDate visitedAt)
    {
        return visitorJpaRepository.countByVisitedAt(visitedAt);
    }

    @Override
    public List<DailyVisitorCountDto> countByVisitedAtBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return visitorJpaRepository.findDailyVisitorCounts(start, end)
                .stream()
                .map(row -> new DailyVisitorCountDto(
                        ((Date) row[0]).toLocalDate(),
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }



}
