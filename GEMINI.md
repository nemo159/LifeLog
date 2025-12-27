# LifeLog 개발 가이드라인

모든 기능 계획(feature plans)에서 자동 생성됨. 마지막 업데이트: 2025-12-27

## 활성 기술 스택 (Active Technologies)

- **Android (Target SDK 36+)**: 최신 안드로이드 정책 준수 (예: Photo Picker 강제 등).
- **Kotlin 2.x**: 최신 언어 기능 활용.
- **Jetpack Compose**: 100% 선언형 UI 프레임워크 사용.
- **Hilt**: 의존성 주입 (Dependency Injection) 표준 라이브러리.
- **Room**: 로컬 데이터베이스 (SQLite 추상화).
- **WorkManager**: 안정적인 백그라운드 작업 처리.
- **Clean Architecture**: 관심사 분리 (Domain, Data, Presentation).

## 프로젝트 구조 (Project Structure)

```text
app/src/main/java/com/rmtm/lifelog/
├── core/model/        # 도메인 모델 (순수 Kotlin)
│                      # - 안드로이드 프레임워크 의존성이 없어야 함.
│                      # - 비즈니스 로직의 핵심 데이터 구조.
├── data/              # 데이터 계층
│                      # - Room DB, DAO, Entity, Repository 구현체.
│                      # - 데이터 소스와 도메인 모델 간의 변환(Mapper) 담당.
├── di/                # Hilt 모듈
│                      # - 의존성 주입 설정 (Singleton, ViewModelScoped 등).
├── feature/           # 기능별 UI (화면 단위)
│                      # - Timeline, Editor 등 화면별 패키지 구성.
│                      # - ViewModel과 Composable 함수가 위치함.
├── navigation/        # 네비게이션
│                      # - 화면 이동 경로(Route) 및 NavHost 정의.
└── ui/theme/          # 디자인 시스템
                       # - 색상(Color), 테마(Theme), 타이포그래피(Type).
```

## 명령어 (Commands)

- `./gradlew build`: 전체 프로젝트 빌드.
- `./gradlew test`: 로컬 유닛 테스트 실행 (JVM 기반).
- `./gradlew connectedCheck`: 기기 연결 테스트 실행 (Instrumented Test).
- `./gradlew help`: 사용 가능한 Gradle 태스크 목록 확인.

## 코드 스타일 (Code Style)

- **Kotlin**
  - 공식 Kotlin 스타일 가이드를 준수합니다.
  - 후행 쉼표(Trailing commas)를 사용하여 변경 사항(Diff)을 최소화합니다.
  - 간단한 함수는 표현식 본문(`fun foo() = ...`)을 선호합니다.

- **Compose**
  - 모든 UI 구성 요소는 `@Composable` 함수로 작성합니다.
  - **State Hoisting(상태 호이스팅)** 패턴을 준수하여, 상태를 호출자에게 위임하고 컴포넌트는 재사용 가능하게 만듭니다.
  - `remember`와 `derivedStateOf`를 적절히 사용하여 불필요한 리컴포지션을 방지합니다.

- **Architecture (아키텍처)**
  - Clean Architecture 원칙을 엄격히 따릅니다.
  - **Core/Model**은 Room이나 Android 의존성을 가지면 안 됩니다.
  - **Mapper**를 사용하여 DB Entity와 Domain Model을 명확히 분리 및 변환해야 합니다.

- **DI (의존성 주입)**
  - 모든 의존성은 `@Inject`와 Hilt 모듈을 통해 주입받습니다.
  - 클래스 내부에서 직접 객체를 생성(`new Class()`)하는 것을 지양합니다.

## 최근 변경 사항 (Recent Changes)

- **초기 설정 (Initial Setup)**: Hilt, Room, Compose를 포함한 프로젝트 스캐폴딩 완료.
- **프로젝트 개요 (Project Overview)**: `specs/overview.md` 작성. MVP 범위 및 아키텍처 정의.
- **GEMINI.md**: AI 상호작용 및 개발 가이드라인 초기화.

<!-- MANUAL ADDITIONS START -->
## 상호작용 기본 설정 (Interaction Preferences)

- **언어 (Language)**: 사용자의 요청에 따라 **한국어**로 응답합니다.
- **어조 (Tone)**: 전문적이고, 직접적이며, 간결하게(CLI 스타일) 응답합니다.
- **보안 (Security)**: 미디어 접근 시 권한 요청 대신 **Android Photo Picker**를 사용하여 Target SDK 36 정책 및 프라이버시를 준수합니다.
- **효율성 (Efficiency)**: 도구(Tool) 출력의 장황함을 최소화합니다. 관련된 작업은 그룹화하여 "실행 허용(Allow execution)" 확인 횟수를 줄입니다.
- **프라이버시 (Privacy)**: 사용자 데이터는 절대 외부로 전송하지 않으며, 모든 데이터는 로컬에만 저장합니다.
<!-- MANUAL ADDITIONS END -->