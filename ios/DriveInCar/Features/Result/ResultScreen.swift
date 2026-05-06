import SwiftUI

struct ResultScreen: View {
    let courseId: String
    let timeMs: Int64
    let averageKmh: Double
    let flagged: Bool
    let personalBest: Bool
    let onViewRanking: () -> Void
    let onRetry: () -> Void
    let onBackToMap: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Text("기록").font(.title3).foregroundStyle(.secondary)
            Text(TimeFormat.raceTime(ms: timeMs))
                .font(.system(size: 96, weight: .bold).monospacedDigit())
            Text("평균 \(Int(averageKmh)) km/h").foregroundStyle(.secondary)

            if personalBest {
                Text("베스트 갱신!")
                    .padding(.horizontal, 12).padding(.vertical, 6)
                    .foregroundStyle(.white)
                    .background(Brand.primary)
                    .clipShape(Capsule())
            }
            if flagged {
                Text("비정상 평균 속도로 리더보드에서 제외됨")
                    .padding(.horizontal, 12).padding(.vertical, 6)
                    .background(Color.red.opacity(0.2))
                    .clipShape(Capsule())
            }

            Spacer()

            HStack(spacing: 8) {
                Button {
                    onViewRanking()
                } label: { Text("랭킹 보기").frame(maxWidth: .infinity, minHeight: 52) }
                    .buttonStyle(.borderedProminent)
                Button {
                    onRetry()
                } label: { Text("다시 도전").frame(maxWidth: .infinity, minHeight: 52) }
                    .buttonStyle(.bordered)
            }
            Button {
                onBackToMap()
            } label: { Text("지도로").frame(maxWidth: .infinity, minHeight: 48) }
                .buttonStyle(.bordered)
        }
        .padding(24)
        .navigationBarBackButtonHidden(true)
    }
}
