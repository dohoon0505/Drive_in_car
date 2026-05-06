import SwiftUI

@MainActor
@Observable
final class ProfileSetupModel {
    let env: AppEnvironment

    var nickname = ""
    var carBrand = ""
    var carModel = ""
    var avatarId = "avatar_01"
    var isSubmitting = false
    var error: String?

    init(env: AppEnvironment) { self.env = env }

    func save() async -> Bool {
        guard let uid = env.auth.currentUid else {
            error = "로그인 세션이 만료되었습니다."
            return false
        }
        guard (2...16).contains(nickname.count) else {
            error = "닉네임은 2~16자입니다."
            return false
        }
        guard !carBrand.isEmpty, !carModel.isEmpty else {
            error = "차량 브랜드와 모델을 입력해주세요."
            return false
        }
        guard !isSubmitting else { return false }
        isSubmitting = true
        error = nil
        defer { isSubmitting = false }

        let user = User(
            uid: uid,
            nickname: nickname.trimmingCharacters(in: .whitespaces),
            carBrand: carBrand.trimmingCharacters(in: .whitespaces),
            carModel: carModel.trimmingCharacters(in: .whitespaces),
            profileImageId: avatarId
        )
        do {
            try await env.users.createUser(user)
            return true
        } catch {
            self.error = error.localizedDescription
            return false
        }
    }
}

struct ProfileSetupScreen: View {
    @Environment(AppEnvironment.self) private var env
    let onCompleted: () -> Void

    @State private var model: ProfileSetupModel?

    var body: some View {
        Group {
            if let model { content(model: model) }
            else { Color.clear.onAppear { model = ProfileSetupModel(env: env) } }
        }
    }

    @ViewBuilder
    private func content(model: ProfileSetupModel) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("프로필 설정").font(.largeTitle.bold())
                Text("닉네임과 차량 정보를 알려주세요")
                    .foregroundStyle(.secondary)

                Spacer().frame(height: 8)

                TextField("닉네임 (2~16자)", text: Binding(get: { model.nickname }, set: { model.nickname = $0 }))
                    .textFieldStyle(.roundedBorder)

                HStack(spacing: 8) {
                    TextField("브랜드 (BMW)", text: Binding(get: { model.carBrand }, set: { model.carBrand = $0 }))
                        .textFieldStyle(.roundedBorder)
                    TextField("모델 (X5)", text: Binding(get: { model.carModel }, set: { model.carModel = $0 }))
                        .textFieldStyle(.roundedBorder)
                }

                Text("아바타").font(.title3.weight(.semibold)).padding(.top, 12)

                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 4), spacing: 12) {
                    ForEach(Avatars.all) { meta in
                        Button {
                            model.avatarId = meta.id
                        } label: {
                            ZStack {
                                Circle().fill(meta.color)
                                Text(meta.initial).font(.system(size: 32))
                            }
                            .frame(width: 64, height: 64)
                            .overlay(
                                Circle().strokeBorder(
                                    Brand.primary,
                                    lineWidth: model.avatarId == meta.id ? 3 : 0
                                )
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }

                if let err = model.error {
                    Text(err).foregroundStyle(.red).font(.footnote)
                }

                Button {
                    Task {
                        if await model.save() { onCompleted() }
                    }
                } label: {
                    Text("저장").frame(maxWidth: .infinity, minHeight: 52)
                }
                .buttonStyle(.borderedProminent)
                .disabled(model.isSubmitting)
                .padding(.top, 8)
            }
            .padding(24)
        }
    }
}
