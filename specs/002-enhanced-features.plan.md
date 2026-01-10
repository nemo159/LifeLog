# 구현 계획: 확장 기능 (Enhanced Features)

**연관 명세**: `specs/002-enhanced-features.md`  
**작성일**: 2026-01-10  
**상태**: Planned

## 목표 (Objective)
MVP 기능(Phase 1) 완료 후, 사용자 경험을 풍부하게 만드는 시각화, 편의성, 안정성 기능을 순차적으로 구현합니다.

## 단계별 계획 (Step-by-Step Plan)

### Phase 2: 시각화 및 UX 개선 (Visualization & UX)
**목표**: 데이터를 보기 좋게 정리하고 사용자가 앱을 꾸밀 수 있게 합니다.

1. **타임라인 그룹화 (Grouping)**
   - `TimelineViewModel`: 데이터를 `Map<YearMonth, List<Entry>>` 형태로 변환하는 로직 추가.
   - `TimelineScreen`: `LazyColumn`의 `stickyHeader`를 사용하여 월별 헤더 구현.

2. **캘린더 뷰 (Calendar View)**
   - `CalendarScreen` 추가.
   - 라이브러리(예: `kizitonwose/CalendarView`) 활용 또는 커스텀 `LazyVerticalGrid`로 달력 구현.
   - 날짜 셀에 기분 아이콘 표시.

3. **테마 시스템 (Theming)**
   - `ThemeStore` (DataStore 기반) 생성하여 설정 저장.
   - `LifeLogTheme` 컴포저블 수정하여 동적 색상(Dynamic Color) 또는 고정 테마 적용.

### Phase 3: 콘텐츠 풍부화 (Content Enrichment)
**목표**: 기록의 밀도를 높이고 자동화된 정보를 제공합니다.

1. **태그 시스템 (Tags)**
   - DB: `tags` 테이블 추가 (M:N 관계) 또는 `Entry` 테이블에 JSON/String 리스트로 저장 (SQLite 한계 고려).
   - Editor: `FlowRow`를 이용한 태그 입력/선택 UI.

2. **위치 및 날씨 (Location & Weather)**
   - `LocationManager` 구현 (FusedLocationProviderClient).
   - Weather API 클라이언트(Retrofit) 구현.
   - Editor 진입 시 권한 확인 후 자동 태깅.

3. **통계 대시보드 (Statistics)**
   - `StatsViewModel`: 기간별 기분 평균, 태그 빈도수 계산 로직.
   - `StatsScreen`: 막대 그래프, 원형 차트(Canvas 활용)로 시각화.

### Phase 4: 데이터 안정성 (Backup)
**목표**: 데이터 분실 방지.

1. **Google Drive 백업**
   - Google Sign-In 연동.
   - Drive API를 통해 DB 파일(`lifelog.db`)과 `files/images` 폴더를 ZIP으로 압축하여 업로드.
   - 복원 시 ZIP 다운로드 -> 압축 해제 -> DB 교체 -> 앱 재시작 프로세스 구현.
