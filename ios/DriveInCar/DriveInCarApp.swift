import SwiftUI
import FirebaseCore
import GoogleMaps

@main
struct DriveInCarApp: App {

    @State private var environment = AppEnvironment()

    init() {
        FirebaseApp.configure()

        // Maps API key from Info.plist (xcconfig 주입)
        if let key = Bundle.main.object(forInfoDictionaryKey: "GMS_API_KEY") as? String,
           !key.isEmpty {
            GMSServices.provideAPIKey(key)
        } else {
            print("[DriveInCar] WARNING: GMS_API_KEY missing in Info.plist")
        }
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(environment)
        }
    }
}
