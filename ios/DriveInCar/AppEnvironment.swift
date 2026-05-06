import SwiftUI

/// 경량 DI 컨테이너. 외부 DI 라이브러리 없이 Environment로 전달.
@Observable
final class AppEnvironment {
    let auth: AuthRepository
    let users: UserRepository
    let courses: CourseRepository
    let rankings: RankingRepository
    let location: LocationProvider

    init(
        auth: AuthRepository = FirebaseAuthRepository(),
        users: UserRepository = FirestoreUserRepository(),
        courses: CourseRepository = FirestoreCourseRepository(),
        rankings: RankingRepository = FirestoreRankingRepository(),
        location: LocationProvider = CoreLocationProvider()
    ) {
        self.auth = auth
        self.users = users
        self.courses = courses
        self.rankings = rankings
        self.location = location
    }
}
