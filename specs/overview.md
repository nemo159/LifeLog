# LifeLog 프로젝트 개요

**프로젝트 명**: LifeLog  
**상태**: MVP (최소 기능 제품)  
**작성일**: 2025-12-27

## 프로젝트 비전
LifeLog는 기분, 메모, 사진을 통해 일상을 기록하는 개인용 저널링 애플리케이션입니다. 
사용자의 프라이버시를 최우선으로 하며, 모든 데이터는 서버가 아닌 **기기 내부에만 저장**됩니다.

<!-- 
  [비전 설명]
  - 이 앱은 "내 데이터는 내 것"이라는 철학을 가집니다.
  - 클라우드 동기화가 없으므로 네트워크 연결 없이도 완벽하게 동작해야 합니다.
  - 추후 AI 기능이 추가되더라도, 서버 전송 없이 온디바이스(On-Device) 모델을 사용하는 것을 목표로 합니다.
-->

## 핵심 기능 (MVP)

1. **타임라인 뷰 (Timeline View)**
   - 날짜순으로 정렬된 기록 목록을 보여줍니다.
   - 각 항목은 기분(이모지/아이콘)과 메모의 일부를 미리 보여줍니다.
   <!--
     [구현 상세]
     - RecyclerView가 아닌 Jetpack Compose의 LazyColumn을 사용합니다.
     - Paging 라이브러리는 MVP에서 오버엔지니어링일 수 있으므로, 
       Room의 Flow를 직접 관찰(collectAsState)하여 UI를 갱신하는 방식을 채택했습니다.
   -->

2. **기록 작성기 (Log Editor)**
   - 날짜 선택 (오늘 날짜 기본).
   - 1~5점 척도의 기분 선택.
   - 텍스트 메모 작성.
   - 사진 첨부 (Android Photo Picker 사용).
   <!--
     [권한 및 보안]
     - Android 14(API 34) 이상 및 Target SDK 36 환경을 고려하여,
       `READ_MEDIA_IMAGES` 권한을 요청하는 대신 **Photo Picker**를 사용합니다.
     - 사용자가 직접 선택한 사진에 대해서만 앱이 접근 권한을 가지므로 개인정보 보호에 유리합니다.
   -->

3. **로컬 저장소 (Local Storage)**
   - Room Database를 사용하여 텍스트 및 메타데이터를 안전하게 저장합니다.
   <!--
     [데이터 구조]
     - Entry(기록)와 Photo(사진)는 1:N 관계입니다.
     - 사진 파일 자체는 내부 저장소(Internal Storage)에 복사하여 관리하고, DB에는 경로만 저장합니다.
   -->

4. **백업 기초 (Backup Foundation)**
   - 데이터 유실 방지를 위한 백그라운드 작업 인프라(WorkManager)를 포함합니다.
   <!--
     [확장성]
     - MVP에서는 실제 백업 로직이 비어있지만(Stub), 
       추후 JSON/CSV 내보내기나 DB 파일 백업 기능을 넣을 수 있도록 구조만 잡아두었습니다.
   -->

## 기술 스펙 (Technical Specification)

### 기술 스택 (Tech Stack)
- **플랫폼**: Android (Target SDK 36+)
- **언어**: Kotlin
- **UI 프레임워크**: Jetpack Compose
  <!-- XML 레이아웃 없이 100% Compose로 UI를 구성하여 생산성과 재사용성을 높였습니다. -->
- **의존성 주입 (DI)**: Hilt
  <!-- 안드로이드 생명주기에 특화된 의존성 관리를 위해 채택했습니다. -->
- **데이터베이스**: Room
  <!-- SQLite에 대한 추상화 레이어를 제공하며, Flow와의 연동성이 뛰어납니다. -->
- **비동기 처리**: Coroutines & Flow
- **백그라운드 작업**: WorkManager
  <!-- 앱이 종료된 상태에서도 신뢰성 있는 작업을 보장합니다. -->

### 아키텍처 (Architecture)
이 프로젝트는 **Clean Architecture** 원칙을 따르며, 관심사를 명확히 분리합니다.

- **Core (핵심 도메인)**
  - 프레임워크에 의존하지 않는 순수 데이터 모델 (`Entry`, `Photo`)을 포함합니다.
  <!-- 예: `core/model` 패키지. JSON 파싱이나 DB 어노테이션이 없는 순수 Kotlin 객체(POJO)입니다. -->
  
- **Data (데이터 계층)**
  - 데이터 소스(Room DB), Entity, DAO, Repository 구현체를 관리합니다.
  - Entity(DB 모델)와 Domain Model 간의 변환(Mapper)을 담당합니다.
  <!-- 
    이 계층은 UI가 데이터를 어떻게 보여줄지 전혀 신경 쓰지 않습니다. 
    오직 데이터의 저장, 조회, 변환에만 집중합니다.
  -->

- **Feature (기능 계층)**
  - 화면 단위로 구성된 UI와 ViewModel을 포함합니다 (예: `feature/timeline`, `feature/editor`).
  <!-- 
    ViewModel은 Repository에서 데이터를 가져와 UI State로 변환(매핑)하여 Compose View에 전달합니다.
  -->

- **Navigation (네비게이션)**
  - 화면 간 이동 경로와 인자를 관리합니다.
  <!-- Compose Navigation을 사용하여 단일 Activity 내에서 화면 전환을 처리합니다. -->

### 주요 엔티티 (Key Entities)
- **Entry (기록)**: 하나의 일기/로그를 의미합니다.
    - 속성: `id`, `dateEpochDay`(날짜), `mood`(기분), `note`(내용), `createdAt`, `updatedAt`, `aiSummary`(AI요약), `moodScore`(감정점수).
- **Photo (사진)**: 기록에 첨부된 이미지입니다.
    - 속성: `id`, `entryId`(FK), `uri`(원본경로), `thumbPath`(썸네일경로).

## 로드맵 (Roadmap)
- **V1 (현재)**: 기본적인 기록(텍스트 + 기분 + 사진) 및 타임라인 조회.
- **V2**: 
    - 백업 및 복구 기능 구현.
    - AI 기반 노트 요약 및 감정 분석(온디바이스).
- **V3**: 
    - 고급 시각화 (기분 변화 차트 등).
    - 태그 지원 및 검색 기능 강화.