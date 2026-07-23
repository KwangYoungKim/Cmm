# ⛪ 교회 AI 얼굴인식 무인 출석 관리 시스템 (Cmm)

> **face-api.js 인공지능 딥러닝 얼굴인식 알고리즘**을 활용하여 주일 예배 및 집회 시 카메라 자동 스캔을 통해 무인으로 교인 출석을 처리하고, 등록 교인 명단 관리 및 출석 통계 대시보드를 제공하는 Spring Boot 기반 웹 애플리케이션입니다.

---

## 📸 주요 기능 (Key Features)

### 1. 📹 AI 무인 출석 체크 (`/attendance`)
- **실시간 다중 얼굴 인식 (Multi-Person Concurrent Tracking)**:
  - 카메라 화면에 포착된 여러 명의 얼굴을 동시 추적하여 개별 캔버스 상자(Bounding Box)와 성명/직분을 표시합니다.
- **1인 1회 중복 출석 방지**:
  - 당일 이미 출석한 교인은 "이미 출석 완료되었습니다" 안내 메시지와 출석 시간을 표시하며 비동기 처리합니다.
- **미등록 방문자 / 새가족 자동 인식 & 안내**:
  - 미등록 얼굴 인식 시 팝업 모달을 제공하여 **"방문자/새가족으로 출석 인정"** 또는 **"신규 교인 얼굴 등록"**으로 유연하게 연결합니다.
  - 동일 미등록 얼굴 10초 쿨다운 및 5초 자동 닫힘 모달 타이머 제공.
- **카메라 디바이스 제어 & 예외 처리**:
  - 카메라 좌우 반전(셀카 뷰) 전환 기능 제공.
  - 웹캠 권한 거부, 디바이스 미연결, HTTP 접속 보안 제한 시 안내 메시지 제공.

### 2. 👤 교인 얼굴 및 프로필 등록 (`/register`)
- **교인 상세 정보 입력**:
  - 성명, 직분(교우, 집사, 권사, 장로, 목사, 전도사, 기타 수기 입력 지원), 소속 부서, 연락처 입력.
- **실시간 중복 얼굴 등록 방지**:
  - 128차원 얼굴 특징 벡터 수치 거리(Distance < 0.45)를 비교하여 동일 물리 인물의 중복 등록을 차단합니다.
  - **동명이인 지원**: 동명이인(이름은 같으나 얼굴이 다른 경우)은 고유 ID(`member_id`) 기반으로 완벽히 구별하여 등록을 허용합니다.
- **프로필 섬네일 자동 생성**:
  - 등록 시 카메라에 찍힌 얼굴 영역을 자동 크롭하여 프로필 섬네일 이미지(`profileImage`)로 DB에 저장합니다.

### 3. 👥 등록 교인 명단 & 수정 관리 (`/members`)
- **교인 목록 및 원형 프로필 섬네일**:
  - 등록된 모든 교인의 정보와 프로필 사진을 목록 테이블 형태로 조회합니다.
- **프로필 사진 크게 보기 & 이미지 교체**:
  - 섬네일 클릭 시 원본 사진을 팝업 모달로 크게 확대해 보거나 컴퓨터의 사진 파일로 교체 업로드할 수 있습니다.
- **실시간 명단 검색 & 직분 필터링**:
  - 성명, 직분, 부서, 연락처 키워드 실시간 검색 및 직분별 필터링 기능.
- **교인 정보 수정 & 삭제**:
  - 교인의 성명, 직분, 부서, 연락처, 사진 수정(`PUT /api/member/{id}`) 및 교인 정보와 연관 출석 이력 정리 삭제(`DELETE /api/member/{id}`).

### 4. 📊 출석 통계 & 캘린더 대시보드 (`/stats`)
- **출석 현황 요약 카드**: 당일 출석 교인 수, 방문자 수, 등록 교인 대비 출석률 계산.
- **시간대별 출석 그래프**: Chart.js 기반 06시~22시 시간대별 출석 분포 시각화.
- **월별 출석 캘린더**: FullCalendar 연동 출석 날짜별 집계 및 날짜 클릭 시 당일 출석 교인 상세 명단 조회.

---

## 🛠️ 기술 스택 (Technology Stack)

| 구분 | 사용 기술 / 라이브러리 |
| :--- | :--- |
| **Backend Framework** | Java 17 / 23, Spring Boot 3.2.5, Spring Data JPA |
| **Database** | PostgreSQL / H2 Database |
| **Build Tool** | Gradle 8.12 (`gradlew.bat`) |
| **Frontend** | HTML5, CSS3 (Vanilla CSS Glassmorphism Design), JavaScript (ES6+), Thymeleaf |
| **AI / Computer Vision** | face-api.js (SSD MobileNet V1, Face Landmark 68, Face Recognition) |
| **Data Visualization** | Chart.js, FullCalendar 6 |

---

## 📂 프로젝트 디렉토리 구조 (Directory Structure)

```
Cmm/
├── src/
│   ├── main/
│   │   ├── java/com/example/cmm/
│   │   │   ├── CmmApplication.java           # Spring Boot 메인 클래스
│   │   │   ├── controller/
│   │   │   │   └── AttendanceController.java # 출석, 교인관리, 통계 REST API & 뷰 라우팅
│   │   │   ├── entity/
│   │   │   │   ├── Member.java               # 교인 엔티티 (이름, 직분, 부서, 연락처, 프로필사진, 얼굴벡터)
│   │   │   │   ├── Attendance.java           # 출석 로그 엔티티
│   │   │   │   └── ServiceType.java          # 예배 유형 엔티티
│   │   │   └── repository/
│   │   │       ├── MemberRepository.java     # 교인 DB 접근 인터페이스
│   │   │       └── AttendanceRepository.java # 출석 로그 DB 접근 및 삭제 인터페이스
│   │   └── resources/
│   │       ├── application.properties        # 포트(8081), DB 및 JPA 설정
│   │       └── templates/
│   │           ├── attendance.html           # 📹 무인 출석 화면
│   │           ├── register.html             # 👤 교인 얼굴 등록 화면
│   │           ├── members.html              # 👥 교인 명단 및 수정 관리 화면
│   │           └── stats.html                # 📊 출석 통계 & 캘린더 대시보드
├── build.gradle                              # Gradle 빌드 및 의존성 설정
├── gradlew.bat                               # Gradle 래퍼 실행 파일
└── README.md                                 # 프로젝트 기술 명세서
```

---

## 🚀 기동 및 빌드 방법 (Run & Build)

### 1. 포그라운드 서버 기동 (Run Server)
```bash
.\gradlew.bat bootRun
```
- 기동 완료 후 브라우저 접속:
  - 무인 출석 체크: [http://localhost:8081/attendance](http://localhost:8081/attendance)
  - 교인 얼굴 등록: [http://localhost:8081/register](http://localhost:8081/register)
  - 교인 명단 & 수정: [http://localhost:8081/members](http://localhost:8081/members)
  - 출석 통계 & 캘린더: [http://localhost:8081/stats](http://localhost:8081/stats)

### 2. 프로젝트 빌드 (Build Executable JAR)
```bash
.\gradlew.bat build
```
- 빌드 산출물 위치: `build/libs/Cmm-0.0.1-SNAPSHOT.jar`

---

## 📜 라이선스 (License)
본 프로젝트는 자유롭게 활용 및 커스텀 개발이 가능합니다.
