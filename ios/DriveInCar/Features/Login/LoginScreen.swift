import SwiftUI

@MainActor
@Observable
final class LoginModel {
    let env: AppEnvironment

    var email = ""
    var password = ""
    var sheetVisible = false
    var isSubmitting = false
    var error: String?

    init(env: AppEnvironment) { self.env = env }

    func openSheet() { sheetVisible = true; error = nil }

    func signIn() async -> Bool? {
        await submit { try await self.env.auth.signIn(email: self.email.trimmingCharacters(in: .whitespaces), password: self.password) }
    }

    func signUp() async -> Bool? {
        await submit { try await self.env.auth.signUp(email: self.email.trimmingCharacters(in: .whitespaces), password: self.password) }
    }

    /// Returns: nil on failure; true if user needs profile setup; false if user already has profile.
    private func submit(_ block: @escaping () async throws -> String) async -> Bool? {
        guard !email.isEmpty && password.count >= 6 else {
            error = "이메일과 6자 이상 비밀번호를 입력해주세요."
            return nil
        }
        guard !isSubmitting else { return nil }
        isSubmitting = true
        error = nil
        defer { isSubmitting = false }

        do {
            let uid = try await block()
            let user = try? await env.users.fetchUser(uid: uid)
            sheetVisible = false
            return user == nil
        } catch {
            self.error = error.localizedDescription
            return nil
        }
    }
}

struct LoginScreen: View {
    @Environment(AppEnvironment.self) private var env
    let onSignedIn: (_ needsProfile: Bool) -> Void

    @State private var model: LoginModel?

    var body: some View {
        Group {
            if let model { content(model: model) }
            else { Color.clear.onAppear { model = LoginModel(env: env) } }
        }
    }

    @ViewBuilder
    private func content(model: LoginModel) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Spacer()
            Text("Drive in Car")
                .font(.system(size: 48, weight: .bold))
                .foregroundStyle(Brand.primary)
            Text("와인딩의 모든 순간을 기록하세요")
                .foregroundStyle(.secondary)

            Spacer().frame(height: 24)

            socialButton(label: "Google로 시작", bg: Color.white, fg: Color(red: 0.12, green: 0.16, blue: 0.22)) {
                model.openSheet()
            }
            socialButton(label: "Naver로 시작", bg: Color(red: 0x03/255, green: 0xC7/255, blue: 0x5A/255), fg: .white) {
                model.openSheet()
            }
            socialButton(label: "Kakao로 시작", bg: Color(red: 0xFE/255, green: 0xE5/255, blue: 0x00/255), fg: .black) {
                model.openSheet()
            }

            Button("이메일로 로그인 / 회원가입") { model.openSheet() }
                .frame(maxWidth: .infinity)
                .padding(.top, 16)

            Spacer()
        }
        .padding(.horizontal, 24)
        .sheet(isPresented: Binding(get: { model.sheetVisible }, set: { model.sheetVisible = $0 })) {
            EmailPasswordSheet(model: model, onSignedIn: onSignedIn)
                .presentationDetents([.medium])
        }
    }

    @ViewBuilder
    private func socialButton(label: String, bg: Color, fg: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            ZStack {
                Text(label).font(.headline).foregroundStyle(fg)
                HStack {
                    Spacer()
                    Text("임시")
                        .font(.caption2.weight(.semibold))
                        .padding(.horizontal, 6).padding(.vertical, 2)
                        .background(Color.black.opacity(0.2))
                        .clipShape(Capsule())
                        .foregroundStyle(fg)
                }
            }
            .frame(maxWidth: .infinity, minHeight: 52)
            .background(bg)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }
}

private struct EmailPasswordSheet: View {
    @Bindable var model: LoginModel
    let onSignedIn: (_ needsProfile: Bool) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("이메일로 시작").font(.title2.bold())

            TextField("이메일", text: $model.email)
                .keyboardType(.emailAddress)
                .textFieldStyle(.roundedBorder)
                .autocapitalization(.none)
            SecureField("비밀번호 (6자 이상)", text: $model.password)
                .textFieldStyle(.roundedBorder)

            if let err = model.error {
                Text(err).foregroundStyle(.red).font(.footnote)
            }

            HStack(spacing: 12) {
                Button {
                    Task {
                        if let needsProfile = await model.signIn() {
                            onSignedIn(needsProfile)
                        }
                    }
                } label: {
                    Text("로그인").frame(maxWidth: .infinity, minHeight: 48)
                }
                .buttonStyle(.borderedProminent)
                .disabled(model.isSubmitting)

                Button {
                    Task {
                        if let needsProfile = await model.signUp() {
                            onSignedIn(needsProfile)
                        }
                    }
                } label: {
                    Text("회원가입").frame(maxWidth: .infinity, minHeight: 48)
                }
                .buttonStyle(.bordered)
                .tint(Brand.secondary)
                .disabled(model.isSubmitting)
            }
            .padding(.top, 8)
        }
        .padding(24)
    }
}
