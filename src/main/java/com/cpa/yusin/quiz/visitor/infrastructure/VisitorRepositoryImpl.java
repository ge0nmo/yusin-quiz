package com.cpa.yusin.quiz.visitor.infrastructure;

import com.cpa.yusin.quiz.visitor.controller.dto.DailyVisitorCountDto;
import com.cpa.yusin.quiz.visitor.domain.Visitor;
import com.cpa.yusin.quiz.visitor.service.port.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    public List<DailyVisitorCountDto> countByVisitedAtBetween(LocalDate start, LocalDate end)
    {
        return visitorJpaRepository.findDailyVisitorCounts(start, end);
    }

}
