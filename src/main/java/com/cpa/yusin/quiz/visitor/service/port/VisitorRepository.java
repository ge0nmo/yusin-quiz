package com.cpa.yusin.quiz.visitor.service.port;

import com.cpa.yusin.quiz.visitor.domain.Visitor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitorRepository
{
    Visitor save(Visitor visitor);

    List<Visitor> saveAll(List<Visitor> visitors);

    Optional<Visitor> findById(long id);

    Optional<Visitor> findByIpAddressAndUserAgent(String ipAddress, String userAgent);

    long countByVisitedAt(LocalDate visitedAt);

    long countAll();
}
