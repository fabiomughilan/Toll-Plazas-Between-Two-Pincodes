package com.freightfox.tollplaza.util;

public class HaversineUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculates great-circle distance between two geographic points using Haversine formula.
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(rLat1) * Math.cos(rLat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates the minimum distance in km from a point (pLat, pLon) to a line segment (lat1, lon1) -> (lat2, lon2).
     */
    public static double distanceToSegment(double pLat, double pLon, double lat1, double lon1, double lat2, double lon2) {
        double l2 = Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2);
        if (l2 == 0) {
            return calculateDistance(pLat, pLon, lat1, lon1);
        }

        // Projection factor t along line segment [0, 1]
        double t = ((pLat - lat1) * (lat2 - lat1) + (pLon - lon1) * (lon2 - lon1)) / l2;
        t = Math.max(0, Math.min(1, t));

        double projLat = lat1 + t * (lat2 - lat1);
        double projLon = lon1 + t * (lon2 - lon1);

        return calculateDistance(pLat, pLon, projLat, projLon);
    }

    /**
     * Projects a point onto a line segment and calculates distance from origin (lat1, lon1) along segment.
     */
    public static double projectedDistanceAlongSegment(double pLat, double pLon, double lat1, double lon1, double lat2, double lon2) {
        double l2 = Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2);
        if (l2 == 0) return 0.0;

        double t = ((pLat - lat1) * (lat2 - lat1) + (pLon - lon1) * (lon2 - lon1)) / l2;
        t = Math.max(0, Math.min(1, t));

        double projLat = lat1 + t * (lat2 - lat1);
        double projLon = lon1 + t * (lon2 - lon1);

        return calculateDistance(lat1, lon1, projLat, projLon);
    }
}
