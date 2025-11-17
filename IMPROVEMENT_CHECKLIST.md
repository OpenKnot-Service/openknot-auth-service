# OpenKnot Auth Service - 개선 사항 체크리스트

> 코드 분석 날짜: 2025-11-17
> 이 문서는 인증 서비스의 개선이 필요한 항목들을 우선순위별로 정리한 체크리스트입니다.

---

## 🔴 즉시 수정 필요 (Critical Priority)

### 보안 취약점

* application <-- gitignore 되어있음
- [x] **Redis 비밀번호 노출 제거**
  - 파일: `src/main/resources/application.yml`
  - 문제: Redis 비밀번호가 하드코딩되어 있고 git에 노출될 위험
  - 조치:
    - [ ] git history에서 민감 정보 제거
    - [ ] Redis 비밀번호 즉시 교체
    - [ ] 환경 변수로 변경
    - [x] `.gitignore` 정상 작동 확인

- [x] **JWT Secret 외부화**
  - 파일: `src/main/resources/application.yml`
  - 문제: JWT secret이 하드코딩되어 모든 환경에서 동일
  - 조치:
    - [ ] 환경별 다른 secret 사용
    - [x] 환경 변수로 관리
    - [ ] 랜덤하게 생성된 강력한 secret 사용

* Q: MSA 프로젝트라 내부에서 서버 끼리만 fegin으로 이동하는데도 문제가 있을까?
  * A: 여전히 문제 있음 (로그 노출, URL 길이 제한, 의미론적 문제, 보안 모범 사례)
- [x] **비밀번호 전송 방식 변경**
  - 파일: `src/main/kotlin/com/openknot/auth/feign/client/UserClient.kt`
  - 문제: 비밀번호가 GET 쿼리 파라미터로 전송됨
  - 조치:
    - [x] GET → POST로 변경 (WebClient 사용)
    - [x] 비밀번호를 request body에 포함
    - [ ] User Service API도 함께 수정 (User Service 쪽 작업 필요)

- [x] **입력 검증 추가**
  - 파일: `src/main/kotlin/com/openknot/auth/dto/LoginRequest.kt`
  - 문제: 이메일/비밀번호 검증이 전혀 없음
  - 조치:
    - [x] `@Email` 어노테이션 추가
    - [x] `@NotBlank` 어노테이션 추가
    - [ ] `@Size` 어노테이션으로 길이 제한
    - [x] Controller에 `@Valid` 추가

### 치명적 버그

- [x] **Refresh Token 저장 로직 구현**
  - 파일: `src/main/kotlin/com/openknot/auth/service/AuthService.kt:14`
  - 문제: 로그인 시 refresh token을 생성하지만 Redis에 저장하지 않음
  - 조치:
    ```kotlin
    val token = jwtProvider.generateTokens(userId, "ROLE_USER")
    refreshTokenRepository.saveToken(
        RefreshToken(
            userId = userId,
            token = token.refreshToken,
            expiryTime = System.currentTimeMillis() + tokenProperties.refreshTokenExpiration
        )
    )
    return token
    ```

- [x] **Refresh Token TTL 수정**
  - 파일: `src/main/kotlin/com/openknot/auth/repository/RefreshTokenRepository.kt:15`
  - 문제: 설정은 7일인데 코드는 2시간으로 하드코딩
  - 조치:
    - [x] `Duration.ofHours(2)` → `Duration.ofMillis(tokenProperties.refreshTokenExpiration)` 변경
    - [x] TODO 주석 제거

- [x] **RefreshTokenRepository 데이터 타입 수정**
  - 파일: `src/main/kotlin/com/openknot/auth/repository/RefreshTokenRepository.kt:12`
  - 문제: userId만 저장하지만 RefreshToken 객체를 조회 시도
  - 조치:
    - [x] 이중 저장 구현 (token key + userId key)
    - [x] RefreshToken 객체 저장
    - [ ] TODO: 성능 개선 (userIdKey에는 token 값만 저장하도록 최적화 필요)

- [x] **Feign Client 블로킹 이슈 해결**
  - 파일: `src/main/kotlin/com/openknot/auth/feign/client/UserClient.kt`
  - 문제: 동기 Feign이 reactive thread를 블로킹
  - 조치:
    - [x] WebClient로 교체 완료
    - [x] 완전 reactive 구현 (suspend 함수)
    - [x] POST 방식으로 변경하여 보안도 개선
    - [x] 에러 처리 및 로깅 개선
    - [x] Feign 의존성 제거

---

## 🟠 높은 우선순위 (High Priority)

### 보안 강화

- [ ] **Rate Limiting 구현**
  - 파일: `src/main/kotlin/com/openknot/auth/controller/AuthController.kt`
  - 조치:
    - [ ] Bucket4j 의존성 추가
    - [ ] IP 기반 rate limiting 구현
    - [ ] 로그인 시도 횟수 제한 (예: 5회/분)

- [ ] **Security Headers 추가**
  - 조치:
    - [ ] CORS 설정 추가
    - [ ] Security headers 설정 (X-Frame-Options, X-Content-Type-Options 등)
    - [ ] Request size limit 설정

### 핵심 기능 구현

- [ ] **Token 검증 메서드 구현**
  - 파일: `src/main/kotlin/com/openknot/auth/provider/JwtProvider.kt`
  - 조치:
    - [ ] `validateToken(token: String): Boolean` 추가
    - [ ] `getClaimsFromToken(token: String): Claims` 추가
    - [ ] `isRefreshTokenValid(token: String): Boolean` 추가

- [ ] **Refresh Token 엔드포인트 추가**
  - 파일: `src/main/kotlin/com/openknot/auth/controller/AuthController.kt`
  - 조치:
    - [ ] `RefreshTokenRequest` DTO 생성
    - [ ] `POST /refresh` 엔드포인트 구현
    - [ ] AuthService에 refresh 로직 추가
    - [ ] 기존 refresh token 검증 및 새 access token 발급

- [ ] **Logout 기능 구현**
  - 조치:
    - [ ] `POST /logout` 엔드포인트 추가
    - [ ] Redis에서 refresh token 삭제
    - [ ] Token blacklist 구현 (선택사항)

- [ ] **Role 동적 할당**
  - 파일: `src/main/kotlin/com/openknot/auth/service/AuthService.kt:14`
  - 문제: "ROLE_USER" 하드코딩
  - 조치:
    - [ ] User Service에서 실제 role 조회
    - [ ] 여러 role 지원

### 코드 품질

- [ ] **로깅 전략 수립**
  - 조치:
    - [x] 주요 클래스에 logger 추가 (UserClient, UserFacade)
    - [x] `printStackTrace()` 제거 (UserFacade에서 제거됨)
    - [ ] 구조화된 로깅 적용
    - [ ] Log4j2 설정 파일 생성 (`src/main/resources/log4j2.xml`)

- [x] **@Transactional 어노테이션 수정**
  - 파일: `src/main/kotlin/com/openknot/auth/service/AuthService.kt`
  - 문제: `readOnly = true`이지만 Redis 쓰기 작업 수행
  - 조치:
    - [x] `@Transactional` 제거 완료 (Redis는 DB 트랜잭션 아님)

---

## 🟡 중간 우선순위 (Medium Priority)

### 테스트

- [ ] **통합 테스트 작성**
  - 조치:
    - [ ] 로그인 플로우 테스트
    - [ ] Token 생성/검증 테스트
    - [ ] Redis 연동 테스트
    - [ ] Feign 클라이언트 테스트
    - [ ] 에러 시나리오 테스트

- [ ] **테스트 의존성 추가**
  - 파일: `build.gradle.kts`
  - 조치:
    - [ ] MockK 추가
    - [ ] Embedded Redis 추가
    - [ ] TestContainers 추가 (선택사항)
    - [ ] Mockito-kotlin 추가 (선택사항)

### API 개선

- [ ] **API 문서화**
  - 조치:
    - [ ] Swagger/OpenAPI 의존성 추가
    - [ ] Controller에 API 문서 어노테이션 추가
    - [ ] `/swagger-ui.html` 엔드포인트 확인

- [ ] **API 기본 경로 추가**
  - 파일: `src/main/kotlin/com/openknot/auth/controller/AuthController.kt`
  - 조치:
    - [ ] `@RequestMapping("/api/auth")` 추가
    - [ ] 엔드포인트: `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`

- [ ] **에러 응답 개선**
  - 조치:
    - [ ] `ErrorResponse`에 timestamp, path, traceId 추가
    - [ ] Field-level validation 에러 포함
    - [ ] HTTP 상태 코드 포함

- [ ] **Request/Response 로깅 추가**
  - 조치:
    - [ ] WebFilter 생성
    - [ ] 요청/응답 로깅
    - [ ] Correlation ID 포함

### Health Check & Monitoring

- [ ] **Custom Health Indicator 추가**
  - 조치:
    - [ ] `RedisHealthIndicator` 구현
    - [ ] `UserServiceHealthIndicator` 구현
    - [ ] `/actuator/health`에서 확인

- [ ] **Metrics 추가**
  - 조치:
    - [ ] Micrometer 설정
    - [ ] 로그인 성공/실패율 메트릭
    - [ ] Token 생성률 메트릭
    - [ ] 인증 지연시간 메트릭

### 성능 개선

- [ ] **YamlMessageSource 싱글톤화**
  - 파일: `src/main/kotlin/com/openknot/auth/converter/MessageConverter.kt:9`
  - 조치:
    - [ ] `@Bean`으로 변경
    - [ ] `@PostConstruct`로 초기화
    - [ ] 인스턴스 재사용

- [ ] **Redis 연결 풀 설정**
  - 파일: `src/main/resources/application.yml`
  - 조치:
    ```yaml
    spring:
      data:
        redis:
          lettuce:
            pool:
              max-active: 8
              max-idle: 8
              min-idle: 2
    ```

- [ ] **Redis 작업 결과 확인**
  - 파일: `src/main/kotlin/com/openknot/auth/repository/CoroutineRedisRepository.kt`
  - 조치:
    - [ ] `save()` 메서드에서 Boolean 반환
    - [ ] 실패 시 에러 처리

### 코드 리팩토링

- [ ] **Magic String 상수화**
  - 조치:
    - [ ] "refresh_token:" → `REFRESH_TOKEN_PREFIX`
    - [ ] "Bearer" → `BEARER_PREFIX`
    - [ ] "ROLE_USER" → `DEFAULT_ROLE`
    - [ ] companion object 또는 Constants 파일 생성

- [ ] **타입 안전성 개선**
  - 파일: `src/main/kotlin/com/openknot/auth/repository/CoroutineRedisRepository.kt:17`
  - 조치:
    - [ ] `inline suspend fun <reified T> get()` 사용
    - [ ] 타입 불일치 시 로깅 추가
    - [ ] 타입 불일치 시 예외 발생 고려

### Docker & 배포

- [ ] **Dockerfile 보안 개선**
  - 파일: `Dockerfile`
  - 조치:
    - [ ] non-root 사용자 추가 및 사용
    - [ ] JVM 메모리 옵션 추가
    - [ ] Health check 추가
    - [ ] Wildcard copy 제거, 특정 jar 파일 지정

- [ ] **Jenkins Pipeline 개선**
  - 파일: `Jenkinsfile`
  - 조치:
    - [ ] Docker image cleanup stage 추가
    - [ ] 보안 스캔 추가 (Trivy 등)
    - [ ] 환경 변수 주입 방식 개선

---

## 🟢 낮은 우선순위 (Low Priority)

### 아키텍처 개선

- [ ] **TokenProvider 인터페이스 생성**
  - 조치:
    - [ ] `TokenProvider` 인터페이스 정의
    - [ ] `JwtProvider`가 인터페이스 구현
    - [ ] `AuthService`에서 인터페이스 의존

- [ ] **Circuit Breaker 추가**
  - 조치:
    - [ ] Resilience4j 의존성 추가
    - [ ] User Service 호출에 circuit breaker 적용
    - [ ] Fallback 전략 구현
    - [ ] Timeout 설정

- [ ] **Correlation ID 구현**
  - 조치:
    - [ ] Correlation ID 필터 추가
    - [ ] MDC에 저장
    - [ ] Feign 호출 시 헤더에 포함
    - [ ] 로그에 포함

### 고급 기능

- [ ] **API 버전 관리**
  - 조치:
    - [ ] `/api/v1/auth` 경로 구조 적용
    - [ ] 또는 헤더 기반 버전 관리
    - [ ] 버전 관리 전략 문서화

- [ ] **Graceful Shutdown**
  - 조치:
    - [ ] 진행 중인 요청 완료 대기
    - [ ] Redis 연결 정리
    - [ ] Shutdown hook 추가

- [ ] **Domain Model 개선**
  - 파일: `src/main/kotlin/com/openknot/auth/entity/RefreshToken.kt`
  - 조치:
    - [ ] `isExpired()` 메서드 추가
    - [ ] 비즈니스 로직을 entity로 이동

### 문서화

- [ ] **KDoc 추가**
  - 조치:
    - [ ] 모든 public 클래스에 KDoc 추가
    - [ ] 복잡한 메서드에 설명 추가
    - [ ] Parameter 설명 추가

- [ ] **README 업데이트**
  - 조치:
    - [ ] API 엔드포인트 목록 추가
    - [ ] 개발 환경 설정 가이드
    - [ ] 배포 가이드
    - [ ] 환경 변수 목록

### 설정

- [ ] **환경별 설정 파일 생성**
  - 조치:
    - [ ] `application-dev.yml` 생성
    - [ ] `application-local.yml` 생성
    - [ ] `application-test.yml` 생성
    - [ ] 각 환경별 적절한 설정

- [ ] **의존성 버전 관리 개선**
  - 파일: `build.gradle.kts`
  - 조치:
    - [ ] Spring Cloud 버전 확인 (2025.0.0 → 안정 버전)
    - [ ] 버전을 변수로 추출
    - [ ] dependencyManagement 활용

- [ ] **.gitignore 수정 및 확인**
  - 조치:
    - [ ] `git rm --cached src/main/resources/application.yml` 실행
    - [ ] `.gitignore`에 올바른 패턴 확인
    - [ ] `application-*.yml` 파일들도 제외
    - [ ] git status로 확인

### Kubernetes (선택사항)

- [ ] **K8s Manifest 생성**
  - 조치:
    - [ ] Deployment YAML 생성
    - [ ] Service YAML 생성
    - [ ] ConfigMap/Secret 생성
    - [ ] Ingress 설정 (필요시)

---

## 📝 참고사항

### 작업 순서 권장사항

1. **✅ 1단계: 보안 수정** (대부분 완료)
   - ✅ 입력 검증 추가 (@Email, @NotBlank, @Valid)
   - ✅ 비밀번호 전송 방식 변경 (GET → POST)
   - ✅ JWT Secret 환경 변수화
   - ⚠️ 민감 정보 git history 제거 (아직 미완)

2. **✅ 2단계: 핵심 버그 수정** (완료)
   - ✅ Refresh token 저장 로직 구현
   - ✅ TTL 수정 (7일)
   - ✅ 데이터 타입 수정 (UUID → String, expiryTime 제거)
   - ✅ Feign 블로킹 이슈 해결 (WebClient 전환)

3. **🔄 3단계: 필수 기능 추가** (진행 예정 - High 섹션)
   - [ ] Token 검증 메서드
   - [ ] Refresh 엔드포인트
   - [ ] Logout 기능

4. **📝 4단계: 테스트 작성** (Medium 섹션 테스트)
   - [ ] 통합 테스트로 기능 검증

5. **📝 5단계: 점진적 개선** (Medium, Low 섹션)
   - [ ] 코드 품질 향상
   - [ ] 모니터링 추가
   - [ ] 문서화

### 추가 도구 추천

- **보안 스캔**: Snyk, OWASP Dependency Check
- **코드 품질**: SonarQube, Detekt
- **API 테스트**: Postman, HTTPie
- **부하 테스트**: K6, Gatling

---

## 📊 진행 상황 요약

### Critical Priority (즉시 수정 필요)
- 완료: 8/9 항목 (88%)
- 보안 취약점: 2/4 완료
- 치명적 버그: 4/4 완료 ✅
- 남은 작업: User Service API 수정, git history 정리

### High Priority (높은 우선순위)
- 완료: 2/7 항목 (29%)
- 코드 품질: 2/2 완료 ✅
- 남은 작업: 보안 강화, 핵심 기능 구현

### 주요 성과
✅ **완전 Reactive 전환**: Feign → WebClient로 마이그레이션
✅ **보안 개선**: GET → POST, 입력 검증 추가
✅ **버그 수정**: Refresh Token 저장, TTL, 데이터 타입 모두 해결
✅ **코드 품질**: @Transactional 제거, 로깅 개선

### 다음 단계
1. User Service API 수정 (POST /api/validate-credentials)
2. Token 검증 메서드 구현
3. Refresh/Logout 엔드포인트 추가

---

**마지막 업데이트**: 2025-11-17
**최근 수정**: WebClient 전환 완료, @Transactional 제거
