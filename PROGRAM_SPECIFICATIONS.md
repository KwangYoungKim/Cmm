# 💻 프로그램 명세서 및 시스템 아키텍처 (Program Specifications & Architecture)

> 본 문서는 **교회 AI 얼굴인식 무인 출석 관리 시스템 (Cmm)**의 시스템 구조, 컨트롤러 매핑, 화면 라우팅, REST API 명세 및 핵심 AI 알고리즘을 정의한 기술 문서입니다.

---

## 🏗️ 1. 시스템 아키텍처 (System Architecture)

```
[ Web Browser (Chrome/Edge) ]
       │
       ├─► HTML5 Camera Stream (Webcam)
       ├─► face-api.js (SSD MobileNet V1 + 68 Landmark + 128D ResNet)
       └─► UI Rendering (Vanilla CSS Glassmorphism + FullCalendar + Chart.js)
       │
       ▼  HTTP / REST API (Port: 8081)
┌─────────────────────────────────────────────────────────┐
│ Spring Boot Web Application (Java 17)                   │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ AttendanceController.java                           │ │
│ │ ├─ Page Navigation Views (/attendance, /members...) │ │
│ │ └─ REST API Services (/api/attendance, /api/member) │ │
│ └─────────────────────────────────────────────────────┘ │
│                           │                             │
│                           ▼                             │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Spring Data JPA Repositories                        │ │
│ │ (MemberRepository, AttendanceRepository)            │ │
│ └─────────────────────────────────────────────────────┘ │
└───────────────────────────┬─────────────────────────────┘
                            │
                            ▼
           [ PostgreSQL / H2 Database ]
```

---

## 🌐 2. 화면 라우팅 명세 (View Routing Specifications)

| URL 경로 | 뷰 파일 (Template) | 설명 및 주요 기능 |
| :--- | :--- | :--- |
| `GET /` | `redirect:/attendance` | 메인 루트 접속 시 무인 출석 페이지로 자동 리다이렉트 |
| `GET /attendance` | `attendance.html` | **📹 무인 실시간 출석 체크 센서 화면**<br>- 실시간 다중 인원 얼굴 감지 & 인식<br>- 1인 1회 출석 처리 & 쿨다운 타이머<br>- 미등록 모달 & 자가 학습 갱신 |
| `GET /members` | `members.html` | **👥 등록 교인 명단 & 수정 관리 화면**<br>- 8컬럼 깔끔 목록 & 전화번호 중간자리 `*` 마스킹<br>- 엑셀 양식 다운로드 & 엑셀/CSV 일괄 업로드<br>- 프로필 원본 확대 / 팝업 정보 수정 / 교인 삭제 |
| `GET /register` | `register.html` | **👤 다각도 3포즈 교인 얼굴 등록 화면**<br>- 1단계(정면) ➔ 2단계(좌) ➔ 3단계(우) 3포즈 촬영<br>- 한글 초성(`ㄱㄱㅇ`, `ㅎㄱㄷ`) 초고속 자동완성 검색<br>- 사전 업로드 교인 정보 자동 연결 |
| `GET /stats` | `stats.html` | **📊 출석 통계 & 캘린더 대시보드 화면**<br>- 450px 컴팩트 캘린더 & 세로 스크롤 제로 레이아웃<br>- Chart.js 시간대별 출석 분포 그래프<br>- 당일 출석 교인 상세 명단 조회 |

---

## 🔌 3. REST API 명세서 (API Specifications)

### 3.1. 교인 관리 API (`/api/member`, `/api/members`)

#### 1) 전체 교인 목록 조회
- **HTTP Method**: `GET`
- **Endpoint**: `/api/members`
- **Description**: 등록된 모든 교인 목록(성명, 직분, 부서, 연락처, 성별, 생년월일, AI 벡터, 섬네일)을 반환합니다.
- **Response**: `200 OK` [ `[ { "id": 1, "name": "김광용", "position": "목사", "department": "청년부", "phone": "01073032538", "gender": "남", "birthDate": "1990-01-01", "faceDescriptor": "[[...]]", "profileImage": "data:image/jpeg;base64,..." } ]` ]

#### 2) 교인 얼굴 등록 (신규 / 사전 업로드 매칭)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/member/register`
- **Request Body**:
  ```json
  {
    "memberId": 9,
    "name": "김광용",
    "position": "목사",
    "department": "청년부",
    "phone": "010-7303-2538",
    "gender": "남",
    "birthDate": "1990-01-01",
    "profileImage": "data:image/jpeg;base64,...",
    "descriptor": "[[128-float], [128-float], [128-float]]"
  }
  ```
- **Response**: `200 OK` `{ "success": true, "message": "'김광용' 교우님의 얼굴 등록이 완료되었습니다." }`

#### 3) 교인 정보 수정
- **HTTP Method**: `PUT`
- **Endpoint**: `/api/member/{id}`
- **Request Body**: `{ "name": "...", "position": "...", "department": "...", "phone": "...", "gender": "...", "birthDate": "...", "profileImage": "..." }`
- **Response**: `200 OK` `{ "success": true, "message": "교인 정보가 수정되었습니다." }`

#### 4) 교인 삭제
- **HTTP Method**: `DELETE`
- **Endpoint**: `/api/member/{id}`
- **Description**: 교인 레코드 및 연관 출석 이력 데이터를 함께 삭제 처리합니다.
- **Response**: `200 OK` `{ "success": true, "message": "교인 정보가 삭제되었습니다." }`

#### 5) 엑셀 업로드 양식 폼 다운로드
- **HTTP Method**: `GET`
- **Endpoint**: `/api/members/excel-template`
- **Response**: Excel File Stream (`church_members_template.xlsx`)

#### 6) 엑셀/CSV 교인 명단 일괄 업로드
- **HTTP Method**: `POST`
- **Endpoint**: `/api/members/upload-excel`
- **Form Param**: `file` (MultipartFile `.xlsx`, `.xls`, `.csv`)
- **Response**: `200 OK` `{ "success": true, "count": 25, "message": "총 25명의 교인 명단이 성공적으로 업로드되었습니다." }`

---

### 3.2. 출석 체크 API (`/api/attendance`)

#### 1) 실시간 출석 체크 & 자가 학습 축적
- **HTTP Method**: `POST`
- **Endpoint**: `/api/attendance/check`
- **Request Body**: `{ "memberId": 9, "descriptor": [128-float-array] }`
- **Response**: `200 OK` `{ "alreadyChecked": false, "message": "'김광용' 목사님 출석 확인되었습니다!" }`

#### 2) 수동 자가 학습 벡터 강제 축적
- **HTTP Method**: `POST`
- **Endpoint**: `/api/member/append-vector-manual`
- **Request Body**: `{ "memberId": 9, "descriptor": [128-float-array] }`
- **Response**: `200 OK` `{ "success": true, "message": "현시점 얼굴 포즈 학습이 즉시 반영되었습니다!" }`

---

## 🧠 4. 핵심 AI 알고리즘 명세 (AI Core Algorithms)

### 1) 자중 매칭 임계값 (Golden Balance Threshold: `0.48`)
- 고개를 갸우뚱 기울이거나(Roll) 위로 쳐드는(Pitch) 3D 포즈 변형 시 2D 랜드마크 왜곡으로 발생하는 거리를 오인식 없이 매칭하기 위해 임계값을 `0.48`로 정밀 설정했습니다.

### 2) 최대 10개 한도 슬라이딩 윈도우 (Sliding Window Algorithm)
- 최초 등록 시 캡처된 3개 정면/측면 포즈(`#1, #2, #3`)는 DB에 **영구 보존**됩니다.
- 4번째 이후 추가 저장되는 자가 학습 포즈는 **최대 10개 한도** 내에서 오래된 순서대로 교체되어 교인의 연령 변화 및 스타일 변화를 지속적으로 자동 학습합니다.

### 3) 한글 초성 추출 및 매칭 (Chosung Search)
- 한글 유니코드 공식 `(code - 44032) / 588`을 적용하여 `ㄱ~ㅎ` 초성을 추출하며, `ㄱㄱㅇ` 입력 시 `김광용`을 0.01초 만에 드롭다운으로 매칭합니다.
