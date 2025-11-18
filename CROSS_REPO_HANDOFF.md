# 교차 레포 이관 항목 정리

Auth Service에서 파생된 개선 과제 중, **Gateway** 또는 **User Service**에서 처리해야 할 작업을 아래에 정리했습니다. 각 항목은 GitHub Issue 번호(있을 경우)와 세부 요구사항, 참고 파일·문서를 포함합니다.

---

## 🚪 Gateway (openknot-gateway-service)

### 1. Access Token 검증 책임 유지·고도화
- **현황:** `com.openknot.gateway.filter.AuthenticationFilter`에서 JWT 검증, 만료·서명 오류 처리, `X-User-Id` 주입을 담당.
- **필요 작업:**
  - Auth Service와 인터페이스 문서화 (`X-User-Id`, 오류 코드 매핑).
  - Refresh/Logout 엔드포인트 추가 시 whitelist 업데이트.
  - 토큰 서명 키 로테이션 시 Gateway와 동기화 절차 정의.
- **참고:** Gateway 코드 스니펫(이미 공유됨), RFC 8725 (JWT 보안).

### 2. Rate Limiting 구현 (IMPROVEMENT_CHECKLIST High Priority)
- **이슈 상태:** Auth 레포에는 생성하지 않음 (Gateway 책임).
- **필요 작업:**
  - Bucket4j 또는 Redis 기반 rate limiter를 Gateway 필터로 구현.
  - `/api/auth/login`, `/api/auth/refresh` 등 민감 엔드포인트에 IP/사용자 단위 제한 적용.
  - 차단 시 `429 Too Many Requests` 응답 + Retry-After 헤더 포함.
- **참고:** OWASP DoS Cheat Sheet, Bucket4j 문서.

### 3. Security Headers & CORS 정책 관리 (#16 이관)
- **기존 이슈:** #16 `[SECURITY] Security Headers 추가 (CORS, CSP, X-Frame-Options)`
- **이관 사유:** 모든 외부 요청이 Gateway를 통과하므로 보안 헤더·CORS 정책은 Gateway에서 단일하게 적용하는 것이 적절.
- **필요 작업:**
  - `X-Frame-Options`, `Content-Security-Policy`, `Strict-Transport-Security`, `Referrer-Policy`, `Permissions-Policy` 등 헤더 설정.
  - 허용 Origin, 메서드, 헤더를 한 곳에서 관리.
- **참고:** OWASP Secure Headers Project.

### 4. Request/Response 로깅 필터 (#10 이관)
- **기존 이슈:** #10 `[ENHANCE] Request/Response 로깅 필터 추가`
- **이관 사유:** 전 서비스 공통 로깅 요구(요청/응답 payload, 소요 시간 등)는 Gateway에서 수집해야 전체 흐름 추적 가능.
- **필요 작업:**
  - WebFilter 혹은 GlobalFilter로 요청/응답 로깅, 마스킹 정책, 샘플링 비율 정의.
  - MDC에 Correlation ID 포함(다음 항목과 연계).

### 5. Correlation ID 생성·전파 (#15 이관)
- **기존 이슈:** #15 `[ENHANCE] Correlation ID 구현 (분산 추적)`
- **이관 사유:** 요청 진입점에서 Correlation ID를 생성/검증해야 모든 마이크로서비스가 동일한 ID를 공유.
- **필요 작업:**
  - Gateway 필터에서 `X-Correlation-Id` 생성(미존재 시) 및 검증.
  - Downstream 호출 시 헤더 전달, 로그 MDC 세팅.
  - Auth Service 포함 모든 서비스는 받은 헤더를 로그에 사용.

---

## 👤 User Service (openknot-user-service)

### 1. 자격 증명 검증 API 전환 (POST /api/validate-credentials)
- **원본 작업:** IMPROVEMENT_CHECKLIST Critical 항목 “User Service API 수정 필요”
- **요구사항:**
  - 기존 `GET /user-id?email=&password=` → `POST /api/validate-credentials`
  - `CredentialValidationRequest { email: String, password: String }`
  - 비밀번호는 Request Body로 전송, Validation 어노테이션 적용.
  - 응답: `{ userId: String, roles: [String] }` (roles는 다음 항목과 연계 가능).
- **근거:** 비밀번호를 쿼리 파라미터로 전달하면 URL/로그에 노출됨. REST 의미론상 인증은 POST가 적절.
  - 참고: OWASP API Security Top 10 A03 (Sensitive Data Exposure)

### 2. Role 동적 조회/전달 지원 (#17 연동)
- **관련 이슈:** #17 `[FEAT] Role 동적 할당 구현`
- **필요 작업:**
  - User Service가 사용자별 Role 목록을 Auth Service로 반환.
  - 관리자가 추가 역할을 부여할 수 있도록 API/DB 스키마 확장.
  - Auth Service는 반환된 roles를 JWT `claim("roles", ...)`로 포함.
- **참고:** Spring Security Role 기반 권한 모델, RBAC 모범 사례.

### 3. 비밀번호 정책 및 응답 개선
- **확인 필요 사항:**
  - Bcrypt/Scrypt 등 해시 알고리즘 사용 여부와 API 스펙 공유.
  - 실패 시 오류 코드/메시지 표준화 (Auth Service와 일관되게).

---

## 📝 진행 가이드

1. **Issue 이관**
   - Gateway 레포: #10, #15, #16 내용을 참조해 신규 이슈 생성 후 Auth 레포 이슈는 `wontfix` 또는 참고 링크로 닫기.
   - User Service 레포: 위 2개의 핵심 과제를 이슈화하고 Auth 레포의 요구사항 링크 첨부.

2. **문서화**
   - 각 레포의 README 혹은 ADR에 책임 범위를 명시해 중복 개발 방지.
   - Gateway ↔ Auth ↔ User Service 간 API 계약을 `.md` 혹은 OpenAPI로 관리.

3. **협업 체크포인트**
   - JWT 서명 키/Role 데이터/Whitelist 경로 등 변경 시 3개 서비스가 동시에 업데이트되도록 운영 절차 마련.

이 문서를 기준으로 교차 레포 작업을 정리하면, 각 팀이 맡아야 할 책임이 명확해지고 Auth Service의 범위도 선명해집니다. 필요한 항목이 추가되면 본 문서를 업데이트해주세요.
