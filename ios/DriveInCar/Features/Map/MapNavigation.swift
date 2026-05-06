import SwiftUI

enum MapPath: Hashable {
    case courseDetail(courseId: String)
    case race(courseId: String)
    case ranking(courseId: String)
    case result(courseId: String, timeMs: Int64, averageKmh: Double, flagged: Bool, personalBest: Bool)
}

struct MapNavigation: View {
    @Environment(AppEnvironment.self) private var env
    let onSignedOut: () -> Void

    @State private var path: [MapPath] = []

    var body: some View {
        NavigationStack(path: $path) {
            MapScreen(
                onCourseSelected: { path.append(.courseDetail(courseId: $0)) },
                onSignedOut: {
                    try? env.auth.signOut()
                    onSignedOut()
                }
            )
            .environment(env)
            .navigationDestination(for: MapPath.self) { dest in
                switch dest {
                case let .courseDetail(courseId):
                    CourseDetailScreen(
                        courseId: courseId,
                        onJoinRace: { path.append(.race(courseId: courseId)) },
                        onViewRanking: { path.append(.ranking(courseId: courseId)) }
                    ).environment(env)

                case let .race(courseId):
                    RaceScreen(
                        courseId: courseId,
                        onFinished: { time, avg, flagged, pb in
                            path = [.result(
                                courseId: courseId,
                                timeMs: time,
                                averageKmh: avg,
                                flagged: flagged,
                                personalBest: pb
                            )]
                        },
                        onCancel: { path.removeLast() }
                    ).environment(env)

                case let .ranking(courseId):
                    RankingScreen(courseId: courseId).environment(env)

                case let .result(courseId, time, avg, flagged, pb):
                    ResultScreen(
                        courseId: courseId,
                        timeMs: time,
                        averageKmh: avg,
                        flagged: flagged,
                        personalBest: pb,
                        onViewRanking: { path.append(.ranking(courseId: courseId)) },
                        onRetry: { path = [.race(courseId: courseId)] },
                        onBackToMap: { path = [] }
                    )
                }
            }
        }
    }
}
