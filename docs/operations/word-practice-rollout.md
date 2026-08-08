# 말문제 빠른 풀이 공개 전 데이터 감사

말문제 빠른 풀이는 공개된 subject와 exam 아래에서 삭제되지 않았고
`requiresCalculation = false`인 문제만 사용한다. 문제 본문을 분석해 계산문제를
자동으로 추론하거나 분류 값을 보정하지 않는다.

## 감사 항목

운영 DB에서 공개·비삭제 subject/exam/problem만 대상으로 아래 수치를 subject별로 확인한다.

- 전체 공개 문제 수
- `requiresCalculation = false`인 말문제 수
- `requiresCalculation = true`인 계산문제 수

```sql
SELECT
    s.id AS subject_id,
    s.name AS subject_name,
    COUNT(p.id) AS total_public_problem_count,
    SUM(p.requires_calculation = FALSE) AS word_problem_count,
    SUM(p.requires_calculation = TRUE) AS calculation_problem_count
FROM subject s
LEFT JOIN exam e
    ON e.subject_id = s.id
   AND e.is_removed = FALSE
   AND e.status = 'PUBLISHED'
LEFT JOIN problem p
    ON p.exam_id = e.id
   AND p.is_removed = FALSE
WHERE s.is_removed = FALSE
  AND (s.status = 'PUBLISHED' OR s.status IS NULL)
GROUP BY s.id, s.name
ORDER BY s.name ASC;
```

`requiresCalculation` 분류가 검증되지 않은 문제는 사람이 직접 확인한다. 기본값이
`false`라는 이유만으로 계산문제를 말문제로 공개해서는 안 된다.

## 공개 체크리스트

- [ ] 공개 subject별 전체 문제 수와 말문제/계산문제 수를 검토했다.
- [ ] 모든 공개 문제의 `requiresCalculation` 값을 사람이 검증했다.
- [ ] 계산문제를 본문 정규식·AI·키워드로 자동 분류하지 않았다.
- [ ] 말문제가 0개인 subject는 앱에서 비활성 상태로 보이는지 확인했다.
- [ ] 운영 데이터에서 DRAFT 또는 삭제된 subject, exam, problem이 결과에 없는지 확인했다.
- [ ] legacy `subject.status IS NULL` 행도 기존 정책대로 공개 subject에 포함해 검증했다.

## 스키마와 인덱스 배포

운영 환경도 현재 `spring.jpa.hibernate.ddl-auto=update`를 사용하므로 첫 애플리케이션 기동이
기존 `exam`, `problem` 테이블의 복합 인덱스 생성까지 맡게 두면 안 된다. 데이터가 많은
테이블의 `ALTER TABLE`은 기동 지연이나 metadata lock을 만들 수 있다.

배포 전에 아래 순서로 스키마를 적용한다.

1. 운영과 같은 MySQL 버전의 staging DB에서 Hibernate가 생성할 DDL을 먼저 확인한다.
2. `word_practice_participant`, `word_practice_cycle`, `word_practice_answer` 테이블과 unique/FK를 사전 생성한다.
3. `exam(subject_id, status, is_removed, id)`와
   `problem(exam_id, requires_calculation, is_removed, id)` 인덱스는 운영 DB가 지원하는
   online DDL 방식으로 별도 적용한다.
4. `SHOW INDEX`와 실제 말문제 count/candidate 쿼리의 `EXPLAIN`으로 적용 결과를 확인한다.
5. 사전 DDL이 완료된 뒤 애플리케이션을 rolling deploy하고, 시작 로그에 추가 schema update가 없는지 확인한다.

unique `(participant_id, subject_id, round_number)`가 같은 컬럼 순서의 조회 인덱스 역할도
하므로 동일한 일반 인덱스를 중복 생성하지 않는다.

## 익명 사용자 남용 방지

token 없는 익명 cycle 시작은 participant와 전체 문제 순서 snapshot을 새로 저장하므로
일반 조회보다 엄격하게 제한한다. 애플리케이션 인스턴스별 메모리 제한은 다중 인스턴스에서
우회될 수 있으므로 gateway/WAF처럼 모든 인스턴스 앞단에서 적용한다.

- [ ] `POST /api/v2/problem/word-practice/subjects/*/cycle`에 IP 기준 burst·분당 제한을 적용했다.
- [ ] cycle 시작, participant 생성, 429 응답, 후보 문제 수와 응답 시간을 모니터링한다.
- [ ] 정상 React Native 재시도는 같은 `X-Guest-Token`을 보내도록 확인했다.
- [ ] 답안이 한 건도 없는 오래된 guest participant/cycle의 보존 기간과 정리 작업을 정했다.
- [ ] rate limit이 준비되지 않은 환경에서는 모바일 앱의 말문제 진입 기능을 공개하지 않는다.
