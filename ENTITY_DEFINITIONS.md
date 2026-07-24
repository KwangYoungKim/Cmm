# 📐 데이터베이스 엔티티 및 테이블 정의서 (Database Entity Specifications)

> 본 문서는 **교회 AI 얼굴인식 무인 출석 관리 시스템 (Cmm)**의 PostgreSQL 데이터베이스 테이블 구조, 데이터 타입, JPA 엔티티 매핑 명세 및 제약 조건을 정의한 기술 문서입니다.

---

## 📊 1. ERD (Entity Relationship Diagram) 구조

```
+------------------------------------+           +------------------------------------+
|             MEMBERS                |           |             ATTENDANCE             |
+------------------------------------+           +------------------------------------+
| PK  id             BIGINT (AUTO)   |<---------1| PK  id             BIGINT (AUTO)   |
|     name           VARCHAR(255)    |           | FK  member_id      BIGINT          |
|     gender         VARCHAR(10)     |           |     check_in_time  TIMESTAMP       |
|     position       VARCHAR(30)     |           |     attendance_date DATE           |
|     department     VARCHAR(50)     |           +------------------------------------+
|     phone          VARCHAR(20)     |
|     birth_date     VARCHAR(10)     |
|     face_descriptor TEXT           |  (최대 10개 AI 128D 얼굴 포즈 벡터 JSON)
|     profile_image  TEXT            |  (Base64 300x300 섬네일 이미지)
|     created_at     TIMESTAMP       |
+------------------------------------+
```

---

## 🏛️ 2. 테이블 상세 명세 (Table Specifications)

### 2.1. `members` (교인 정보 테이블)

- **설명**: 교인의 인적사항, 프로필 사진 섬네일 및 AI 딥러닝 128차원 얼굴 포즈 특징 벡터를 저장하는 메인 엔티티 테이블입니다.

| 컬럼명 (Column) | 데이터 타입 (Type) | Nullable | 기본값 (Default) | 설명 및 비고 |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `BIGINT` | **NO** | `AUTO_INCREMENT` | 교인 고유 식별자 (Primary Key) |
| `name` | `VARCHAR(255)` | **NO** | - | 교인 성명 |
| `gender` | `VARCHAR(10)` | YES | `NULL` | 성별 (`남`, `여`) |
| `position` | `VARCHAR(30)` | YES | `'교우'` | 직분 (`교우`, `집사`, `권사`, `장로`, `목사`, `전도사`, 수기입력) |
| `department` | `VARCHAR(50)` | YES | `NULL` | 소속 부서 (예: `청년부`, `1교구`, `남선교회` 등) |
| `phone` | `VARCHAR(20)` | YES | `NULL` | 연락처 (DB 하이픈 제거 숫자 전용 저장 e.g. `01073032538`) |
| `birth_date` | `VARCHAR(10)` | YES | `NULL` | 생년월일 (`YYYY-MM-DD` 형식) |
| `face_descriptor` | `TEXT` | YES | `NULL` | AI 128차원 얼굴 포즈 벡터 JSON<br>(사전업로드 교인은 `""` 또는 `NULL`, 등록 시 최대 10개 슬라이딩 윈도우 축적) |
| `profile_image` | `TEXT` | YES | `NULL` | Base64 인코딩 정면 크롭 프로필 섬네일 이미지 |
| `created_at` | `TIMESTAMP` | **NO** | `NOW()` | 최초 등록 일시 |

---

### 2.2. `attendance` (출석 체크 이력 테이블)

- **설명**: 무인 센서 카메라 및 수동 확인을 통해 기록된 일자별/시간대별 출석 로그를 저장하는 테이블입니다.

| 컬럼명 (Column) | 데이터 타입 (Type) | Nullable | 기본값 (Default) | 설명 및 비고 |
| :--- | :--- | :---: | :---: | :--- |
| `id` | `BIGINT` | **NO** | `AUTO_INCREMENT` | 출석 기록 고유 ID (Primary Key) |
| `member_id` | `BIGINT` | **NO** | - | 출석 교인 FK (`members.id` 참조) |
| `check_in_time` | `TIMESTAMP` | **NO** | `NOW()` | 출석 확인 상세 일시 (`YYYY-MM-DD HH:mm:ss`) |
| `attendance_date` | `DATE` | **NO** | `CURRENT_DATE` | 출석 확인 일자 (`YYYY-MM-DD`) |

---

## 🔒 3. 데이터 무결성 및 인덱스 제약 조건

1. **외래키 제약조건 (Foreign Key)**:
   - `attendance.member_id` ➔ `members.id` (ON DELETE CASCADE)
   - 교인 정보 삭제 시 해당 교인의 과거 출석 체크 이력도 함께 안전하게 자동 정리됩니다.
2. **동명이인 지원 구조**:
   - `name` 컬럼에는 UNIQUE 제약이 없으므로 동명이인(이름이 동일한 여러 교인)의 등록이 완벽히 지원됩니다.
   - 모든 AI 벡터 및 출석 기록은 `id` (PK)를 기준으로 일대일 매칭됩니다.
3. **전화번호 자동 정제**:
   - DB 입력 시 엔티티 `@PrePersist` 및 `setPhone`을 통해 숫자가 아닌 문자를 모두 제거하여 `01073032538` 형식으로 규격화합니다.
