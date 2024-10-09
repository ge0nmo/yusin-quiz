package com.cpa.yusin.quiz.subject.infrastructure;

import com.cpa.yusin.quiz.subject.domain.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubjectJpaRepository extends JpaRepository<Subject, Long>
{
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Subject s WHERE s.name = :name ")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Subject s " +
            "WHERE s.id != :id AND s.name = :name ")
    boolean existsByNameAndIdNot(@Param("id") long id, @Param("name") String name);

    @Query("SELECT s FROM Subject s ORDER BY s.name ASC ")
    Page<Subject> findAllOrderByNameAsc(Pageable pageable);
}
