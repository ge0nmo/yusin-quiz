package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long>
{
    @Query("SELECT m FROM Member m WHERE m.email = :email")
    Optional<Member> findByEmail(@Param("email") String email);

    boolean existsByEmail(@Param("email") String email);


    @Query("SELECT m FROM Member m " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY m.createdAt")
    Page<Member> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Member m " +
            "WHERE m.role != 'ADMIN' " +
            "AND (:keyword IS NULL OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY m.createdAt")
    Page<Member> findAllByKeywordAndAdminNot(@Param("keyword") String keyword, Pageable pageable);
}
