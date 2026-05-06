import SwiftUI

@MainActor
@Observable
final class RaceModel {
    let env: AppEnvironment
    let courseId: String

    var state: RaceState = .idle
    var course: Course?

    private var machine: RaceStateMachine?
    private var trackingTask: Task<Void, Never>?
    private var didEmit = false

    init(env: AppEnvironment, courseId: String) {
        self.env = env
        self.courseId = courseId
    }

    func start(onFinished: @escaping (Int64, Double, Bool, Bool) -> Void,
               onCancel: @escaping (CancelReason) -> Void) async {
        env.location.requestPermission()
        guard let c = try? await env.courses.fetchCourse(id: courseId) else {
            onCancel(.permissionLost); return
        }
        course = c
        let m = RaceStateMachine(course: c)
        machine = m

        trackingTask?.cancel()
        trackingTask = Task { [env] in
            for await sample in env.location.samples(intervalMs: 1_000) {
                if Task.isCancelled { return }
                let new = m.onSample(sample)
                self.state = new
                switch new {
                case let .finished(time, avg, flagged):
                    if !self.didEmit {
                        self.didEmit = true
                        await self.handleFinish(time: time, avg: avg, flagged: flagged, onFinished: onFinished)
                    }
                case let .cancelled(reason):
                    if !self.didEmit {
                        self.didEmit = true
                        onCancel(reason)
                    }
                default:
                    break
                }
            }
        }
    }

    func userCancel(onCancel: @escaping (CancelReason) -> Void) {
        _ = machine?.cancel(reason: .userCancelled)
        if !didEmit { didEmit = true; onCancel(.userCancelled) }
        trackingTask?.cancel()
    }

    private func handleFinish(time: Int64, avg: Double, flagged: Bool,
                              onFinished: @escaping (Int64, Double, Bool, Bool) -> Void) async {
        trackingTask?.cancel()
        guard let uid = env.auth.currentUid else {
            onFinished(time, avg, flagged, false); return
        }
        let submit = SubmitTimeUseCase(users: env.users, rankings: env.rankings)
        _ = try? await submit(
            uid: uid,
            courseId: courseId,
            timeMs: time,
            averageKmh: avg,
            flagged: flagged
        )
        // MVP: PR 비교는 추후. 지금은 false 고정.
        onFinished(time, avg, flagged, false)
    }
}

struct RaceScreen: View {
    @Environment(AppEnvironment.self) private var env
    let courseId: String
    let onFinished: (_ timeMs: Int64, _ averageKmh: Double, _ flagged: Bool, _ pb: Bool) -> Void
    let onCancel: () -> Void

    @State private var model: RaceModel?

    var body: some View {
        Group {
            if let model { content(model: model) }
            else {
                Color.clear.task {
                    let m = RaceModel(env: env, courseId: courseId)
                    await m.start(
                        onFinished: onFinished,
                        onCancel: { _ in onCancel() }
                    )
                    model = m
                }
            }
        }
        .navigationBarBackButtonHidden(true)
    }

    @ViewBuilder
    private func content(model: RaceModel) -> some View {
        VStack {
            switch model.state {
            case .idle, .arming:
                arming(distance: distanceForArming(state: model.state))
            case .armed:
                armed
            case let .inRace(elapsed, distEnd, kmh):
                inRace(elapsed: elapsed, distEnd: distEnd, kmh: kmh)
            case .finished, .cancelled:
                Text("처리 중…").foregroundStyle(.secondary)
            }
            Spacer()
            cancelButton(model: model)
        }
        .padding(24)
    }

    private func distanceForArming(state: RaceState) -> Double {
        if case let .arming(d) = state { return d }
        return 0
    }

    @ViewBuilder
    private func arming(distance: Double) -> some View {
        VStack(spacing: 16) {
            Spacer()
            Text("출발 지점으로").font(.title.bold())
            Text("\(Int(distance)) m")
                .font(.system(size: 64, weight: .bold).monospacedDigit())
        }
    }

    @ViewBuilder
    private var armed: some View {
        VStack(spacing: 16) {
            Spacer()
            Text("출발 준비 완료").font(.title.bold())
            Text("출발선을 통과하면 자동으로 시작됩니다").foregroundStyle(.secondary)
        }
    }

    @ViewBuilder
    private func inRace(elapsed: Int64, distEnd: Double, kmh: Double) -> some View {
        VStack(spacing: 16) {
            Spacer()
            Text(TimeFormat.raceTime(ms: elapsed))
                .font(.system(size: 96, weight: .bold).monospacedDigit())
            HStack {
                stat(label: "남은 거리", value: "\(Int(distEnd)) m")
                Spacer()
                stat(label: "현재 속도", value: "\(Int(kmh)) km/h")
            }
            .padding(.horizontal, 32)
        }
    }

    @ViewBuilder
    private func stat(label: String, value: String) -> some View {
        VStack {
            Text(label).foregroundStyle(.secondary).font(.caption)
            Text(value).font(.title3.bold())
        }
    }

    @ViewBuilder
    private func cancelButton(model: RaceModel) -> some View {
        Button(role: .destructive) {
            model.userCancel(onCancel: { _ in onCancel() })
        } label: {
            Text("중단").frame(maxWidth: .infinity, minHeight: 52)
        }
        .buttonStyle(.borderedProminent)
        .tint(.red)
    }
}
