# 구현 계획: MVP 핵심 기능 (Core Features)

**연관 명세**: `specs/001-mvp-core.md`  
**작성일**: 2025-12-27  
**상태**: Planned

## 목표 (Objective)
LifeLog 앱의 핵심 가치인 **기록(작성)**과 **회고(조회)** 기능을 구현합니다.  
특히 Android 최신 정책(Target SDK 36)에 맞춰 **Photo Picker**를 연동하고, 이미지를 내부 저장소로 안전하게 복사하는 로직을 포함합니다.

## 구현 전략 (Implementation Strategy)
1. **데이터 계층 (Data Layer)**: 이미지 파일 관리 로직(`ImageStorageManager`)을 우선 구현하여, Repository가 `Content URI`를 실제 파일로 변환할 수 있게 합니다.
2. **기능 계층 - 에디터 (Feature - Editor)**: Photo Picker 연동 및 UI 상태 관리를 구현합니다.
3. **기능 계층 - 타임라인 (Feature - Timeline)**: DB 데이터를 `LazyColumn`으로 렌더링합니다.
4. **네비게이션 (Navigation)**: 화면 간 이동을 연결합니다.

---

## 단계별 계획 (Step-by-Step Plan)

### Step 1: 이미지 처리 인프라 구축 (Infrastructure)
- **목표**: 갤러리 원본 의존성을 제거하고 앱 전용 이미지 사본 생성.
- **작업 내용**:
    - [ ] `core/common` 또는 `data/local` 패키지에 `ImageStorageManager` 클래스 생성.
    - [ ] `context.contentResolver.openInputStream(uri)`를 사용하여 `filesDir/images` 경로로 스트림 복사 기능 구현.
    - [ ] 비동기 처리(`suspend fun`) 및 예외 처리(파일 입출력 에러).
    - [ ] `EntryRepository`의 `upsert` 로직 수정:
        - 전달받은 `Photo` 객체들의 `uri`가 `content://` 스킴인 경우, 내부 저장소로 복사 후 `file://` 경로(또는 상대 경로)로 업데이트하여 DB에 저장.

### Step 2: Editor 화면 (UI & Logic)
- **목표**: 사용자 입력을 받아 저장하는 화면 구현.
- **작업 내용**:
    - [ ] `EditorViewModel` 보완: UI State에 `List<Uri>` 추가 (미리보기용).
    - [ ] `EditorScreen` UI 구성:
        - 날짜 선택기 (DatePickerDialog).
        - 기분 선택 (Row + Icon 5개).
        - 메모 입력 (OutlinedTextField).
        - 사진 추가 버튼: `ActivityResultContracts.PickVisualMedia` (Photo Picker) 연동.
        - 사진 미리보기: `LazyRow` + `AsyncImage` (Coil 라이브러리).
    - [ ] '저장' 버튼 클릭 시 ViewModel의 `save()` 호출.

### Step 3: Timeline 화면 (UI & Logic)
- **목표**: 저장된 기록 목록 조회.
- **작업 내용**:
    - [ ] `TimelineItem` Composable 구현:
        - 날짜, 기분 아이콘, 메모(1줄 요약), 대표 썸네일 표시.
    - [ ] `TimelineScreen` 구현:
        - `TimelineViewModel`의 `state` 구독.
        - `LazyColumn`으로 리스트 렌더링.
        - 데이터 없을 시 Empty State 표시 ("첫 기록을 남겨보세요").

### Step 4: 통합 및 테스트 (Integration)
- **목표**: 전체 플로우 연결 및 검증.
- **작업 내용**:
    - [ ] `NavGraph`: Home(Timeline) -> Editor 이동 경로 연결.
    - [ ] Hilt 모듈 점검: `ImageStorageManager` 주입 설정.
    - [ ] **수동 테스트**:
        1. 사진 2장 포함하여 기록 저장.
        2. 갤러리에서 원본 삭제.
        3. 앱 타임라인 및 상세에서 사진 정상 표시 확인.

## 검증 계획 (Verification Plan)

### 자동화 테스트 (Automated Tests)
- **Unit Test**: `ImageStorageManager`가 파일을 정상적으로 생성하는지 테스트 (Robolectric 또는 Instrumented Test 필요).
- **Unit Test**: `EditorViewModel`이 Repository 호출 시 올바른 데이터 모델로 변환하는지 검증.

### 수동 체크리스트 (Manual Checklist)
- [ ] 앱 실행 시 권한 팝업이 뜨지 않는가? (Photo Picker 사용 확인)
- [ ] 이미지가 앱 데이터 삭제 전까지 유지되는가?
- [ ] 가로 모드 회전 시 입력 데이터가 유지되는가? (ViewModel 상태 저장)
