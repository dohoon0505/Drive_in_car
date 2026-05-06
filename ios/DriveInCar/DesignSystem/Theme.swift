import SwiftUI

enum Brand {
    static let primary = Color(red: 0x1F / 255, green: 0x6F / 255, blue: 0xEB / 255)
    static let onPrimary = Color.white
    static let secondary = Color(red: 0xF8 / 255, green: 0x71 / 255, blue: 0x71 / 255)
    static let surface = Color(.systemBackground)
    static let onSurfaceMuted = Color.secondary
}

enum Avatars {
    struct Meta: Identifiable, Hashable {
        let id: String
        let initial: String
        let color: Color
    }

    static let all: [Meta] = [
        .init(id: "avatar_01", initial: "🚗", color: Color(red: 0xEF/255, green: 0x44/255, blue: 0x44/255)),
        .init(id: "avatar_02", initial: "🏎️", color: Color(red: 0xF5/255, green: 0x9E/255, blue: 0x0B/255)),
        .init(id: "avatar_03", initial: "🚙", color: Color(red: 0x10/255, green: 0xB9/255, blue: 0x81/255)),
        .init(id: "avatar_04", initial: "🚘", color: Color(red: 0x3B/255, green: 0x82/255, blue: 0xF6/255)),
        .init(id: "avatar_05", initial: "🏁", color: Color(red: 0x8B/255, green: 0x5C/255, blue: 0xF6/255)),
        .init(id: "avatar_06", initial: "🛣️", color: Color(red: 0xEC/255, green: 0x48/255, blue: 0x99/255)),
        .init(id: "avatar_07", initial: "🌄", color: Color(red: 0x14/255, green: 0xB8/255, blue: 0xA6/255)),
        .init(id: "avatar_08", initial: "🔧", color: Color(red: 0x6B/255, green: 0x72/255, blue: 0x80/255)),
    ]

    static func byId(_ id: String) -> Meta { all.first(where: { $0.id == id }) ?? all[0] }
}

struct AvatarBadge: View {
    let avatarId: String
    var size: CGFloat = 56

    var body: some View {
        let m = Avatars.byId(avatarId)
        ZStack {
            Circle().fill(m.color)
            Text(m.initial).font(.system(size: size * 0.5))
        }
        .frame(width: size, height: size)
    }
}
