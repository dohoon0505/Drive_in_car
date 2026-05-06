import SwiftUI

enum RootDestination: Equatable {
    case splash
    case login
    case profileSetup
    case map
}

@MainActor
@Observable
final class RootModel {
    var destination: RootDestination = .splash

    private let env: AppEnvironment

    init(env: AppEnvironment) {
        self.env = env
    }

    func decideStartDestination() async {
        if let uid = env.auth.currentUid {
            let user = try? await env.users.fetchUser(uid: uid)
            destination = (user == nil) ? .profileSetup : .map
        } else {
            destination = .login
        }
    }
}

struct RootView: View {
    @Environment(AppEnvironment.self) private var env
    @State private var model: RootModel?

    var body: some View {
        Group {
            if let model {
                content(model: model)
                    .task { await model.decideStartDestination() }
            } else {
                SplashScreen()
                    .onAppear { model = RootModel(env: env) }
            }
        }
    }

    @ViewBuilder
    private func content(model: RootModel) -> some View {
        switch model.destination {
        case .splash:
            SplashScreen()
        case .login:
            LoginScreen(onSignedIn: { needsProfile in
                model.destination = needsProfile ? .profileSetup : .map
            })
            .environment(env)
        case .profileSetup:
            ProfileSetupScreen(onCompleted: { model.destination = .map })
                .environment(env)
        case .map:
            MapNavigation(onSignedOut: { model.destination = .login })
                .environment(env)
        }
    }
}
