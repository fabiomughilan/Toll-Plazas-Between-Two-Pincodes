package com.freightfox.tollplaza.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HaversineUtilTest {

    @Test
    @DisplayName("Should calculate accurate Haversine distance between Delhi and Bengaluru")
    void testCalculateDistanceDelhiToBengaluru() {
        // Delhi: 28.6315, 77.2167
        // Bengaluru: 12.9716, 77.5946
        double dist = HaversineUtil.calculateDistance(28.6315, 77.2167, 12.9716, 77.5946);
        // Approx 1740 km direct air distance
        assertTrue(dist > 1700 && dist < 1800, "Distance should be ~1740km");
    }

    @Test
    @DisplayName("Should calculate distance to line segment accurately")
    void testDistanceToSegment() {
        // Point on line segment
        double pLat = 15.0, pLon = 77.0;
        double segStartLat = 10.0, segStartLon = 77.0;
        double segEndLat = 20.0, segEndLon = 77.0;

        double dev = HaversineUtil.distanceToSegment(pLat, pLon, segStartLat, segStartLon, segEndLat, segEndLon);
        assertEquals(0.0, dev, 0.1, "Perpendicular deviation should be ~0km");
    }
}
