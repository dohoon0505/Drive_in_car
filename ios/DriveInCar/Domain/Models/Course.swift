import Foundation
import CoreLocation

struct LatLng: Equatable, Sendable, Hashable {
    let lat: Double
    let lng: Double

    var clCoordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: lat, longitude: lng)
    }
}

struct Waypoint: Equatable, Sendable, Hashable {
    let lat: Double
    let lng: Double
    let order: Int
}

struct Course: Identifiable, Equatable, Sendable {
    let courseId: String
    let name: String
    let description: String
    let regionName: String
    let startCoord: LatLng
    let endCoord: LatLng
    let waypoints: [Waypoint]
    let distanceMeters: Double
    let difficulty: Int
    let isActive: Bool

    var id: String { courseId }
}
