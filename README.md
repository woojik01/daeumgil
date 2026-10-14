# 다음길 (Daeumgil)

지금 가는 길에서, 목적지까지.

Android 우선 대중교통 안내 앱입니다. 버튼과 목록으로 현재 이동 상태를 선택하고, 해당 상태를 반영한 다음 행동을 안내합니다.

## 개발 흐름

GitHub Issue → 기능 브랜치 → Pull Request → 빌드·테스트 → 사용자 승인 → 병합.

자동 병합을 사용하지 않습니다. 각 PRD는 독립 PR로 검증하고 사용자 승인 후 병합합니다.

## 현재 구현 범위

PRD-01: Compose 홈, 8개 화면의 탐색 구조, 라이트·다크·시스템 테마, DataStore 설정 저장, ViewModel/Flow, CI와 자동 테스트.

PRD-02 (`0.2.0-dev`): 교통수단 선택, 노선·정류장 검색 목록, 지역·방향·일반/급행·현재/직전 위치·탑승 확인, 입력 검증, 선택값 수정·저장과 복원, 홈의 저장 상태 요약.

현재 이동 상태 선택은 **명시적인 샘플 데이터 모드**입니다. 모든 역·노선·지역은 가상의 테스트 자료이며 실제 운행정보가 아닙니다. 목적지·즐겨찾기는 준비 또는 빈 화면이고, 길 찾기는 비활성화되어 있습니다. 실제 교통 데이터와 경로 안내는 후속 단계에서 구현합니다. 현재 버전은 이동 안내용으로 사용할 수 없습니다.

선택한 상태는 기기에 저장되며 홈에서 이전 선택과 확인 시각을 보여줍니다. 이전 상태를 현재 실시간 위치처럼 취급하지 않습니다. 저장 상태 수정 시 탑승 여부를 다시 확인합니다. 방향을 모르면 명시적으로 미확인 상태로 저장할 수 있지만 경로 안내에는 사용할 수 없습니다.

## 개발 환경과 빌드

- Android Studio: AGP 9.4을 지원하는 버전
- JDK 17
- Android SDK Platform 37.0 / Build Tools 36.0.0
- Gradle 9.8.1 (동봉한 Wrapper 사용)
- 최소 Android 8.0 (API 26)

프로젝트를 Android Studio에서 열고 SDK 경로를 설정합니다. 로컬 SDK 경로는 `local.properties`에만 저장합니다.

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Windows에서는 `gradlew.bat`를 사용합니다. 두 번째 명령은 실행 중인 Android 에뮬레이터 또는 연결된 기기가 필요합니다.

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. 개발 앱의 패키지는 `com.daeumgil.app.dev`로 정식 앱과 분리합니다. 정식 패키지 `com.daeumgil.app`은 배포 전 확정해야 합니다. 서명키는 포함하지 않습니다.

PR의 **Checks → Android CI → Artifacts → daeumgil-debug-apk**에서도 Debug APK를 받을 수 있습니다. CI는 빌드·단위 테스트·Lint 후 API 35 에뮬레이터에서 화면 탐색과 재생성 테스트를 실행합니다.

## 문서

- [제품 요구사항](docs/PRD.md)
- [아키텍처와 확장 계획](docs/ARCHITECTURE.md)
- [외부 API 검증 계획](docs/API_PROVIDERS.md)
- [실기기 확인 항목](docs/QA.md)
- [PRD-02 구현·검증 범위](docs/PRD-02.md)
