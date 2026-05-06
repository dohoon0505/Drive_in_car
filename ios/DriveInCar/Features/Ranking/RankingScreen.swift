import SwiftUI

@MainActor
@Observable
final class RankingModel {
    let env: AppEnvironment
    let courseId: String
    var course: Course?
    var rankings: [Ranking] = []

    private var task: Task<Void, Never>?

    init(env: AppEnvironment, courseId: String) {
        self.env = env
        self.courseId = courseId
    }

    func start() async {
        course = try? await env.courses.fetchCourse(id: courseId)
        task?.cancel()
        task = Task { [env, courseId] in
            for await list in env.rankings.observeLeaderboard(courseId: courseId, limit: 100) {
                if Task.isCancelled { return }
                self.rankings = list
            }
        }
    }

    func stop() { task?.cancel() }
}

struct RankingScreen: View {
    @Environment(AppEnvironment.self) private var env
    let courseId: String

    @State private var model: RankingModel?

    var body: some View {
        Group {
            if let model { content(model: model) }
            else {
                Color.clear.task {
                    let m = RankingModel(env: env, courseId: courseId)
                    await m.start()
                    model = m
                }
            }
        }
        .navigationTitle("랭킹")
    }

    @ViewBuilder
    private func content(model: RankingModel) -> some View {
        if model.rankings.isEmpty {
            VStack {
                Spacer()
                Text("아직 기록이 없습니다").foregroundStyle(.secondary)
                Spacer()
            }
        } else {
            List(Array(model.rankings.enumerated()), id: \.element.id) { idx, r in
                HStack(spacing: 12) {
                    Text("#\(idx + 1)").font(.title3.bold())
                    AvatarBadge(avatarId: r.profileImageId, size: 36)
                    VStack(alignment: .leading) {
                        Text(r.nickname)
                        Text(r.carDisplay).font(.caption).foregroundStyle(.secondary)
                    }
                    Spacer()
                    Text(TimeFormat.raceTime(ms: r.timeMs))
                        .font(.title3.bold().monospacedDigit())
                }
                .padding(.vertical, 4)
            }
            .listStyle(.plain)
        }
    }
}
