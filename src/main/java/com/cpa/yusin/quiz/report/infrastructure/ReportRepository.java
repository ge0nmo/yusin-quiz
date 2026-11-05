package com.cpa.yusin.quiz.report.infrastructure;

import com.cpa.yusin.quiz.report.domain.Report;
import com.cpa.yusin.quiz.report.domain.ReportCategory;
import com.cpa.yusin.quiz.report.domain.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    String ADMIN_FILTER = "where r.status in :statuses and r.category in :categories " +
            "and (:qualificationExamId is null or r.qualificationExam.id = :qualificationExamId) ";
    String ADMIN_SELECT = "select r from Report r join fetch r.qualificationExam " +
            "left join fetch r.problem p left join fetch p.subjectMapping ";
    String ADMIN_COUNT = "select count(r) from Report r " + ADMIN_FILTER;

    record StatusCount(ReportStatus status, long count) {}

    record ProblemCount(Long problemId, long count) {}

    long countByDeviceIdAndCreatedAtAfter(String deviceId, LocalDateTime since);

    boolean existsByDeviceIdAndProblemIdAndCategoryAndStatusIn(String deviceId, Long problemId,
                                                               ReportCategory category,
                                                               Collection<ReportStatus> statuses);

    @EntityGraph(attributePaths = {"problem"})
    List<Report> findTop100ByDeviceIdAndQualificationExamIdOrderByCreatedAtDescIdDesc(String deviceId,
                                                                                     Long qualificationExamId);

    @EntityGraph(attributePaths = {"problem"})
    Optional<Report> findByIdAndDeviceIdAndQualificationExamId(Long id, String deviceId, Long qualificationExamId);

    @EntityGraph(attributePaths = {"qualificationExam", "problem", "problem.subjectMapping"})
    @Query("select r from Report r where r.id = :id")
    Optional<Report> findDetailById(@Param("id") Long id);

    long countByDeviceId(String deviceId);

    long countByDeviceIdAndStatus(String deviceId, ReportStatus status);

    long countByStatusIn(Collection<ReportStatus> statuses);

    boolean existsByQualificationExamId(Long qualificationExamId);

    List<Report> findAllByProblemIdAndStatusInAndIdNotOrderByCreatedAtDescIdDesc(Long problemId,
                                                                                 Collection<ReportStatus> statuses,
                                                                                 Long id);

    /** 같은 문제의 미처리 신고 수가 많은 순. 문제가 없는 신고는 하위 쿼리가 0이 된다. */
    @Query(value = ADMIN_SELECT + ADMIN_FILTER +
            "order by (select count(o) from Report o where o.problem = r.problem " +
            "and o.status in (com.cpa.yusin.quiz.report.domain.ReportStatus.RECEIVED, " +
            "com.cpa.yusin.quiz.report.domain.ReportStatus.IN_REVIEW)) desc, r.createdAt desc, r.id desc",
            countQuery = ADMIN_COUNT)
    Page<Report> searchBySameProblem(@Param("statuses") Collection<ReportStatus> statuses,
                                     @Param("categories") Collection<ReportCategory> categories,
                                     @Param("qualificationExamId") Long qualificationExamId,
                                     Pageable pageable);

    @Query(value = ADMIN_SELECT + ADMIN_FILTER + "order by r.createdAt desc, r.id desc", countQuery = ADMIN_COUNT)
    Page<Report> searchLatest(@Param("statuses") Collection<ReportStatus> statuses,
                              @Param("categories") Collection<ReportCategory> categories,
                              @Param("qualificationExamId") Long qualificationExamId,
                              Pageable pageable);

    @Query(value = ADMIN_SELECT + ADMIN_FILTER + "order by r.createdAt asc, r.id asc", countQuery = ADMIN_COUNT)
    Page<Report> searchOldest(@Param("statuses") Collection<ReportStatus> statuses,
                              @Param("categories") Collection<ReportCategory> categories,
                              @Param("qualificationExamId") Long qualificationExamId,
                              Pageable pageable);

    @Query("select new com.cpa.yusin.quiz.report.infrastructure.ReportRepository$ProblemCount(r.problem.id, count(r)) " +
            "from Report r where r.problem.id in :problemIds and r.status in :statuses group by r.problem.id")
    List<ProblemCount> countByProblemIds(@Param("problemIds") Collection<Long> problemIds,
                                         @Param("statuses") Collection<ReportStatus> statuses);

    @Query("select new com.cpa.yusin.quiz.report.infrastructure.ReportRepository$StatusCount(r.status, count(r)) " +
            "from Report r where (:qualificationExamId is null or r.qualificationExam.id = :qualificationExamId) " +
            "group by r.status")
    List<StatusCount> countByStatus(@Param("qualificationExamId") Long qualificationExamId);

    /** 문제를 지우기 전에 그 문제를 가리키는 신고를 끊는다. 스냅숏 컬럼은 그대로 둔다. */
    @Modifying(flushAutomatically = true)
    @Query("update Report r set r.problem = null where r.problem.id = :problemId")
    int detachProblem(@Param("problemId") Long problemId);
}
