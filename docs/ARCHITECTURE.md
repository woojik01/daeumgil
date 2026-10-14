# 아키텍처

## PRD-01 구조

초기에는 단일 `app` 모듈 안에 책임별 패키지를 구성합니다. 비어 있는 모듈과 아직 사용하지 않는 라이브러리는 추가하지 않습니다.

| 패키지 | 책임 |
| --- | --- |
| `core/ui` | 테마, 공통 스크롤 레이아웃, 빈 화면 |
| `domain/settings` | 설정 모델과 저장소 계약; Android API와 분리 |
| `data/settings` | Preferences DataStore 구현 |
| `feature/home` | 현재 상태·목적지 진입, 최근·즐겨찾기 빈 상태 |
| `feature/settings` | ViewModel, 읽기/쓰기 오류 상태, 테마 선택 UI |
| `navigation` | 8개 목적지와 NavHost, 돌아가기, 공통 Scaffold |
| 앱 루트 | Application 범위 의존성 구성, Activity 진입 |

앱 범위 DataStore 인스턴스는 하나만 생성합니다. ViewModel은 저장소 계약만 의존합니다. Compose는 lifecycle을 고려하여 StateFlow를 관찰하고, 저장 성공 후 실제 저장소 값으로 테마를 갱신합니다. 저장 실패 시 이전 테마를 유지합니다. 읽기 실패 시 오류와 재시도를 표시하고 설정 덮어쓰기를 차단합니다. 알 수 없는 저장값은 시스템 모드로 해석합니다.

수동 의존성 주입은 `AppContainer`에서 수행합니다. Hilt는 기능과 의존성 규모가 커질 때 도입할 수 있습니다. 아직 교통·장소·위치·안내 세션을 저장하거나 조회하지 않습니다.

Scaffold는 안전 영역을 사용하고 긴 본문은 스크롤합니다. 내용 최대 폭은 640dp입니다. 테마 변경은 시스템 바 아이콘 밝기에도 반영합니다. 모든 사용자 텍스트는 문자열 리소스에 둡니다.

## 후속 확장

PRD-02에서 현재 상태 모델과 검증 유스케이스, PRD-03에서 장소 공급자 계약과 최근·즐겨찾기 저장소를 추가합니다. PRD-04에서 실제 공급자 어댑터와 식별자·운행 패턴을 검증합니다. PRD-05 이전까지 아직 검증되지 않은 상태로 경로 안내를 활성화하지 않습니다.

데이터 규모가 커지면 Room을 도입하고 설정과 이동 데이터 저장 책임을 분리합니다. 다중 모듈 전환은 기능이 실제로 존재하고 빌드 경계가 필요할 때 진행합니다. 최종 목표 구조는 `core/{model,common,database,network,ui}`, `data/{transit,places,navigation}`, `domain/{routing,navigation}`, `feature/{home,currenttransit,destination,routes,navigation,settings}`입니다.

현재 탑승 상태의 providerRouteId, 방향, servicePatternId, 정차 순번, 사용자 확인 시각을 함께 관리합니다. 외부 경로 API의 일반 좌표 검색을 탑승 차량 지정 검색으로 간주하지 않습니다.

## 빌드 결정과 공식 출처

2026-10-14 확인. 라이브러리 버전은 버전 카탈로그에 고정합니다. Compose BOM은 공식 사용 예시에 확인된 안정 BOM 2026.09.00을 사용하며 알파·베타를 도입하지 않습니다.

- [AGP 9.4 공식 릴리스·호환성](https://developer.android.com/build/releases/agp-9-4-0-release-notes): 9.4.1을 사용합니다. 빌드 환경은 Gradle 9.8.1 / JDK 17 / SDK 37.0입니다.
- [Built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin): 별도 kotlin-android 플러그인을 적용하지 않습니다.
- [KGP 버전 조정](https://developer.android.com/build/releases/agp-9-0-0-release-notes): Kotlin과 Compose compiler를 2.4.21로 정렬합니다.
- [Compose BOM](https://developer.android.com/develop/ui/compose/bom)
- [Activity](https://developer.android.com/jetpack/androidx/releases/activity)
- [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)
- [Navigation](https://developer.android.com/jetpack/androidx/releases/navigation)
- [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore)
- [Gradle 9.8.1](https://github.com/gradle/gradle/releases/tag/v9.8.1)

Debug 패키지는 `.dev`로 분리합니다. CI에서는 서명 없는 release 배포를 하지 않고 개발 APK만 만듭니다. API 키·서명키·계정·위치 권한은 PRD-01에 필요하지 않습니다.
