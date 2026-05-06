pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Naver Maps SDK 는 별도 Maven 저장소를 사용 (Maven Central 에 미배포).
        maven { url = uri("https://repository.map.naver.com/archive/maven") }
    }
}

rootProject.name = "DriveInCar"
include(":app")
