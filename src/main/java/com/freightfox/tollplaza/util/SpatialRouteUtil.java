package com.freightfox.tollplaza.util;

import java.util.List;

public class SpatialRouteUtil {

    // Maximum perpendicular distance (in km) from route corridor to consider a toll plaza on the route
    private static final double MAX_CORRIDOR_DEVIATION_KM = 35.0;

    public static class Point {
        public double lat;
        public double lon;

        public Point(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    /**
     * Determines if a toll plaza is located along a route path (represented as polyline points).
     */
    public static boolean isTollPlazaOnRoute(double tollLat, double tollLon, List<Point> routePath) {
        if (routePath == null || routePath.size() < 2) {
            return false;
        }

        for (int i = 0; i < routePath.size() - 1; i++) {
            Point p1 = routePath.get(i);
            Point p2 = routePath.get(i + 1);

            double distToSeg = HaversineUtil.distanceToSegment(tollLat, tollLon, p1.lat, p1.lon, p2.lat, p2.lon);
            if (distToSeg <= MAX_CORRIDOR_DEVIATION_KM) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates accumulated route distance from origin (first point) to the projected location of the toll plaza on the route path.
     */
    public static double calculateDistanceFromSource(double tollLat, double tollLon, List<Point> routePath) {
        if (routePath == null || routePath.isEmpty()) {
            return 0.0;
        }

        double minDev = Double.MAX_VALUE;
        double bestAccDistance = 0.0;
        double currentAccDistance = 0.0;

        for (int i = 0; i < routePath.size() - 1; i++) {
            Point p1 = routePath.get(i);
            Point p2 = routePath.get(i + 1);

            double segLength = HaversineUtil.calculateDistance(p1.lat, p1.lon, p2.lat, p2.lon);
            double dev = HaversineUtil.distanceToSegment(tollLat, tollLon, p1.lat, p1.lon, p2.lat, p2.lon);

            if (dev < minDev) {
                minDev = dev;
                double projDistOnSeg = HaversineUtil.projectedDistanceAlongSegment(tollLat, tollLon, p1.lat, p1.lon, p2.lat, p2.lon);
                bestAccDistance = currentAccDistance + projDistOnSeg;
            }

            currentAccDistance += segLength;
        }

        return Math.round(bestAccDistance);
    }
}
