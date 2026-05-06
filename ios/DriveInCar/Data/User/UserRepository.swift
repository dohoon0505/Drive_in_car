import Foundation
import FirebaseFirestore

protocol UserRepository: AnyObject, Sendable {
    func fetchUser(uid: String) async throws -> User?
    func observeUser(uid: String) -> AsyncStream<User?>
    func createUser(_ user: User) async throws
    func updateUser(_ user: User) async throws
}

final class FirestoreUserRepository: UserRepository {

    private let db = Firestore.firestore()

    private func usersCol() -> CollectionReference { db.collection("users") }

    func fetchUser(uid: String) async throws -> User? {
        let snap = try await usersCol().document(uid).getDocument()
        return snap.toUser()
    }

    func observeUser(uid: String) -> AsyncStream<User?> {
        AsyncStream { cont in
            let reg = usersCol().document(uid).addSnapshotListener { snap, _ in
                cont.yield(snap?.toUser())
            }
            cont.onTermination = { _ in reg.remove() }
        }
    }

    func createUser(_ user: User) async throws {
        try await usersCol().document(user.uid).setData([
            "nickname": user.nickname,
            "carBrand": user.carBrand,
            "carModel": user.carModel,
            "profileImageId": user.profileImageId,
            "createdAt": FieldValue.serverTimestamp(),
            "updatedAt": FieldValue.serverTimestamp(),
        ])
    }

    func updateUser(_ user: User) async throws {
        try await usersCol().document(user.uid).updateData([
            "nickname": user.nickname,
            "carBrand": user.carBrand,
            "carModel": user.carModel,
            "profileImageId": user.profileImageId,
            "updatedAt": FieldValue.serverTimestamp(),
        ])
    }
}

private extension DocumentSnapshot {
    func toUser() -> User? {
        guard exists, let d = data() else { return nil }
        guard
            let nickname = d["nickname"] as? String,
            let carBrand = d["carBrand"] as? String,
            let carModel = d["carModel"] as? String,
            let profileImageId = d["profileImageId"] as? String
        else { return nil }
        return User(
            uid: documentID,
            nickname: nickname,
            carBrand: carBrand,
            carModel: carModel,
            profileImageId: profileImageId
        )
    }
}
