# 구현 계획: MVP 핵심 기능 (Core Features)

**연관 명세**: `specs/001-mvp-core.md`  
**작성일**: 2026-01-10 (Updated)  
**상태**: In Progress

## 목표 (Objective)
LifeLog 앱의 핵심 가치인 **기록(작성)**과 **회고(조회)** 기능을 구현합니다.  
Android 최신 정책(Target SDK 36)을 준수하며, 기본적인 CRUD(Create, Read, Update, Delete)를 완성합니다.

## 구현 범위 (Scope) - Phase 1
1. **사진 업로드**: Photo Picker 연동, 내부 저장소(`filesDir/images`)로 복사.
2. **삭제 기능**: DB 데이터 및 물리 이미지 파일 삭제.
3. **상세 화면**: 기록 전체 내용 조회 및 네비게이션 연결.
4. **정렬 기능**: 타임라인 날짜순(최신/과거) 정렬 토글.

## 단계별 계획 (Step-by-Step Plan)

### Step 1: 이미지 처리 및 데이터 계층 (Infrastructure)
- [x] `ImageStorageManager`: URI -> 내부 파일 복사, 파일 삭제 구현.
- [x] `EntryRepository`: `observeEntry`(단건 조회), `delete`(삭제) 추가.
- [x] `PhotoDao`, `EntryDao`: 관련 쿼리 추가.

### Step 2: Editor 화면 고도화 (Editor)
- [x] `EditorViewModel`: `ImageStorageManager` 주입, 저장 시 이미지 복사 로직 추가.
- [x] `EditorScreen`: `PickMultipleVisualMedia` (Photo Picker) 연동, `LazyRow`로 미리보기 구현.

### Step 3: 상세 화면 구현 (Detail)
- [x] `DetailViewModel`: `savedStateHandle`로 ID 수신, 데이터 로드, 삭제 로직 구현.
- [x] `DetailScreen`: 스크롤 가능한 UI, 이미지 크게 보기, 삭제 다이얼로그.
- [x] `NavGraph`: 타임라인 -> 상세 이동 경로(`detail/{id}`) 추가.

### Step 4: 타임라인 화면 개선 (Timeline)
- [ ] **정렬 기능**:
    - ViewModel에 `SortOrder` 상태 추가 (DESC, ASC).
    - DAO 쿼리 분리 또는 `Flow` 조합으로 정렬 변경 지원.
    - UI 상단 바(TopAppBar)에 정렬 액션 버튼 추가.
- [ ] **아이템 클릭**: 상세 화면으로 이동 이벤트 연결.
- [ ] **UI 폴리싱**: 날짜 포맷, 이미지 썸네일(Coil) 최적화.

### Step 5: 통합 테스트 (Verification)
- [ ] 전체 CRUD 플로우 수동 테스트.
- [ ] 이미지 삭제 확인 (앱 데이터 폴더 확인).