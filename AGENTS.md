# AGENT.md

## 문서 목적 / 독자

- 이 문서는 `yusin-quiz` 저장소를 수정하는 코딩 에이전트와 엔지니어를 위한 현재 기준 온보딩 문서.
- 목표는 시스템 경계, API 네임스페이스, 변경 시 우선 확인 포인트를 빠르게 파악하게 만드는 것.
- 내용은 현재 리포지토리 코드와 테스트에서 직접 확인한 사실만 포함.
- 기준 스택은 Java 21, Spring Boot 3.3.1, Gradle, Spring Data JPA, MySQL, Spring Security, JWT, Google 소셜 로그인, AWS S3.

## 빠른 사실 요약

- 루트 패키지는 `com.cpa.yusin.quiz`.
- 백엔드는 API 전용 서버. Thymeleaf 관리자 UI, `web/controller`, `/admin/**` HTML 렌더링은 제거된 상태.
- 관리자 프론트는 Next.js를 전제로 하며, 백엔드 관리자 기능은 `/api/admin/**`와 `/api/v2/admin/problem`만 사용.
- 모든 JPA 엔티티는 `BaseEntity`를 상속하고 `createdAt`, `updatedAt` 감사를 공통 사용.
- 문제(`Problem`)는 V1 HTML 저장 모델과 V2 JSON Block 저장 모델을 동시에 보유.
- 관리자 파일 업로드는 `file` 도메인의 `AdminFileController`가 `/api/admin/file`로 제공.
- 관리자 답변 요청 DTO는 `answer.controller.dto.request` 패키지에 위치.
- 시간 의존 로직은 `ClockHolder`, UUID 생성은 `UuidHolder`를 우선 사용.
- CORS 허용 도메인은 `app.security.cors.allowed-origins`로 명시 설정한다.
- `Subject`는 삭제 플래그와 별도로 `status(DRAFT, PUBLISHED)`를 가지며, 사용자 앱은 `PUBLISHED` 과목만 본다.
- `./gradlew test`는 현재 작업 환경에서 성공 확인됨.
- `./gradlew test asciidoctor openapi3`로 REST Docs HTML과 OpenAPI 3 JSON까지 함께 생성할 수 있음.

## 실행 / 검증

### 핵심 명령

- 테스트: `./gradlew test`
- 문서 생성 포함: `./gradlew test asciidoctor openapi3`
- generated docs:
  - `build/docs/asciidoc/*.html`
  - `build/api-spec/openapi3.json`
  - `docs/frontend-api/*.md`

### 실행 전 알아둘 점

- 애플리케이션 기본 설정은 `src/main/resources/application.yml`에 존재.
- Gradle 실행 JDK는 21을 기준으로 맞춰야 함. 로컬 기본 JDK가 더 높으면 `JAVA_HOME`을 Corretto 21 등으로 지정한 뒤 실행해야 함.
- 현재 코드상 주요 외부 의존성 키는 아래와 같음.
  - `spring.datasource.*`
  - `jwt.token.*`
  - `cloud.aws.*`
  - `spring.security.oauth2.client.registration.google.client-id`
- 문서, 로그, 커밋에 `application.yml`의 실제 값 복사 금지.

### Docker / 인프라 파일

- `docker-compose/` 하위 파일은 모니터링, nginx, blue-green 배포 보조 성격이 강함.
- 확인한 파일 기준 Prometheus, Grafana, Node Exporter, nginx, certbot 관련 설정이 존재.
- `Dockerfile`은 빌드된 JAR를 실행하며 런타임 인자를 전달받음.

## 아키텍처 개요

### 패키지 구조

- `answer`, `bookmark`, `choice`, `exam`, `file`, `member`, `problem`, `question`, `study`, `subject`
  - 각 도메인의 HTTP 진입점, 비즈니스 로직, 인프라가 함께 모여 있음.
- `config`
  - 보안, S3, async executor, AOP 설정.
- `global`
  - JWT, 예외 처리, 필터, 사용자 상세 정보, 공통 유틸리티, 로깅.
- `common`
  - 공통 응답 모델, 공통 인프라 추상화, 헬스체크.
- `web` 패키지는 현재 존재하지 않음.

### 레이어 흐름

- 일반적인 흐름은 `Controller -> controller.port -> ServiceImpl -> service.port -> RepositoryImpl -> JpaRepository`.
- DTO 매핑은 도메인별 `*Mapper`가 담당.
- 예외는 도메인별 `*Exception`과 `ExceptionAdvice`로 일관 처리.
- JSON 응답은 대부분 `GlobalResponse<T>`를 사용하지만, 일부 관리자 엔드포인트는 raw list를 그대로 반환.

### 공통 인프라

- `BaseEntity`는 `createdAt`, `updatedAt` 감사를 제공하는 공통 부모.
- `YusinQuizApplication`은 `@EnableJpaAuditing`, `@EnableAsync`를 사용.
- 신규 JPA 엔티티는 반드시 `BaseEntity`를 상속해야 하며, 해당 규칙은 테스트에서 강제됨.
- `AppConfig`는 `taskExecutor`를 등록하고, `StudyEventListener`는 비동기 이벤트 처리에 이를 사용.
- 컨트롤러 응답 타입은 wildcard `ResponseEntity<?>` 대신 구체 타입 또는 plain body를 사용한다.

## HTTP 경계와 API 네임스페이스

### `/api/v1/**`

- 사용자 앱용 REST API.
- 예시 도메인:
  - 과목: `/api/v1/subject`
  - 시험: `/api/v1/exam`
  - 문제 V1: `/api/v1/problem`
  - 질문/답변: `/api/v1/question`, `/api/v1/answer`
  - 학습 세션: `/api/v1/study`
  - 학습 로그: `/api/v1/study-logs`
  - 북마크: `/api/v1/bookmarks`
  - 인증: `/api/v1/auth/*`
- 운영 확인용 헬스 엔드포인트로 `/api/v1/hc`, `/api/v1/env`를 유지.

### `/api/admin/**`

- 관리자용 JWT API.
- `/api/admin/login`만 비인증 허용.
- 그 외 모든 `/api/admin/**`는 `ROLE_ADMIN` 필요.
- 예시:
  - 관리자 로그인: `/api/admin/login`
  - 과목 관리: `/api/admin/subject`
  - 시험 관리: `/api/admin/exam`, `/api/admin/subject/{subjectId}/exam`
  - 문제 V1 삭제: `/api/admin/problem/{problemId}`
  - 질문/답변 관리: `/api/admin/question`, `/api/admin/answer`
  - 파일 업로드: `/api/admin/file`

### `/api/v2/problem` 및 `/api/v2/admin/problem`

- 문제의 Block JSON 모델 전용 API.
- 조회 응답은 `ProblemV2Response`를 사용하고 `content`, `explanation`이 `List<Block>` 형태.
- 관리자 V2 저장은 `POST /api/v2/admin/problem` 단일 엔드포인트에서 생성/수정을 함께 처리.

### 제거된 경계

- `/admin/**` 레거시 관리자 HTML 및 AJAX 엔드포인트는 더 이상 지원하지 않음.
- 서버 렌더링 템플릿, 정적 JS/CSS, Thymeleaf 의존성도 제거된 상태.

## 핵심 도메인 모델

### 1. 시험 콘텐츠 축

- `Subject -> Exam -> Problem -> Choice`
- `Subject`
  - 과목 최상위 분류.
  - 이름 중복 검증 존재.
  - `status` 가 `PUBLISHED` 인 과목만 사용자 앱 진입점으로 노출된다.
- `Exam`
  - 시험 이름, 연도, `subjectId`를 가짐.
  - `Subject`와 JPA 연관관계가 아니라 `subjectId` 스칼라로만 연결.
- `Problem`
  - 시험에 속한 문제.
  - `number`는 시험 내부 문제 번호.
  - V1 HTML과 V2 Block JSON 저장 필드를 동시에 가짐.
- `Choice`
  - 문제의 보기.
  - `(problem_id, number)` 유니크 제약 존재.
  - `isAnswer`가 정답 여부.

### 2. 사용자 Q&A 축

- `Member -> Question -> Answer`
- `Question`
  - 특정 `Problem`에 대해 사용자가 남기는 질문.
  - `answeredByAdmin`, `answerCount`, `isRemoved`를 가짐.
- `Answer`
  - 질문에 대한 답변.
  - 작성자 본인 또는 ADMIN만 수정/삭제 가능.
- 관리자 답변 생성/수정 요청 DTO는 `answer.controller.dto.request` 패키지에 위치.

### 3. 북마크 축

- `Member -> Bookmark -> Problem`
- `Bookmark`
  - `(member_id, problem_id)` 유니크 제약 존재.
  - 북마크 조회 응답은 현재 `ProblemV2Response` 형식을 사용.

### 4. 학습 세션 축

- `Member -> StudySession -> SubmittedAnswer`
- 후처리 누적: `DailyStudyLog`
- `StudySession`
  - 특정 회원이 특정 시험을 푸는 세션.
  - `examId`는 `Exam` 연관관계가 아니라 스칼라 ID.
  - `mode`, `status`, `lastIndex`, `currentScore`, `startedAt`, `finishedAt` 보유.
- `SubmittedAnswer`
  - `(study_session_id, problem_id)` 유니크 제약 존재.
  - 시험 풀이 중 실제 선택한 보기 저장.
- `DailyStudyLog`
  - `(member_id, date)` 유니크 제약 존재.
  - 일별 푼 문제 수를 누적 저장.

### 중요한 구분

- `Choice.isAnswer`
  - 문제의 정답 보기 여부.
- `Answer`
  - 질문 게시판 답변 엔티티.
- `SubmittedAnswer`
  - 시험 풀이 중 제출한 답안.

이 셋은 이름이 비슷하지만 역할이 완전히 다름.

## 중요 모델링 특징

### V1 / V2 문제 모델 공존

- `Problem`은 아래 두 모델을 동시에 가짐.
  - V1: `content`, `explanation` 문자열 HTML
  - V2: `contentJson`, `explanationJson` JSON Block 리스트
- V1 조회 응답은 `ProblemDTO`.
- V2 조회 응답은 `ProblemV2Response`.
- `Problem`은 공통 해설강의 필드 `lectureYoutubeUrl`, `lectureStartSecond`도 함께 가짐.
- `ProblemDTO`, `ProblemV2Response`, 북마크 문제 응답은 모두 nullable `lecture` 객체를 포함할 수 있음.
- 문제 관련 기능 수정 시 어느 API와 어느 저장 필드를 대상으로 하는지 먼저 분리해야 함.

### 소프트 삭제와 하드 삭제가 혼재

- `Subject`, `Exam`, `Problem`, `Question`은 `isRemoved` 기반 삭제 경로가 존재.
- `Subject`는 soft delete 와 별도로 `status(DRAFT, PUBLISHED)` 로 게시 상태를 관리한다.
- `Choice`, `Answer`, `Bookmark`는 서비스/리포지토리에서 실제 삭제 호출이 존재.
- 삭제 로직 변경 시 플래그 삭제인지 실제 삭제인지 먼저 확인해야 함.

### 스칼라 ID 연결

- `Exam.subjectId`
- `StudySession.examId`

위 두 값은 JPA 연관관계가 아니라 단순 ID 저장. 연관 엔티티를 바로 탐색할 수 없으므로 서비스 계층에서 별도 조회가 필요.

### 응답 래핑 규칙

- 사용자 API와 관리자 API 대부분은 `GlobalResponse<T>` 사용.
- 페이징 정보는 `pageInfo`에 담기며 nullable.
- 일부 관리자 조회 엔드포인트는 raw list를 직접 반환.
- 예외는 `ExceptionAdvice`가 `ErrorResponse`로 변환.

### Frontend Integration Contracts

- 프론트 영향 API의 최종 권위는 integration test로 검증된 계약이어야 함.
- 프론트 에이전트는 아래 순서로 읽어야 함.
  - `docs/frontend-api/*.md`
  - `build/docs/asciidoc/*.html`
  - `build/api-spec/openapi3.json`
  - Controller 주석 + DTO
  - integration test
- 프론트 영향 API 변경 시 아래를 같이 수정해야 함.
  - integration test docs
  - `docs/frontend-api/*.md`
  - `AGENT.md`
  - `quiz-admin` 타입 또는 매핑 레이어
- nullable / optional 정책은 AGENT와 프론트 문서에 명시해야 함.

### Block JSON 스키마

- `Block`은 Jackson 다형성 타입.
- 확인한 타입:
  - `text`
  - `image`
  - `list`
  - `listItem`
- V2 문제 저장/조회 로직은 이 구조에 의존.

## 핵심 유즈케이스

### 회원 인증

- 관리자 로그인: `AuthenticationServiceImpl.loginAsAdmin`
  - 일반 로그인 흐름을 재사용한 뒤 `Role.ADMIN` 여부를 검증.
- 구글 로그인: `SocialLoadService -> GoogleTokenVerifier -> AuthenticationServiceImpl.socialLogin`
  - 신규 소셜 회원은 랜덤 닉네임과 랜덤 비밀번호로 자동 등록.
- 토큰 재발급: `refreshAccessToken`

### 문제 저장 / 이미지 처리

- V1 문제 저장은 `ProblemServiceImpl`이 담당.
- 문제 본문과 해설의 Base64 이미지를 파싱해 S3 업로드 후 URL로 치환.
- V2 문제 저장은 `CreateProblemV2ServiceImpl`이 담당.
- V2는 클라이언트가 전달한 `List<Block>`을 그대로 저장하며 이미지 URL도 그대로 저장.
- V2 저장 요청의 `lecture` 규칙은 아래와 같음.
  - `lecture = null` 이면 기존 해설강의 링크 제거
  - `lecture.youtubeUrl`만 있어도 저장 가능
  - `lecture.startTimeSecond`는 nullable
  - 저장 전 `YoutubeLectureUrlProcessor`가 canonical YouTube watch URL로 정규화

### 문제 조회 / Presigned URL 변환

- 이미지 URL은 조회 시점에 `ProblemContentProcessor`가 presigned URL로 치환.
- V1은 HTML `<img src>`를 치환.
- V2는 `ImageBlock.src`를 치환.
- 관리자 파일 업로드는 `AdminFileController`가 raw S3 URL을 저장한 뒤, 프론트 즉시 사용을 위해 presigned URL을 반환.
- 해설강의는 `ProblemLectureResponse`로 응답되며 `lecture.playbackUrl`을 프론트가 그대로 사용해야 함.

### 학습 세션

- 시작: `StudySessionService.startSession`
  - 같은 회원, 같은 시험, 같은 모드의 `IN_PROGRESS` 세션이 있으면 재사용.
  - 없으면 새 세션 생성.
- 답안 저장: `saveAnswer`
  - 세션은 `PESSIMISTIC_WRITE` 잠금 조회.
  - `SubmittedAnswer`는 `(session, problem)` 기준 upsert.
  - PRACTICE 모드면 정답 여부와 해설 반환.
- 종료: `completeSession`
  - 서버 측에서 정답 수를 다시 계산해 점수 산출.

### 학습 로그 적재

- `StudySessionService`는 `StudySolvedEvent`를 발행.
- `StudyEventListener`는 `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`로 후처리.
- `StudyLogService.recordActivity`는 `(member_id, date)` 유니크 키 기반 upsert 로 일별 문제 풀이 수를 누적 저장한다.

### 질문 / 답변

- 질문 생성은 문제 단위.
- 답변 생성/삭제 시 `Question.answerCount` 갱신.
- 관리자 답변 작성 시 `Question.answeredByAdmin` 갱신.

## 보안 / 인증

### 핵심 구성 요소

- `SecurityConfig`
  - `/api/v1/**`용 stateless 체인
  - `/api/admin/**`용 stateless 체인
- `SecurityFilter`
  - JWT 인증 필터
  - `Authorization: Bearer ...` 헤더 또는 `JWT_TOKEN` 쿠키에서 토큰 추출
- `JwtServiceImpl`
  - access token / refresh token 생성 및 파싱
  - `subject`는 회원 이메일
- `MemberDetailsService`
  - 이메일 기준 회원 로드

### 현재 보안 정책 특징

- `/api/v1/**`
  - GET은 대체로 permit-all
  - `/api/v1/bookmarks/**`는 인증 필요
  - `/api/v1/auth/**`는 permit-all
- `/api/admin/**`
  - `/api/admin/login`만 permit-all
  - 나머지는 `ROLE_ADMIN` 필요
- `/api/v2/admin/problem`도 관리자 체인에 포함된다.
- CORS는 `app.security.cors.allowed-origins` 값만 허용하며 `allowCredentials = true`와 wildcard 조합은 사용하지 않는다.
- 폼 로그인 기반 관리자 체인, Thymeleaf 로그인 페이지, `FormAuthenticationProvider`는 현재 구조에 없음.

## 파일 저장 / S3

- `FileServiceImpl`이 파일 업로드와 presigned URL 발급을 모두 담당.
- 업로드 방식 두 가지:
  - `save(MultipartFile)`
  - `saveByteArray(byte[], filename, contentType)`
- 저장 후 DB에는 `File` 엔티티가 남고, 조회용 URL은 presigned URL을 별도로 생성.
- 테스트 프로파일에서는 `S3MockConfig`가 `S3Client`, `S3Presigner`를 mock으로 대체.

## 관측 / 운영 보조

- `TraceIdFilter`
  - `X-Request-ID`를 우선 사용하고 없으면 8자리 trace id 생성.
  - MDC 정리까지 수행.
- `PerformanceLoggingAspect`
  - Controller / Service 호출 시간을 로깅.
  - 보안 관련 민감 메서드는 인자/결과를 숨김 처리.
- Actuator, Prometheus registry 의존성 존재.
- `prometheus/prometheus.yml`, `docker-compose/infra.yml` 등 모니터링 관련 파일 존재.

## 테스트 전략

### 1. 순수 단위 / 서비스 테스트

- `src/test/java/com/cpa/yusin/quiz/config/TestContainer.java`는 스프링 없이 fake repository와 실제 서비스 객체를 직접 묶는 테스트 전용 조립기.
- `src/test/java/com/cpa/yusin/quiz/mock/*`에 fake 구현들이 존재.
- 서비스, 컨트롤러, 리포지토리 일부 테스트는 이 fake 기반 테스트를 사용.
- 다만 동시성, 락, 보안 경계, soft delete 전파, 문서 계약 같은 핵심 회귀는 fake 테스트만으로 충분하지 않다.

### 2. 스프링 통합 테스트

- `@SpringBootTest`, `@AutoConfigureMockMvc`, REST Docs 기반 테스트가 존재.
- 예시: `subject.integration.SubjectTest`, `problem.integration.ProblemTest`, `config.AdminApiSecurityIntegrationTest`.
- `TeardownExtension` + `CleanDatabase`가 각 테스트 전 DB 정리를 수행.
- `src/main/resources/static/asciidoc/*.adoc`와 Gradle Asciidoctor 설정이 존재해 API 문서화 흐름이 연결돼 있음.
- 학습 도메인에는 세션 재개, 답안 단일행 보장, 일별 로그 누적을 검증하는 동시성 통합 테스트가 존재한다.

### 3. 아키텍처 / 회귀 테스트

- 모든 JPA 엔티티의 `BaseEntity` 상속 여부를 별도 테스트가 검증.
- API-only 구조 회귀 테스트가 아래 사실을 고정해야 함.
  - `com.cpa.yusin.quiz.web` 패키지 부재
  - 순수 MVC `@Controller` 부재
  - Thymeleaf 핵심 클래스 부재
  - 제거된 대표 템플릿/정적 리소스 부재

## 변경 시 우선 확인할 포인트

### 문제 관련 변경

- 먼저 V1인지 V2인지 구분할 것.
- 사용자 노출 여부는 `Problem` 자체가 아니라 상위 `Subject.status` 에 의해 추가로 제한된다.
- V1 변경 시 확인 대상:
  - `ProblemServiceImpl`
  - `ProblemController`
  - `AdminProblemController` 삭제 경로
- V2 변경 시 확인 대상:
  - `CreateProblemV2ServiceImpl`
  - `GetProblemV2ServiceImpl`
  - `ProblemV2Controller`
  - `AdminProblemV2Controller`
- 이미지 처리 변경 시 `ProblemContentProcessor`, `FileServiceImpl`, `AdminFileController`까지 함께 볼 것.
- 프론트 영향이 있는 API 변경이면 `docs/frontend-api`, REST Docs, OpenAPI 3 산출물 기준까지 함께 갱신해야 함.
- lecture 계약 변경이면 아래도 함께 확인해야 함.
  - `ProblemLectureResponse`
  - `YoutubeLectureUrlProcessor`
  - `src/test/java/com/cpa/yusin/quiz/problem/integration/ProblemTest.java`
  - `src/test/java/com/cpa/yusin/quiz/bookmark/integration/BookmarkTest.java`

### 인증 / 보안 변경

- 최소 확인 대상:
  - `SecurityConfig`
  - `SecurityFilter`
  - `JwtServiceImpl`
  - `MemberDetailsService`
  - `AuthenticationServiceImpl`
- 경로 permit-all 변경 시 실제 컨트롤러 매핑과 일치하는지 반드시 대조할 것.
- 관리자 기능 변경 시 `/api/admin/**`와 `/api/v2/admin/problem`을 함께 확인해야 함.

### 학습 기능 변경

- 최소 확인 대상:
  - `StudySessionService`
  - `StudySessionJpaRepository`
  - `SubmittedAnswer`
  - `StudyEventListener`
  - `StudyLogService`
- 세션 재개 규칙, 잠금 조회, upsert 규칙, 이벤트 후처리까지 하나의 기능으로 취급해야 함.

### 관리자 기능 변경

- 레거시 `/admin/**` UI를 전제로 작업하면 안 됨.
- 관리자 소비자는 Next.js 프론트와 `/api/admin/**`, `/api/v2/admin/problem` API.
- 관리자 파일 업로드 경로는 `/api/admin/file`.
- V1 문제 생성/수정은 더 이상 `/api/admin/problem`로 제공되지 않음.
- 서버 렌더링 템플릿, 정적 페이지 스크립트, 폼 로그인 흐름을 기준으로 설계하면 안 됨.

## 확인된 주의사항

- 문제 도메인은 V1 HTML 모델과 V2 Block JSON 모델이 동시에 살아 있으므로, 단일 모델만 가정하면 누락이 발생하기 쉬움.
- 공개 인증 경계는 현재 `/api/v1/auth/*`와 `/api/admin/login` 중심이므로, permit-all 변경 시 실제 컨트롤러 매핑과 반드시 대조해야 함.
- `application.yml`에는 실제 값이 들어 있으므로 문서화, 출력, 공유 시 키 이름만 다루고 값은 다루지 말 것.
- 신규 엔티티 추가 시 `BaseEntity` 상속을 누락하면 감사 컬럼, 정렬, 응답 매핑 규칙이 깨질 수 있음.

## 품질 불변식

- 시간 값을 새로 만들 때 서비스 계층에서는 `ClockHolder`를 우선 사용.
- UUID를 새로 만들 때 애플리케이션 코드에서는 `UuidHolder`를 우선 사용.
- `jakarta.transaction.Transactional` 대신 Spring `@Transactional`로 통일한다.
- 이력성 주석(`[수정]`, `[Refactor]`, `[변경]`)은 남기지 않는다.
- 보안 필터 401 응답은 `SecurityErrorResponse` 형식으로 유지한다.
- 외부 HTTP 계약을 바꾸는 변경이면 integration test, REST Docs, OpenAPI 산출물을 같이 갱신한다.

## 코드까지 열어야 할 때 먼저 볼 파일

- 인증 / 보안
  - `src/main/java/com/cpa/yusin/quiz/config/SecurityConfig.java`
  - `src/main/java/com/cpa/yusin/quiz/global/filter/SecurityFilter.java`
  - `src/main/java/com/cpa/yusin/quiz/member/service/AuthenticationServiceImpl.java`
- 문제 / 파일
  - `src/main/java/com/cpa/yusin/quiz/problem/service/ProblemServiceImpl.java`
  - `src/main/java/com/cpa/yusin/quiz/problem/service/CreateProblemV2ServiceImpl.java`
  - `src/main/java/com/cpa/yusin/quiz/problem/service/GetProblemV2ServiceImpl.java`
  - `src/main/java/com/cpa/yusin/quiz/problem/service/ProblemContentProcessor.java`
  - `src/main/java/com/cpa/yusin/quiz/file/service/FileServiceImpl.java`
  - `src/main/java/com/cpa/yusin/quiz/file/controller/AdminFileController.java`
- 학습
  - `src/main/java/com/cpa/yusin/quiz/study/service/StudySessionService.java`
  - `src/main/java/com/cpa/yusin/quiz/study/service/StudyLogService.java`
  - `src/main/java/com/cpa/yusin/quiz/study/event/StudyEventListener.java`
- 공통 규약
  - `src/main/java/com/cpa/yusin/quiz/common/infrastructure/BaseEntity.java`
  - `src/test/java/com/cpa/yusin/quiz/common/infrastructure/BaseEntityArchitectureTest.java`

## 최종 판단 규칙

- 문제 관련 작업이면 V1/V2 여부부터 확정.
- 시험 풀이 관련 작업이면 `Answer`가 아니라 `SubmittedAnswer`인지 먼저 확인.
- 관리자 관련 작업이면 `/api/admin/**` 또는 `/api/v2/admin/problem`인지 먼저 확정.
- 인증 관련 작업이면 경로 permit-all, JWT 필터, 컨트롤러 경로 세 가지를 함께 비교.
- 신규 엔티티를 추가하면 `BaseEntity` 상속을 기본값으로 생각.
- 서버 렌더링 템플릿이나 `/admin/**` 레거시 UI를 가정한 변경은 현재 기준과 맞지 않음.
