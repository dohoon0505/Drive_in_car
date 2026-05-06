import Foundation
import FirebaseFirestore

protocol CourseRepository: AnyObject, Sendable {
    func observeActive() -> AsyncStream<[Course]>
    func fetchCourse(id: String) async throws -> Course?
}

final class FirestoreCourseRepository: CourseRepository {

    private let db = Firestore.firestore()
    private func coursesCol() -> CollectionReference { db.collection("courses") }

    func observeActive() -> AsyncStream<[Course]> {
        AsyncStream { cont in
            let reg = coursesCol()
                .whereField("isActive", isEqualTo: true)
                .addSnapshotListener { snap, _ in
                    let list = snap?.documents.compactMap { $0.toCourse() } ?? []
                    cont.yield(list)
                }
            cont.onTermination = { _ in reg.remove() }
        }
    }

    func fetchCourse(id: String) async throws -> Course? {
        let snap = try await coursesCol().document(id).getDocument()
        return snap.toCourse()
    }
}

private extension DocumentSnapshot {
    func toCourse() -> Course? {
        guard exists, let d = data() else { return nil }
        guard
            let name = d["name"] as? String,
            let start = d["startCoord"] as? GeoPoint,
            let end = d["endCoord"] as? GeoPoint
        else { return nil }
        let waypointsRaw = d["waypoints"] as? [[String: Any]] ?? []
        let waypoints: [Waypoint] = waypointsRaw.compactMap { m in
            guard
                let lat = (m["lat"] as? NSNumber)?.doubleValue,
                let lng = (m["lng"] as? NSNumber)?.doubleValue
            else { return nil }
            let order = (m["order"] as? NSNumber)?.intValue ?? 0
            return Waypoint(lat: lat, lng: lng, order: order)
        }.sorted { $0.order < $1.order }

        return Course(
            courseId: documentID,
            name: name,
            description: d["description"] as? String ?? "",
            regionName: d["regionName"] as? String ?? "",
            startCoord: LatLng(lat: start.latitude, lng: start.longitude),
            endCoord: LatLng(lat: end.latitude, lng: end.longitude),
            waypoints: waypoints,
            distanceMeters: (d["distanceMeters"] as? NSNumber)?.doubleValue ?? 0,
            difficulty: (d["difficulty"] as? NSNumber)?.intValue ?? 0,
            isActive: d["isActive"] as? Bool ?? false
        )
    }
}
