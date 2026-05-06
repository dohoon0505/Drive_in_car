import SwiftUI

struct SplashScreen: View {
    var body: some View {
        ZStack {
            Brand.surface.ignoresSafeArea()
            Text("Drive in Car")
                .font(.system(size: 48, weight: .bold))
                .foregroundStyle(Brand.primary)
        }
    }
}
