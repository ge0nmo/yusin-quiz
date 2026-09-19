# 퀴즈 전자책 만들기 — macOS / Windows 사용 안내

이 기능은 퀴즈앱에 저장된 **텍스트 문제·정답·해설로 실제 EPUB 파일을 만드는 기능**입니다. 생성된 파일은 인터넷 연결 없이 전자책 리더로 읽을 수 있습니다.

현재 지원: EPUB. PDF 출력은 다음 단계이며, 데이터와 책 구성 로직은 PDF에도 재사용할 수 있도록 분리했습니다.

## 1. 나에게 필요한 실행 방법 고르기

| 상황 | 필요한 작업 |
| --- | --- |
| 담당자가 새 기능을 서버에 배포했고 관리자 주소가 있다 | 아래 2번부터 사용하세요. Java·Node.js·DB 설치가 필요 없습니다. |
| 백엔드는 이미 실행 중이고 관리자 화면만 내 컴퓨터에서 실행하고 싶다 | 3번의 Node.js 설치와 4번 또는 5번의 ‘관리자 화면 실행’만 진행하세요. |
| 백엔드와 관리자 화면을 모두 내 컴퓨터에서 실행하고 싶다 | 3번 준비 후, Mac은 4번 / Windows는 5번을 진행하세요. |
| 전자책 파일을 받았고 읽기만 하고 싶다 | 7번으로 이동하세요. 퀴즈앱이나 서버를 실행할 필요 없습니다. |

**관리자 화면은 전자책 리더가 아닙니다.** 파일을 만드는 화면입니다. 내려받은 `.epub`은 별도의 리더에서 엽니다.

## 2. 관리자 화면에서 책 만들기

1. 관리자 사이트에 접속하고 관리자 계정으로 로그인합니다.
2. 메뉴에서 **전자책 만들기**를 선택합니다. 직접 주소는 관리자 사이트의 `/ebooks`입니다.
3. **시험 종목**에서 감정평가사·관세사 등 한 종목을 선택합니다.
4. **책 제목**을 입력합니다. 기본 제목을 그대로 써도 됩니다.
5. 수록할 **과목**과 **연도**에 체크합니다. 처음에는 해당 종목의 가능한 항목이 모두 선택됩니다.
6. **연도 순서**를 선택합니다. 기본은 최신 연도부터입니다.
7. **정답 위치**와 **해설 위치**를 선택합니다.
8. 아래 문항 수를 확인하고 **EPUB 생성·다운로드**를 누릅니다.
9. 생성 중에는 화면을 유지합니다. 완료되면 브라우저의 다운로드 폴더에서 `.epub` 파일을 확인합니다.
10. 자동 저장이 시작되지 않았다면 **파일 다시 다운로드**를 누릅니다.

기본 배치는 다음과 같습니다.

```text
2025년 문제
  1번 문제 → 보기 → 정답
  2번 문제 → 보기 → 정답

2025년 정답·해설
  1번 전체 해설 + 보기별 해설
  2번 전체 해설 + 보기별 해설

2024년 문제
  ...
```

- 정답·해설 위치는 각각 **각 문제 아래 / 해당 연도 끝 / 책 맨 뒤** 중 선택할 수 있습니다.
- ‘정답·해설’ 장에는 그 위치로 설정한 항목만 모입니다. 기본값에서는 연도 끝 장에 해설만 들어갑니다.
- 해설이 없는 문제는 정답만 수록됩니다. 빈 해설이나 새 해설을 만들지 않습니다.
- 복수 정답은 모두 표시됩니다.
- 연도 안에서는 원래 문제 번호순입니다. 같은 번호가 여러 과목에 있으면 시험명·과목명으로 구분합니다.
- 본문의 정답·해설 이동 링크와 **문제로 돌아가기**는 제거했습니다. 연도별 이동은 도서 앱의 목차를 사용합니다.
- 설정은 **새로 생성하는 파일**에 적용됩니다. 이미 배포한 파일은 바뀌지 않으므로 새 파일을 다시 전달해야 합니다.
- **목록 새로고침**을 누르면 범위 선택이 첫 종목의 전체 항목으로 돌아갑니다. 생성 전에 다시 확인하세요.
- 제목 변경과 원문 수정은 다릅니다. 오타나 오래된 내용은 기존 문제 관리 화면에서 담당자가 수정한 뒤 다시 만드세요.

### 어떤 문제가 목록에 나오나요?

시험 종목, 과목, 과목 연결, 시험 회차, 문제의 상태가 **모두 공개(PUBLISHED)**여야 합니다. 비공개 초안은 수록하지 않습니다. 목록에 없는 종목은 수록 가능한 공개 문제가 없는 상태일 수 있습니다.

### 제한과 저장 방식

- 한 번에 최대 **5,000문항**, 원문 JSON 합계 **20MB**입니다. 넘으면 과목이나 연도를 나눠 생성합니다.
- 서버 한 개에서 동시에 한 권만 생성합니다. 다른 작업이 진행 중이면 잠시 후 다시 시도합니다.
- 브라우저는 최대 2분 기다립니다. 제한을 넘으면 범위를 줄여 다시 시도합니다. 브라우저 대기가 끝나도 서버 작업은 잠시 더 진행될 수 있습니다.
- 이미지가 실제 데이터에 있으면 해당 문제 ID를 알려주고 생성을 중단합니다. 이미지를 몰래 빼고 책을 만들지 않습니다.
- 서버에 생성 이력을 영구 보관하지 않습니다. 화면을 닫거나 새로고침하면 다시 다운로드하려면 재생성이 필요합니다. 필요한 파일은 직접 보관하세요.
- 파일명 예: `appraiser-2024-2025.epub`. 책의 내부 제목은 화면에 입력한 제목입니다. 같은 이름의 파일을 여러 번 받으면 브라우저가 번호를 붙일 수 있습니다.

## 3. 내 컴퓨터에서 실행하기 전 준비

로컬 실행은 **백엔드(Java 프로그램) + 관리자 화면(Node.js 프로그램) + MySQL 데이터베이스**가 필요합니다. 코드는 다음과 같이 두 폴더가 나란히 있어야 안내대로 이동할 수 있습니다.

```text
quiz/
  yusin-quiz/       ← 데이터 조회와 EPUB 생성
  quiz-admin/       ← 관리자 웹 화면
```

준비물:

1. **JDK 21**: [Adoptium 다운로드](https://adoptium.net/temurin/releases/?version=21)에서 OS에 맞는 21 버전을 설치합니다. Mac Apple Silicon은 aarch64, Intel은 x64를 선택합니다.
2. **Node.js 22**: [Node.js 다운로드](https://nodejs.org/en/download)에서 22 버전 설치 파일을 사용합니다. npm도 함께 설치됩니다.
3. **MySQL**: 로컬 또는 접근 가능한 개발용 DB. 이미 퀴즈 데이터가 있는 개발용 DB를 쓰거나 담당자에게 현재 앱과 호환되는 DB 복사본을 요청하세요.
4. 관리자 로그인 정보. 새 DB에 관리자가 없다면 아래 환경 변수로 최초 관리자를 만들 수 있습니다.

**프로그램 설치만으로 기존 퀴즈가 자동 복사되지는 않습니다.** 빈 DB에는 문제도 없습니다. 로컬 서버를 켰는데 목록이 비어 있다면 데이터 연결부터 확인하세요.

기존 백엔드 설정이 준비되어 있다면 평소 실행 방식을 그대로 사용해도 됩니다. 아래는 새로 추가한 **`ebook-local` 프로필**을 사용하는 방법입니다. 이 프로필은 내 컴퓨터의 `127.0.0.1`에만 서버를 열고, DB와 JWT 비밀 값을 환경 변수로 받습니다. 이미지 업로드용 S3는 로컬 더미 설정이며 **이미지 업로드는 지원하지 않습니다**. 텍스트 전자책 생성에는 S3가 필요 없습니다.

### DB 정보 확인

담당자에게 다음 정보를 받으세요.

| 이름 | 예시 | 의미 |
| --- | --- | --- |
| DB 주소 | `jdbc:mysql://localhost:3306/quiz_ebook?serverTimezone=Asia/Seoul&characterEncoding=UTF-8` | DB 위치와 이름 |
| DB 사용자 | `ebook_local` | MySQL 계정 |
| DB 비밀번호 | 담당자가 설정한 값 | 관리자 웹 로그인 비밀번호와 다를 수 있음 |

기본값은 기존 스키마를 **검증(validate)**하며 테이블을 새로 만들지 않습니다. 처음부터 빈 전용 DB를 만드는 경우에만 뒤에서 설명하는 `EBOOK_DDL_AUTO=update`를 지정하세요. 운영 DB 설정을 바꿀 필요는 없습니다.

MySQL을 처음 준비하는 담당자는 [MySQL 공식 설치 안내](https://dev.mysql.com/doc/refman/8.4/en/installing.html)를 참고하세요. DB 생성·복원은 사용 중인 운영 데이터와 구분된 개발용 DB에서 진행하세요.

### 선택: 빈 로컬 DB를 처음 만드는 경우

MySQL 설치가 끝났다면 MySQL Workbench에서 로컬 서버에 관리자 계정으로 접속하고 새 SQL 탭에서 다음을 실행할 수 있습니다. `여기에_직접_정한_DB비밀번호` 부분은 실제 사용할 비밀번호로 바꾸세요. 이미 있는 DB를 지우는 명령은 아닙니다.

```sql
CREATE DATABASE IF NOT EXISTS quiz_ebook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'ebook_local'@'localhost' IDENTIFIED BY '여기에_직접_정한_DB비밀번호';
GRANT ALL PRIVILEGES ON quiz_ebook.* TO 'ebook_local'@'localhost';
```

이미 `ebook_local` 계정이 있었다면 `IF NOT EXISTS`는 그 계정의 비밀번호를 바꾸지 않습니다. 기존 비밀번호를 사용하세요. 이 계정은 위 전용 DB 범위에 권한을 갖습니다. 터미널 사용에 익숙하다면 `mysql -u root -p`로 접속한 뒤 같은 SQL을 실행할 수도 있습니다.

이후 `EBOOK_DDL_AUTO=update`로 첫 실행하면 빈 테이블이 생깁니다. 관리자에서 시험 종목·과목·회차·문제를 등록하거나, 담당자가 제공한 현재 버전 DB 복사본을 별도 절차로 복원해야 실제 책을 만들 수 있습니다.

## 4. macOS에서 실행하기

### 4-1. 설치 확인

‘터미널’을 열어 아래 명령을 한 줄씩 실행합니다.

```bash
java -version
node -v
npm -v
```

Java는 21, Node는 22 계열을 권장합니다. Java 버전이 다르면 다음을 실행합니다.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

### 4-2. 백엔드 실행 — 첫 번째 터미널

아래 경로는 예시입니다. 실제 `yusin-quiz` 폴더 경로로 바꾸세요.

```bash
cd ~/dev/quiz/yusin-quiz
export EBOOK_DB_URL='jdbc:mysql://localhost:3306/quiz_ebook?serverTimezone=Asia/Seoul&characterEncoding=UTF-8'
export EBOOK_DB_USERNAME='ebook_local'
read -s 'EBOOK_DB_PASSWORD?DB 비밀번호를 입력하세요: '
export EBOOK_DB_PASSWORD
export EBOOK_JWT_SECRET="$(node -e "process.stdout.write(require('node:crypto').randomBytes(32).toString('hex'))")"
```

비밀번호 입력 중 글자가 보이지 않는 것은 정상입니다. 입력 후 Enter를 누르세요. 위 `read` 명령은 macOS 기본 셸인 zsh 기준입니다.

**DB에 관리자가 없는 경우에만** 다음도 실행합니다. 이미 관리자가 있다면 기존 계정으로 로그인하며, 이 변수로 기존 비밀번호가 변경되지는 않습니다.

```bash
export ADMIN_BOOTSTRAP_LOGIN_ID='ebook-admin'
read -s 'ADMIN_BOOTSTRAP_PASSWORD?새 관리자 비밀번호를 입력하세요: '
export ADMIN_BOOTSTRAP_PASSWORD
```

전용 빈 DB에서 테이블을 최초 생성하는 경우에만 다음을 추가합니다. 이후에는 `unset EBOOK_DDL_AUTO`로 기본 검증 모드로 되돌릴 수 있습니다.

```bash
export EBOOK_DDL_AUTO=update
```

서버를 시작합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=ebook-local'
```

최초 실행은 라이브러리 다운로드 때문에 오래 걸릴 수 있습니다. `Started YusinQuizApplication` 로그가 나오면 준비되었습니다. 이 터미널은 켜둡니다. 기본 서버 주소는 `http://localhost:8080`입니다.

### 4-3. 관리자 화면 실행 — 두 번째 터미널

새 터미널 창을 열고 실행합니다.

```bash
cd ~/dev/quiz/quiz-admin
npm ci
export SOURCE_API_URL='http://localhost:8080'
npm run dev -- --webpack --hostname 127.0.0.1
```

이 안내에서는 로컬 환경 차이를 줄이기 위해 Next.js의 webpack 실행 옵션을 사용합니다. 기존 `npm run dev`도 프로젝트 기본 실행 명령입니다.

브라우저에서 **`http://localhost:3000`**을 열고 로그인합니다. 이후에는 2번의 순서대로 책을 만듭니다. 다른 PC/개발 서버의 백엔드를 연결할 때는 `SOURCE_API_URL`에 담당자가 알려준 주소를 넣으세요. 주소 끝에 `/api`는 붙이지 않습니다.

`npm ci`는 최초 설치 또는 의존성 파일이 바뀐 뒤에 실행하면 됩니다. 매번 필요하지 않습니다.

## 5. Windows에서 실행하기

Windows PowerShell 기준입니다. 명령 프롬프트(cmd)와 문법이 다릅니다.

### 5-1. 설치 확인

PowerShell을 새로 열고 실행합니다.

```powershell
java -version
node -v
npm.cmd -v
```

Java 21이 아니라면 JDK 21 설치 폴더를 확인하고 다음 예시의 경로를 실제 경로로 바꾸세요.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### 5-2. 백엔드 실행 — 첫 번째 PowerShell 창

```powershell
cd C:\dev\quiz\yusin-quiz
$env:EBOOK_DB_URL = 'jdbc:mysql://localhost:3306/quiz_ebook?serverTimezone=Asia/Seoul&characterEncoding=UTF-8'
$env:EBOOK_DB_USERNAME = 'ebook_local'
$env:EBOOK_DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'DB 비밀번호' -AsSecureString)).Password
$env:EBOOK_JWT_SECRET = node -e "process.stdout.write(require('node:crypto').randomBytes(32).toString('hex'))"
```

DB에 관리자가 없다면 다음도 실행합니다.

```powershell
$env:ADMIN_BOOTSTRAP_LOGIN_ID = 'ebook-admin'
$env:ADMIN_BOOTSTRAP_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host '새 관리자 비밀번호' -AsSecureString)).Password
```

빈 전용 DB에서 테이블을 처음 만드는 경우에만 다음을 추가합니다. 이후에는 `Remove-Item Env:EBOOK_DDL_AUTO`로 기본 검증 모드로 되돌립니다.

```powershell
$env:EBOOK_DDL_AUTO = 'update'
```

서버를 시작합니다.

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=ebook-local"
```

`Started YusinQuizApplication` 로그가 나올 때까지 기다리고 이 창은 켜둡니다.

### 5-3. 관리자 화면 실행 — 두 번째 PowerShell 창

```powershell
cd C:\dev\quiz\quiz-admin
npm.cmd ci
$env:SOURCE_API_URL = 'http://localhost:8080'
npm.cmd run dev -- --webpack --hostname 127.0.0.1
```

브라우저에서 **`http://localhost:3000`**을 열고 로그인합니다. `npm.cmd`를 사용하면 PowerShell에서 `npm.ps1` 실행 정책에 걸리는 문제를 피할 수 있습니다.

Windows 명령은 문서에 제공하지만 이 개발 작업의 실제 검증 환경은 macOS입니다. Windows의 JDK 설치 경로·방화벽·DB 연결은 해당 PC에서 확인해야 합니다.

## 6. 다음 날 다시 실행하거나 종료하기

- 서버를 종료할 때는 각 터미널/PowerShell 창에서 **Ctrl+C**를 누릅니다.
- 창을 닫으면 위에서 설정한 환경 변수도 사라집니다. 다음 실행 때 같은 절차로 다시 입력하세요.
- 새 JWT 비밀 값을 만들면 이전 로그인은 만료되므로 다시 로그인하면 됩니다. 기존 관리자 계정과 문제는 DB에 그대로 남습니다.
- 백엔드 서버와 관리자 서버가 켜져 있는 동안만 생성 화면을 사용할 수 있습니다.
- **이미 받은 EPUB을 읽을 때는 서버를 켤 필요가 없습니다.**

## 7. 생성한 EPUB 읽기

### Mac — 도서(Books)

1. Finder에서 다운로드 폴더를 엽니다.
2. `.epub` 파일을 도서 앱으로 열거나, 도서 앱에서 **파일 → 가져오기**로 선택합니다.
3. 목차에서 연도를 선택합니다.
4. 글자 크기를 바꿔 보고 문제·보기·해설이 편하게 읽히는지 확인합니다.

Apple의 [도서 가져오기 안내](https://support.apple.com/en-ae/guide/books/ibkseed72068/mac)를 참고할 수 있습니다. 지원 범위와 메뉴 표현은 OS 버전에 따라 다를 수 있습니다.

### Windows — calibre

1. [calibre 공식 다운로드](https://calibre-ebook.com/download)에서 Windows용 설치 파일을 받습니다.
2. 설치 후 calibre를 열고 **책 추가(Add books)**로 `.epub` 파일을 선택합니다.
3. 추가한 책을 선택해 **보기(View)**로 엽니다.
4. 목차 이동·글자 크기 변경·정답/해설 배치를 확인합니다.

calibre의 [전자책 뷰어 안내](https://manual.calibre-ebook.com/viewer.html)도 참고하세요. Mac에서도 calibre를 사용할 수 있습니다.

교보문고·방송통신대 전자책은 가독성 참고 방향입니다. 그 서비스의 서재 등록·DRM·유통 연동을 구현한 것은 아니며, 해당 전용 앱에서 외부 EPUB을 열 수 있다고 보장하지 않습니다.

## 8. 배포 전 사람이 확인할 항목

- 선택한 종목·과목·연도가 맞는지
- 같은 번호가 다른 과목에 있어도 출처를 구분할 수 있는지
- 원문과 보기, 정답이 누락되지 않았는지
- 복수 정답과 전체·보기별 해설이 맞는지
- 글자 크기를 크게 해도 본문이 잘리거나 겹치지 않는지
- 목차에서 각 연도의 문제와 정답·해설로 이동하는지
- 법령·제도 변경 및 오타를 담당자가 검토했는지

샘플 EPUB은 아래 개발자 검증을 실행하면 `yusin-quiz/build/ebook-samples/sample-review.epub`에 만들어집니다. **샘플에는 가상 문항만 있으며 실제 기출문제가 아닙니다.**

## 9. 문제가 생겼을 때

| 증상 | 확인 방법 |
| --- | --- |
| 관리자 화면이 열리지 않음 | 관리자 터미널의 실행 주소와 포트를 확인합니다. 3000이 사용 중이면 다른 포트가 안내될 수 있습니다. |
| 로그인/목록 조회에서 502 | 백엔드 실행 여부와 `SOURCE_API_URL`을 확인하고 관리자 서버를 재시작합니다. |
| 로그인 실패 | MySQL 계정이 아니라 관리자 계정으로 로그인합니다. 최초 관리자 변수는 기존 계정을 변경하지 않습니다. |
| `EBOOK_DB_USERNAME` 또는 JWT 설정 오류 | 백엔드를 실행하는 바로 그 창에 환경 변수를 설정했는지 확인합니다. |
| DB `Access denied` / 연결 실패 | DB 주소, 사용자, 비밀번호, MySQL 실행 상태를 확인합니다. |
| 테이블 없음 / schema validation 오류 | 현재 앱 스키마와 맞는 DB를 연결했는지 확인합니다. 빈 전용 DB라면 최초 1회 `EBOOK_DDL_AUTO=update`를 사용합니다. |
| 공개 문제가 없음 | 종목·과목·연결·회차·문제 모두 공개인지, 실제 데이터가 연결된 DB에 있는지 확인합니다. |
| 선택한 항목에 문제가 없음 | 현재 과목과 연도 조합을 확인해 빈 항목을 해제하거나 목록을 새로고침합니다. |
| 이미지/지원하지 않는 블록 | 오류에 나온 문제 ID를 문제 관리에서 확인합니다. 이 버전은 텍스트 콘텐츠를 지원합니다. |
| 정답/보기 오류 | 원본 문제에서 보기가 2~5개이고 번호가 연속이며 정답이 하나 이상인지 확인합니다. |
| 생성 중 / 429 | 다른 생성이 끝날 때까지 기다린 후 다시 시도합니다. |
| 2분 초과 / 5,000문항·20MB 초과 | 과목이나 연도를 나눕니다. 서버가 멈춘 경우 서버 로그를 담당자에게 전달합니다. |
| 파일이 자동으로 저장되지 않음 | 완료 화면의 ‘파일 다시 다운로드’를 클릭합니다. |
| Mac에서 `gradlew` 실행 권한 오류 | 백엔드 폴더에서 `chmod +x gradlew` 후 다시 실행합니다. |
| Windows에서 npm 실행 정책 오류 | `npm` 대신 안내대로 `npm.cmd`를 사용합니다. |
| Turbopack 오류 | `npm run dev -- --webpack` 또는 빌드 시 `npm run build -- --webpack`을 사용합니다. |

## 10. 개발자용 검증 명령

기본 사용에는 필요 없습니다. 구현을 수정한 뒤 검증하거나, 최종 파일의 EPUB 형식을 검사할 때 사용합니다.

**macOS — 백엔드 폴더**

```bash
./gradlew test asciidoctor openapi3
./gradlew validateEpub -PepubFile='/절대/경로/book.epub'
```

**Windows PowerShell — 백엔드 폴더**

```powershell
.\gradlew.bat test asciidoctor openapi3
.\gradlew.bat validateEpub "-PepubFile=C:\Users\사용자\Downloads\book.epub"
```

EPUBCheck는 테스트/별도 검증 명령에서 실행합니다. 다운로드 요청마다 검증기를 실행하는 구조는 아닙니다. 규격 검사가 통과해도 정답·법령·오타 검수는 사람이 해야 합니다.

현재 저장소의 기존 통합 테스트는 로컬에서 별도 `src/test/resources/application-test.properties`를 사용합니다. 공유받은 코드에 없다면 담당자에게 테스트용 설정을 요청하세요. 운영 DB 설정으로 테스트를 실행하지 마세요.

**관리자 폴더**

```bash
npm run lint
npm run test
npm run build -- --webpack
```

Windows에서는 `npm`을 `npm.cmd`로 바꾸면 됩니다. 배포할 때는 기존 standalone 배포 방식을 유지하며, 운영에서는 HTTPS와 기존 관리자 설정을 사용합니다.


## 새 디자인 확인과 Mac 도서 앱 조작 (2026-09-19)

전자책은 **책 파일**, 상단 메뉴와 페이지 넘김은 **읽기 앱의 기능**입니다. 별도 앱을 설치하지 않고 Mac 기본 도서 앱에서 확인할 수 있습니다. 교보 eBook을 참고하여 책의 표지와 편집을 개선했으며, 교보 앱 자체를 복제하거나 연동한 것은 아닙니다.

### 새 디자인이 보이지 않을 때

1. 수정된 백엔드를 다시 실행하거나 서버에 반영합니다. 이미 실행 중인 이전 버전 서버는 새 디자인을 만들지 않습니다.
2. 관리자 **전자책 만들기**에서 다시 생성합니다. 구분하기 쉽도록 검수할 때는 제목에 `디자인 확인용`처럼 표시해도 됩니다.
3. 방금 내려받은 `.epub`를 더블 클릭합니다. 도서 앱에 이미 들어 있던 책은 자동으로 바뀌지 않습니다.
4. 새 책의 표지(크림색 바탕·짙은 녹색 글자), 배경 박스 없는 고딕 본문, 은은한 문제 번호 색상, 굵은 정답·해설 제목을 확인합니다. 본문의 `문제로 돌아가기`, `정답 확인`, 반복 이동 링크가 없어야 합니다.

### 왼쪽·오른쪽 페이지를 한 번에 보기

- 책을 열고 **독서 창의 옆면을 드래그해 가로로 넓힙니다.** 도서 앱이 공간에 맞춰 한 페이지 또는 두 페이지로 표시합니다.
- 최신 macOS에서 한 페이지만 보이면 상단의 **화면 모드 → 사용자화 → 다중 열 허용**도 확인합니다. 메뉴 이름은 macOS 버전에 따라 다릅니다.
- EPUB 안에서 본문을 두 열로 고정하지 않았습니다. 작은 창과 큰 글씨에서는 한 페이지로 바뀔 수 있습니다.

### 상단바 표시와 글자 크기

- **책 화면의 맨 위로 마우스 포인터를 옮기면** 도서 앱의 도구 막대가 나타납니다. 읽는 동안 자동으로 숨겨질 수 있습니다.
- **화면 모드(가/글자 모양 아이콘)**에서 작은/큰 `가`를 눌러 크기를 바꿉니다.
- 이번 본문 디자인은 **라이트 모드 기준**입니다. 본문 뒤에 별도 색상 면을 칠하지 않고 리더의 종이색을 사용합니다. 읽기 앱에서 어두운 테마를 강제하면 보이는 색이 달라질 수 있으므로 라이트 테마에서 확인해 주세요. EPUB이 외부 앱의 테마 선택까지 제한하지는 못합니다.
- 글자가 지나치게 굵으면 최신 버전의 **사용자화 → 볼드 텍스트** 설정을 확인합니다. 원문 자체의 강조는 유지됩니다.

### 페이지와 연도 이동

- 책의 왼쪽·오른쪽 가장자리에 포인터를 올려 나타나는 화살표로 페이지를 넘깁니다.
- 상단 **목차**에서 원하는 연도 또는 정답·해설 장을 선택합니다.
- 상단 **검색**에 페이지 번호를 입력해 이동할 수 있습니다. 글자 크기나 창 너비를 바꾸면 전체 페이지 수와 문제의 페이지 위치도 바뀝니다.
- 상단바를 항상 고정하거나 버튼 위치를 바꾸는 것은 EPUB 파일로 제어할 수 없습니다. 도서 앱의 기본 동작을 사용합니다.

공식 안내: [Apple — 책의 모양새 변경](https://support.apple.com/ko-kr/guide/books/ibks8923126d/mac), [Apple — 책 읽기·이동](https://support.apple.com/ko-kr/guide/books/ibks5f526382/mac), [교보 eBook 이용 안내](https://ebook.kyobobook.co.kr/dig/etc/ebookgdnc).

### 본문 디자인과 기기별 읽기

- 고딕 본문과 작은 출처를 사용하고, 문제 번호에만 은은한 색을 둡니다. 본문 배경 박스와 색상 띠는 사용하지 않습니다.
- 정답과 해설은 굵은 글씨와 짧은 간격으로 구분합니다. 보기별 해설만 있으면 중복된 `해설` 제목 없이 `N번 보기 해설`로 바로 시작합니다. 문항 간 여백은 본문 글자 크기의 2배(`2em`)를 한 번만 적용합니다.
- 글씨를 확대하거나 화면 방향을 바꾸면 본문이 다시 흐릅니다. 넓은 화면에서는 한 줄이 지나치게 길어지지 않도록 본문 폭에 상한을 둡니다.
- 원문과 정답·해설의 수록 위치 설정은 유지됩니다. 원래 저장된 강조와 줄바꿈도 보존합니다.
- 이번 변경은 EPUB 생성 결과에 적용됩니다. 관리자 화면이나 별도 읽기 앱을 새로 만드는 기능은 아닙니다.

개발 검수 샘플은 `yusin-quiz/build/ebook-samples/sample-review.epub`(연도 끝 해설)과 `sample-reading-inline.epub`(문제 아래 해설)에 생성됩니다. 실제 기출 데이터가 아닌 가상 문항입니다. 본문 XHTML의 라이트 화면·가로/세로·큰 글씨 검수는 실제 Mac 도서 앱이나 Windows·모바일 리더의 최종 검수를 대신하지 않습니다.

문항 사이 큰 공백을 줄이기 위해 기존의 중첩 여백을 제거했습니다. 다만 다음 문제의 제목·출처·첫 줄이 페이지 끝에 들어가지 않으면 리더가 다음 페이지로 넘겨 일부 빈 공간이 남을 수 있습니다. 모든 문항을 새 페이지에서 시작하도록 강제하지는 않습니다.
