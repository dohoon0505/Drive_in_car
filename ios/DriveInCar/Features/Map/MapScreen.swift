import SwiftUI

@MainActor
@Observable
final class MapModel {
    let env: AppEnvironment

    var courses: [Course] = []
    var me: User?

    private var coursesTask: Task<Void, Never>?
    private var meTask: Task<Void, Never>?

    init(env: AppEnvironment) { self.env = env }

    func start() {
        coursesTask?.cancel()
        coursesTask = Task { [env] in
            for await list in env.courses.observeActive() {
                if Task.isCancelled { return }
                self.courses = list
            }
        }
        meTask?.cancel()
        if let uid = env.auth.currentUid {
            meTask = Task { [env] in
                for await u in env.users.observeUser(uid: uid) {
                    if Task.isCancelled { return }
                    self.me = u
                }
            }
        }
    }

    func stop() {
        coursesTask?.cancel()
        meTask?.cancel()
    }
}

struct MapScreen: View {
    @Environment(AppEnvironment.self) private var env
    let onCourseSelected: (String) -> Void
    let onSignedOut: () -> Void

    @State private var model: MapModel?
    @State private var menuOpen = false

    var body: some View {
        Group {
            if let model { content(model: model) }
            else {
                Color.clear.onAppear {
                    let m = MapModel(env: env)
                    m.start()
                    model = m
                }
            }
        }
    }

    @ViewBuilder
    private func content(model: MapModel) -> some View {
        ZStack(alignment: .topLeading) {
            GoogleMapView(courses: model.courses) { c in
                onCourseSelected(c.courseId)
            }
            .ignoresSafeArea()

            profileChip(model: model)
                .padding(16)
        }
        .onDisappear { model.stop() }
    }

    @ViewBuilder
    private func profileChip(model: MapModel) -> some View {
        HStack(spacing: 8) {
            AvatarBadge(avatarId: model.me?.profileImageId ?? "avatar_01", size: 40)
            if let nick = model.me?.nickname, !nick.isEmpty {
                Text(nick).font(.subheadline.weight(.medium)).padding(.trailing, 8)
            }
        }
        .padding(8)
        .background(.thinMaterial)
        .clipShape(Capsule())
        .shadow(color: .black.opacity(0.1), radius: 4, y: 2)
        .onLongPressGesture {
            menuOpen = true
        }
        .confirmationDialog("계정", isPresented: $menuOpen) {
            Button("로그아웃", role: .destructive) { onSignedOut() }
            Button("취소", role: .cancel) {}
        }
    }
}
