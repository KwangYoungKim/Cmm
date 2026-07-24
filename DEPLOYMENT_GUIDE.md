# 🚀 신규 서버 이관 및 환경 구축 설치 가이드 (Server Migration & Deployment Guide)

> 본 문서는 **교회 AI 얼굴인식 무인 출석 관리 시스템 (Cmm)**을 새로운 컴퓨터, 교회 전용 서버 또는 클라우드(AWS/GCP/NCP) 환경으로 이관하고 신규 설치/구동하기 위한 상세 절차서입니다.

---

## 📋 1. 사전 필수 요구 사항 (Prerequisites)

이관할 서버 장비에 다음 소프트웨어가 먼저 설치되어 있어야 합니다:

1. **Java Development Kit (JDK) 17 이상**
   - 추천: Eclipse Temurin OpenJDK 17 LTS
   - 설치 확인: `java -version`
2. **PostgreSQL Database Server (14 버전 이상)**
   - 데이터베이스명: `cmm_db`
   - 기본 계정/비밀번호: `postgres` / `postgres`
3. **웹캠 (USB Webcam 또는 통합 인공지능 센서 카메라)**
   - 무인 출석 및 얼굴 등록 PC에 카메라가 연결되어 있어야 합니다.
4. **Git (선택 사항)**
   - 소스코드 클론 및 자동 업데이트용.

---

## 🗄️ 2. PostgreSQL 데이터베이스 생성

새로운 서버에 설치된 PostgreSQL에 접속하여 아래 SQL 명령어로 전용 데이터베이스를 생성합니다:

```sql
-- 1. PostgreSQL 접속 (psql 또는 pgAdmin 사용)
CREATE DATABASE cmm_db;

-- 2. 사용자 및 권한 설정 (필요 시)
-- CREATE USER postgres WITH PASSWORD 'postgres';
-- GRANT ALL PRIVILEGES ON DATABASE cmm_db TO postgres;
```

> **참고**: 데이터베이스 연결 정보는 소스코드 내 `src/main/resources/application.properties` 파일에서 언제든지 변경할 수 있습니다.

---

## 📥 3. 프로젝트 소스코드 복사 및 설정

### 3.1. 소스코드 다운로드 (Git Clone)
새 서버의 원하는 디렉토리(예: `C:\Cmm` 또는 `/var/www/Cmm`)에서 실행합니다:

```bash
git clone https://github.com/KwangYoungKim/Cmm.git
cd Cmm
```

### 3.2. 환경 설정 파일 검토 (`src/main/resources/application.properties`)
필요 시 포트 번호나 DB 비밀번호를 수정합니다:

```properties
# 서버 접속 포트 (기본: 8081)
server.port=8081

# PostgreSQL 데이터베이스 연결 설정
spring.datasource.url=jdbc:postgresql://localhost:5432/cmm_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Hibernate 설정 (최초 실행 시 테이블 자동 생성)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# 대용량 프로필 사진 인코딩 수신 설정 (50MB)
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

---

## 🔨 4. 프로젝트 빌드 및 실행 (Build & Run)

### 4.1. 윈도우(Windows) 환경 빌드 및 실행

#### [방법 A] Gradle 실행 (개발 및 테스트 시)
```cmd
.\gradlew.bat bootRun
```

#### [방법 B] 실행 가능한 JAR 파일 생성 및 실행 (운영 서버 권장)
```cmd
# 1. JAR 파일 빌드
.\gradlew.bat build

# 2. 생성된 실행 파일 구동 (build/libs 디렉토리 위치)
java -jar build\libs\Cmm-0.0.1-SNAPSHOT.jar
```

---

### 4.2. 리눅스(Linux / Ubuntu / RHEL) 환경 빌드 및 실행

```bash
# 1. 실행 권한 부여
chmod +x gradlew

# 2. JAR 파일 빌드
./gradlew build

# 3. 백그라운드 프로세스로 실행 (서버 종료 후에도 계속 구동)
nohup java -jar build/libs/Cmm-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

---

## 🌐 5. 브라우저 접속 및 초기 권한 설정

서버가 구동되면 웹 브라우저(Chrome / Edge 최신 버전)를 열고 접속합니다:

1. **무인 출석 센서 화면**: [http://localhost:8081/attendance](http://localhost:8081/attendance)
2. **교인 명단 & 정보 관리**: [http://localhost:8081/members](http://localhost:8081/members)
3. **교인 얼굴 3포즈 등록**: [http://localhost:8081/register](http://localhost:8081/register)
4. **출석 통계 대시보드**: [http://localhost:8081/stats](http://localhost:8081/stats)

> ⚠️ **중요 (카메라 접근 보안 규칙)**:
> 브라우저 보안 정책에 의해 카메라(Webcam)는 `http://localhost:8081` 또는 `https://` 보안 프로토콜에서만 동작합니다.
> 타 PC에서 네트워크 IP(예: `http://192.168.0.10:8081`)로 접속하여 카메라를 사용하려면 HTTPS SSL서버 설정을 적용하거나 Chrome 정책(`chrome://flags/#unsafely-treat-insecure-origin-as-secure`)에 해당 IP를 등록해야 합니다.

---

## 🛠️ 6. 데이터 백업 및 복구 가이드 (Backup & Restore)

### 6.1. DB 백업 (Backup)
```bash
pg_dump -U postgres -d cmm_db > cmm_backup_2026.sql
```

### 6.2. DB 복구 (Restore)
```bash
psql -U postgres -d cmm_db < cmm_backup_2026.sql
```

---

## ❓ 7. 자주 발생하는 문제 및 조치 (Troubleshooting)

| 현상 | 원인 | 해결 조치 방법 |
| :--- | :--- | :--- |
| `카메라가 이미 사용 중입니다` 오류 | Zoom, Teams 등 타 프로그램이 웹캠 선점 | 타 화상 회의 앱을 끄고 브라우저 새로고침(F5) |
| `Connection refused` DB 오류 | PostgreSQL 서비스 미구동 또는 비밀번호 불일치 | PostgreSQL 서비스 시작 및 `application.properties` 설정 확인 |
| `Webcam Permission Denied` | 브라우저 주소창 🔒 자물쇠 아이콘 권한 차단 | 주소창 🔒 클릭 ➔ 카메라 권한을 **'허용'** 변경 후 F5 |
