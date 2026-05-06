import Foundation

struct User: Equatable, Sendable, Identifiable {
    let uid: String
    var nickname: String
    var carBrand: String
    var carModel: String
    var profileImageId: String

    var id: String { uid }
    var carDisplay: String { "\(carBrand) \(carModel)" }
}
