package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
        @Query("SELECT m FROM Member m WHERE m.email = :email " +
                        "AND m.email != :withdrawnAuthorEmail")
        Optional<Member> findByEmail(@Param("email") String email,
                        @Param("withdrawnAuthorEmail") String withdrawnAuthorEmail);

        @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT m FROM Member m WHERE m.email = :email " +
                        "AND m.email != :withdrawnAuthorEmail")
        Optional<Member> findByEmailWithLock(@Param("email") String email,
                        @Param("withdrawnAuthorEmail") String withdrawnAuthorEmail);

        @Query("SELECT m FROM Member m WHERE m.email = :withdrawnAuthorEmail")
        Optional<Member> findWithdrawnAuthor(@Param("withdrawnAuthorEmail") String withdrawnAuthorEmail);

        @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT m FROM Member m WHERE m.email = :withdrawnAuthorEmail")
        Optional<Member> findWithdrawnAuthorWithLock(@Param("withdrawnAuthorEmail") String withdrawnAuthorEmail);

        boolean existsByEmail(@Param("email") String email);

        boolean existsByUsername(@Param("username") String username);

        @Query("SELECT m FROM Member m " +
                        "WHERE m.email != :withdrawnAuthorEmail " +
                        "AND (:keyword IS NULL OR " +
                        "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(m.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "ORDER BY m.createdAt")
        Page<Member> findAllByKeyword(@Param("keyword") String keyword,
                        @Param("withdrawnAuthorEmail") String withdrawnAuthorEmail,
                        Pageable pageable);

        @Query("SELECT m FROM Member m " +
                        "WHERE m.role != 'ADMIN' " +
                        "AND m.email != :withdrawnAuthorEmail " +
                        "AND (:keyword IS NULL OR " +
                        "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(m.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "ORDER BY m.createdAt")
        Page<Member> findAllByKeywordAndAdminNot(@Param("keyword") String keyword,
                        @Param("withdrawnAuthorEmail") String withdrawnAuthorEmail,
                        Pageable pageable);

        @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT m FROM Member m WHERE m.id = :id")
        Optional<Member> findByIdWithLock(@Param("id") long id);
}
