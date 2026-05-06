import Foundation
import FirebaseAuth

protocol AuthRepository: AnyObject, Sendable {
    var currentUid: String? { get }
    func signIn(email: String, password: String) async throws -> String
    func signUp(email: String, password: String) async throws -> String
    func signOut() throws
    func authState() -> AsyncStream<String?>
}

final class FirebaseAuthRepository: AuthRepository {

    var currentUid: String? { Auth.auth().currentUser?.uid }

    func signIn(email: String, password: String) async throws -> String {
        let r = try await Auth.auth().signIn(withEmail: email, password: password)
        return r.user.uid
    }

    func signUp(email: String, password: String) async throws -> String {
        let r = try await Auth.auth().createUser(withEmail: email, password: password)
        return r.user.uid
    }

    func signOut() throws { try Auth.auth().signOut() }

    func authState() -> AsyncStream<String?> {
        AsyncStream { cont in
            let handle = Auth.auth().addStateDidChangeListener { _, user in
                cont.yield(user?.uid)
            }
            cont.onTermination = { _ in
                Auth.auth().removeStateDidChangeListener(handle)
            }
        }
    }
}
