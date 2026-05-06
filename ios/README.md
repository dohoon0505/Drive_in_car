# iOS 프로젝트 셋업

이 폴더의 Swift 소스 파일들은 Xcode 프로젝트 파일(`.xcodeproj`) 없이 보관된다 (`.xcodeproj`는 자동 생성된 바이너리 메타가 많아 hand-roll에 부적합). 아래 단계대로 한 번 Xcode로 생성하여 이 소스들을 연결한다.

## 1. Xcode 프로젝트 생성

1. Xcode → File → New → Project → **iOS App**
2. 옵션:
   - Product Name: `DriveInCar`
   - Organization Identifier: `com.driveincar`
   - Bundle Identifier: `com.driveincar.app`
   - Interface: **SwiftUI**
   - Language: **Swift**
   - Storage: None
   - Include Tests: 체크
3. 저장 위치: `ios/` 폴더 (이 README와 같은 위치). 자동 생성된 `DriveInCar/` 폴더와 이 폴더의 기존 `DriveInCar/` 소스가 병합된다.
4. Xcode가 만든 기본 `ContentView.swift`와 `DriveInCarApp.swift`는 **삭제**하고 이 저장소의 같은 이름 파일을 사용한다.

## 2. SwiftPM 의존성 추가

Xcode → File → Add Packages...

| 패키지 | URL | 추가할 라이브러리 |
|---|---|---|
| firebase-ios-sdk | https://github.com/firebase/firebase-ios-sdk | FirebaseAuth, FirebaseFirestore, FirebaseFirestoreSwift, FirebaseStorage |
| GoogleMaps-iOS | https://github.com/googlemaps/ios-maps-sdk | GoogleMaps |

## 3. Info.plist 키 추가

타겟의 Info 탭에서 다음 키 추가:

| Key | Value |
|---|---|
| `NSLocationWhenInUseUsageDescription` | 와인딩 코스 진행 시간을 측정합니다 |
| `NSLocationAlwaysAndWhenInUseUsageDescription` | 백그라운드에서도 레이스 시간을 측정합니다 |
| `UIBackgroundModes` | `location` 항목 추가 |

## 4. Maps API 키

`Resources/Secrets.xcconfig` (gitignored) 작성:
```
GOOGLE_MAPS_API_KEY = YOUR_IOS_MAPS_KEY
```
Build Settings에서 이 xcconfig를 Configuration에 연결. `Info.plist`에 다음 항목 추가:
```
GMS_API_KEY = $(GOOGLE_MAPS_API_KEY)
```
앱 시작 시 `DriveInCarApp.swift`에서 `GMSServices.provideAPIKey(...)` 호출.

## 5. GoogleService-Info.plist

Firebase Console → 프로젝트 `drive-3c0fd` → iOS 앱 등록 (Bundle ID `com.driveincar.app`) → `GoogleService-Info.plist` 다운로드 → `Resources/`에 추가 (Xcode Target에 포함).

## 6. 빌드

```
cd ios
xed .
# Xcode → Product → Run
```
