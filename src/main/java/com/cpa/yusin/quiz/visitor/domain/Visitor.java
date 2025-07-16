package com.cpa.yusin.quiz.visitor.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ipAddress", "userAgent", "visitedAt"})
})
@Entity
public class Visitor
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userAgent;

    @Column(nullable = false)
    private LocalDate visitedAt;

    public static Visitor of(String ipAddress, String userAgent, LocalDate today)
    {
        Visitor visitor = new Visitor();
        visitor.ipAddress = ipAddress;
        visitor.userAgent = userAgent;
        visitor.visitedAt = today;
        return visitor;
    }
}
