package com.cpa.yusin.quiz.subject.infrastructure;

import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectJpaRepository extends JpaRepository<Subject, Long>
{
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Subject s WHERE s.name = :name AND s.isRemoved = false ")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Subject s " +
            "WHERE s.id != :id AND s.name = :name AND s.isRemoved = false ")
    boolean existsByNameAndIdNot(@Param("id") long id, @Param("name") String name);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.isRemoved = false " +
            "ORDER BY s.name ASC ")
    Page<Subject> findAllOrderByNameAsc(Pageable pageable);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.isRemoved = false " +
            "AND (s.status = :publishedStatus OR s.status IS NULL) " +
            "ORDER BY s.name ASC ")
    Page<Subject> findAllPublishedOrderByNameAsc(@Param("publishedStatus") SubjectStatus publishedStatus, Pageable pageable);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.isRemoved = false " +
            "ORDER BY s.name ASC ")
    List<Subject> findAllByIsRemovedFalseOrderByNameAsc();

    @Query("SELECT s FROM Subject s " +
            "WHERE s.isRemoved = false " +
            "AND (s.status = :publishedStatus OR s.status IS NULL) " +
            "ORDER BY s.name ASC ")
    List<Subject> findAllPublishedByIsRemovedFalseOrderByNameAsc(@Param("publishedStatus") SubjectStatus publishedStatus);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.id = :id " +
            "AND s.isRemoved = false ")
    Optional<Subject> findByIdAndIsRemovedFalse(@Param("id") long id);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.id = :id " +
            "AND s.isRemoved = false " +
            "AND (s.status = :publishedStatus OR s.status IS NULL)")
    Optional<Subject> findPublishedById(@Param("id") long id, @Param("publishedStatus") SubjectStatus publishedStatus);
}
