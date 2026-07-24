# ⛪ 교회 AI 얼굴인식 무인 출석 관리 시스템 (Cmm)

> **face-api.js 인공지능 딥러닝 얼굴인식 알고리즘**을 활용하여 주일 예배 및 집회 시 카메라 자동 스캔을 통해 무인으로 교인 출석을 처리하고, 등록 교인 명단 관리, 엑셀 일괄 등록, 한글 초성 검색 및 출석 통계 대시보드를 제공하는 Spring Boot 기반 웹 애플리케이션입니다.

---

## 📚 기술 상세 문서 목록 (Documentation)

- 📐 **[데이터베이스 엔티티 및 테이블 정의서 (ENTITY_DEFINITIONS.md)](file:///d:/Cmm/ENTITY_DEFINITIONS.md)**
- 💻 **[프로그램 명세서 및 시스템 아키텍처 (PROGRAM_SPECIFICATIONS.md)](file:///d:/Cmm/PROGRAM_SPECIFICATIONS.md)**
- 🚀 **[신규 서버 이관 및 환경 구축 설치 가이드 (DEPLOYMENT_GUIDE.md)](file:///d:/Cmm/DEPLOYMENT_GUIDE.md)**

---

## 📸 주요 기능 (Key Features)

### 1. 📹 AI 무인 출석 체크 (`/attendance`)
- **실시간 다중 얼굴 동시 추적 (Multi-Person Tracking)**:
  - 카메라 화면에 포착된 여러 명의 교인을 동시 추적하며 초록색 상자 및 성명/직분을 표시합니다.
- **AI 지속 연속 자가 학습 (Continuous Self-Learning & Sliding Window)**:
  - 출석을 지날 때마다 유의미하게 달라진 포즈 및 조명 조건의 이목구비 벡터를 **최대 10개 한도 내에서 자동으로 DB에 축적**합니다.
  - 고개 기울임(Roll) 및 쳐듬(Pitch) 각도 보정 골든 밸런스 임계값(`0.48`) 적용.
- **미등록 모달 & 즉시 자가 학습**:
  - 미등록 인식 시 모달 창에서 등록된 교인을 선택하여 **`✨ 선택 교인으로 즉시 학습`** 버튼 클릭 시 비디오 재시작 없이 메모리와 DB에 1초 만에 최신 포즈를 반영합니다.

### 2. 👤 다각도 3포즈 얼굴 등록 & 한글 초성 검색 (`/register`)
- **한글 초성(Chosung) 초고속 자동완성 검색**:
  - 성명 입력란에서 초성(예: `ㄱ`, `ㄱㄱㅇ`, `ㅎㄱㄷ`) 입력 시 사전 업로드 교인이 팝업 드롭다운으로 0.01초 만에 매칭되어 선택 시 성명, 직분, 부서, 연락처, 성별, 생년월일이 자동 입력됩니다.
- **다각도 3포즈 (정면 ➔ 좌측 ➔ 우측) 촬영 등록**:
  - 좌/우 윙 아이콘 버튼 UI를 통해 정면, 좌측, 우측 포즈를 3단계로 촬영하여 AI 인식률을 다각도로 정밀화합니다.

### 3. 👥 등록 교인 명단 & 엑셀 업로드 관리 (`/members`)
- **8컬럼 깔끔 레이아웃 & 마스킹 처리**:
  - `사진`, `교인명`, `성별`, `직분`, `소속 부서`, `연락처`, `상태`, `관리` 8개 컬럼으로 화면 정돈.
  - 연락처 중간자리 3~4자리 `*` 마스킹 표기 (`010-****-2538`). 수정 팝업 모달 시 원본 표시.
- **📥 엑셀 양식 다운로드 & 📤 일괄 명단 업로드**:
  - 표준 Excel 양식 폼(`church_members_template.xlsx`) 다운로드 및 `.xlsx`, `.csv` 명단 파일 일괄 업로드 지원.

### 4. 📊 출석 통계 & 캘린더 대시보드 (`/stats`)
- **수직 스크롤 제로 450px 컴팩트 캘린더**:
  - 캘린더 가로 폭 `450px` 유지 및 수직 스크롤 없는 한눈 조망 레이아웃.
- **시간대별 출석 그래프 & 일자별 상세 명단**:
  - Chart.js 06시~22시 시간대별 출석 분포 시각화 및 FullCalendar 날짜 클릭 시 당일 출석 교인 명단 리스트 제공.

---

## 🛠️ 기술 스택 (Technology Stack)

| 구분 | 사용 기술 / 라이브러리 |
| :--- | :--- |
| **Backend Framework** | Java 17 / Spring Boot 3.2.5, Spring Data JPA, Apache POI 5.2.5 |
| **Database** | PostgreSQL / H2 Database |
| **Build Tool** | Gradle 8.12 (`gradlew.bat`) |
| **Frontend** | HTML5, CSS3, JavaScript (ES6+), Thymeleaf |
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
│   │   │   │   └── AttendanceController.java # 출석, 교인관리, 엑셀, 통계 REST API & 뷰 컨트롤러
│   │   │   ├── entity/
│   │   │   │   ├── Member.java               # 교인 엔티티 (성명, 성별, 직분, 부서, 연락처, 생년월일, 사진, 얼굴벡터)
│   │   │   │   ├── Attendance.java           # 출석 로그 엔티티
│   │   │   │   └── ServiceType.java          # 예배 유형 엔티티
│   │   │   └── repository/
│   │   │       ├── MemberRepository.java     # 교인 DB 접근 인터페이스
│   │   │       └── AttendanceRepository.java # 출석 로그 DB 접근 인터페이스
│   │   └── resources/
│   │       ├── application.properties        # 포트(8081), DB 및 JPA 설정
│   │       └── templates/
│   │           ├── attendance.html           # 📹 무인 출석 화면
│   │           ├── register.html             # 👤 교인 얼굴 등록 화면
│   │           ├── members.html              # 👥 교인 명단 및 수정 관리 화면
│   │           └── stats.html                # 📊 출석 통계 & 캘린더 대시보드
├── build.gradle                              # Gradle 빌드 및 의존성 설정
├── gradlew.bat                               # Gradle 래퍼 실행 파일
├── ENTITY_DEFINITIONS.md                     # 📐 엔티티 및 DB 테이블 정의서
├── PROGRAM_SPECIFICATIONS.md                 # 💻 프로그램 명세서 및 아키텍처
├── DEPLOYMENT_GUIDE.md                       # 🚀 신규 서버 이관 및 환경 구축 가이드
└── README.md                                 # 프로젝트 통합 기술 개요서
```

---

## 🚀 빠른 기동 및 빌드 방법 (Quick Run & Build)

### 1. 서버 기동 (Run Server)
```bash
.\gradlew.bat bootRun
```
- 브라우저 접속: [http://localhost:8081](http://localhost:8081)

### 2. 실행 가능한 JAR 파일 빌드 (Build Executable JAR)
```bash
.\gradlew.bat build
java -jar build/libs/Cmm-0.0.1-SNAPSHOT.jar
```
