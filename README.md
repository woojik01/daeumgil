# 다음길 (Daeumgil)

지금 가는 길에서, 목적지까지.

Android 우선 대중교통 안내 앱입니다. 버튼과 목록으로 현재 이동 상태를 선택하고, 해당 상태를 반영한 다음 행동을 안내합니다.

## 개발 흐름

GitHub Issue → 기능 브랜치 → Pull Request → 빌드·테스트 → 사용자 승인 → 병합.

자동 병합을 사용하지 않습니다. 각 PRD는 독립 PR로 검증하고 사용자 승인 후 병합합니다.

## 현재 구현 범위

PRD-01: Compose 홈, 8개 화면의 탐색 구조, 라이트·다크·시스템 테마, DataStore 설정 저장, ViewModel/Flow, CI와 자동 테스트.

현재 이동 상태·목적지·즐겨찾기는 준비 또는 빈 화면입니다. 길 찾기는 비활성화되어 있습니다. 실제 교통 데이터와 경로 안내는 후속 단계에서 구현합니다. 현재 버전은 이동 안내용으로 사용할 수 없습니다.

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
