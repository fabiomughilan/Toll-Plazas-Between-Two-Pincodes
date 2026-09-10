package com.freightfox.tollplaza.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightfox.tollplaza.util.HaversineUtil;
import com.freightfox.tollplaza.util.SpatialRouteUtil.Point;

@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RoutingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public static class RouteResult {
        private final double distanceInKm;
        private final List<Point> waypoints;

        public RouteResult(double distanceInKm, List<Point> waypoints) {
            this.distanceInKm = distanceInKm;
            this.waypoints = waypoints;
        }

        public double getDistanceInKm() {
            return distanceInKm;
        }

        public List<Point> getWaypoints() {
            return waypoints;
        }
    }

    public RouteResult getRoute(double startLat, double startLon, double endLat, double endLon) {
        // Try calling OSRM API for driving route polyline
        try {
            String osrmUrl = String.format("http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                    startLon, startLat, endLon, endLat);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TollPlazaService/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(osrmUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if ("Ok".equals(root.path("code").asText())) {
                    JsonNode routeNode = root.path("routes").get(0);
                    double distanceMeters = routeNode.path("distance").asDouble();
                    double distanceKm = Math.round(distanceMeters / 1000.0);

                    List<Point> waypoints = new ArrayList<>();
                    JsonNode coordinates = routeNode.path("geometry").path("coordinates");
                    for (JsonNode coord : coordinates) {
                        double lon = coord.get(0).asDouble();
                        double lat = coord.get(1).asDouble();
                        waypoints.add(new Point(lat, lon));
                    }

                    if (!waypoints.isEmpty()) {
                        log.info("Successfully fetched OSRM route. Distance: {} km, Waypoints: {}", distanceKm, waypoints.size());
                        return new RouteResult(distanceKm, waypoints);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("OSRM routing API call unavailable or timed out: {}. Using geometric route corridor fallback.", e.getMessage());
        }

        // Fallback: Generate interpolated spatial waypoints between origin & destination
        return generateFallbackRoute(startLat, startLon, endLat, endLon);
    }

    private RouteResult generateFallbackRoute(double startLat, double startLon, double endLat, double endLon) {
        double straightDistance = HaversineUtil.calculateDistance(startLat, startLon, endLat, endLon);
        // Multiply by 1.22 for realistic road tortuosity/distance
        double estimatedRoadDistance = Math.round(straightDistance * 1.22);
        if (estimatedRoadDistance < 1) estimatedRoadDistance = 1;

        List<Point> waypoints = new ArrayList<>();
        int steps = 100;
        for (int i = 0; i <= steps; i++) {
            double fraction = (double) i / steps;
            double lat = startLat + fraction * (endLat - startLat);
            double lon = startLon + fraction * (endLon - startLon);
            waypoints.add(new Point(lat, lon));
        }

        return new RouteResult(estimatedRoadDistance, waypoints);
    }
}
