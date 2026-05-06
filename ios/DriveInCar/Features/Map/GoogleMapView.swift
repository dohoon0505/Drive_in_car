import SwiftUI
import GoogleMaps

/// SwiftUI에서 GMSMapView를 래핑한다. 마커 탭 시 onMarkerTap 호출.
struct GoogleMapView: UIViewRepresentable {
    let courses: [Course]
    let onMarkerTap: (Course) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(onMarkerTap: onMarkerTap)
    }

    func makeUIView(context: Context) -> GMSMapView {
        let camera = GMSCameraPosition(latitude: 37.5, longitude: 127.5, zoom: 6.5)
        let map = GMSMapView()
        map.camera = camera
        map.delegate = context.coordinator
        return map
    }

    func updateUIView(_ uiView: GMSMapView, context: Context) {
        context.coordinator.refreshMarkers(on: uiView, with: courses)
    }

    @MainActor
    final class Coordinator: NSObject, GMSMapViewDelegate {
        private var markers: [String: GMSMarker] = [:]
        private var coursesById: [String: Course] = [:]
        let onMarkerTap: (Course) -> Void

        init(onMarkerTap: @escaping (Course) -> Void) {
            self.onMarkerTap = onMarkerTap
        }

        func refreshMarkers(on map: GMSMapView, with courses: [Course]) {
            let newIds = Set(courses.map { $0.courseId })
            let oldIds = Set(markers.keys)

            // remove gone
            for id in oldIds.subtracting(newIds) {
                markers[id]?.map = nil
                markers[id] = nil
                coursesById[id] = nil
            }

            // add or update
            for c in courses {
                coursesById[c.courseId] = c
                if let m = markers[c.courseId] {
                    m.position = c.startCoord.clCoordinate
                    m.title = c.name
                    m.snippet = c.regionName
                } else {
                    let m = GMSMarker(position: c.startCoord.clCoordinate)
                    m.title = c.name
                    m.snippet = c.regionName
                    m.userData = c.courseId
                    m.map = map
                    markers[c.courseId] = m
                }
            }
        }

        nonisolated func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
            guard let courseId = marker.userData as? String else { return false }
            Task { @MainActor in
                if let c = self.coursesById[courseId] {
                    self.onMarkerTap(c)
                }
            }
            return true
        }
    }
}
