import SwiftUI

@MainActor
@Observable
final class CourseDetailModel {
    let env: AppEnvironment
    let courseId: String

    var course: Course?
    var top3: [Ranking] = []

    private var top3Task: Task<Void, Never>?

    init(env: AppEnvironment, courseId: String) {
        self.env = env
        self.courseId = courseId
    }

    func start() async {
        course = try? await env.courses.fetchCourse(id: courseId)
        top3Task?.cancel()
        top3Task = Task { [env, courseId] in
            for await list in env.rankings.observeTop3(courseId: courseId) {
                if Task.isCancelled { return }
                self.top3 = list
            }
        }
    }

    func stop() { top3Task?.cancel() }
}

struct CourseDetailScreen: View {
    @Environment(AppEnvironment.self) private var env
    let courseId: String
    let onJoinRace: () -> Void
    let onViewRanking: () -> Void

    @State private var model: CourseDetailModel?

    var body: some View {
        Group {
            if let model { content(model: model) }
            else {
                Color.clear.task {
                    let m = CourseDetailModel(env: env, courseId: courseId)
                    await m.start()
                    model = m
                }
            }
        }
    }

    @ViewBuilder
    private func content(model: CourseDetailModel) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let c = model.course {
                    Text(c.regionName).foregroundStyle(.secondary)
                    HStack(spacing: 16) {
                        Text("거리 \(String(format: "%.1f", c.distanceMeters / 1000)) km")
                        Text("난이도 \(String(repeating: "★", count: c.difficulty))")
                    }
                    .padding(.bottom, 4)

                    Text(c.description)
                        .padding(.bottom, 16)

                    Text("Top 3").font(.title3.bold())
                    if model.top3.isEmpty {
                        Text("아직 기록이 없습니다").foregroundStyle(.secondary)
                    } else {
                        ForEach(Array(model.top3.enumerated()), id: \.element.id) { idx, r in
                            rankingRow(rank: idx + 1, ranking: r)
                        }
                    }

                    Spacer().frame(height: 24)

                    HStack(spacing: 8) {
                        Button {
                            onJoinRace()
                        } label: {
                            Text("랭킹전 참여").frame(maxWidth: .infinity, minHeight: 52)
                        }
                        .buttonStyle(.borderedProminent)
                        Button {} label: {
                            Text("참여 안내").frame(maxWidth: .infinity, minHeight: 52)
                        }
                        .buttonStyle(.bordered)
                    }
                    Button {
                        onViewRanking()
                    } label: {
                        Text("전체 랭킹").frame(maxWidth: .infinity, minHeight: 48)
                    }
                    .buttonStyle(.bordered)
                } else {
                    Text("코스를 불러오는 중…")
                }
            }
            .padding(24)
        }
        .navigationTitle(model.course?.name ?? "코스")
        .onDisappear { model.stop() }
    }

    @ViewBuilder
    private func rankingRow(rank: Int, ranking r: Ranking) -> some View {
        HStack(spacing: 12) {
            Text("#\(rank)").font(.title3.bold())
            AvatarBadge(avatarId: r.profileImageId, size: 32)
            VStack(alignment: .leading) {
                Text(r.nickname)
                Text(r.carDisplay).font(.caption).foregroundStyle(.secondary)
            }
            Spacer()
            Text(TimeFormat.raceTime(ms: r.timeMs)).font(.title3.bold().monospacedDigit())
        }
        .padding(.vertical, 4)
    }
}
