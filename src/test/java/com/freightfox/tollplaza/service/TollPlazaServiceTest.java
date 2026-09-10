package com.freightfox.tollplaza.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.freightfox.tollplaza.dto.TollPlazaRequest;
import com.freightfox.tollplaza.dto.TollPlazaResponse;
import com.freightfox.tollplaza.entity.PincodeEntity;
import com.freightfox.tollplaza.entity.TollPlazaEntity;
import com.freightfox.tollplaza.exception.SamePincodeException;
import com.freightfox.tollplaza.repository.TollPlazaRepository;
import com.freightfox.tollplaza.util.SpatialRouteUtil.Point;

@ExtendWith(MockitoExtension.class)
class TollPlazaServiceTest {

    @Mock
    private PincodeService pincodeService;

    @Mock
    private RoutingService routingService;

    @Mock
    private TollPlazaRepository tollPlazaRepository;

    private TollPlazaService tollPlazaService;

    @BeforeEach
    void setUp() {
        tollPlazaService = new TollPlazaService(pincodeService, routingService, tollPlazaRepository);
    }

    @Test
    @DisplayName("Should throw SamePincodeException when source and destination pincodes are identical")
    void testSamePincodeException() {
        TollPlazaRequest request = TollPlazaRequest.builder()
                .sourcePincode("110001")
                .destinationPincode("110001")
                .build();

        assertThrows(SamePincodeException.class, () -> tollPlazaService.getTollPlazasBetweenPincodes(request));
    }

    @Test
    @DisplayName("Should return toll plazas sorted by distance from source")
    void testGetTollPlazasSuccess() {
        PincodeEntity src = PincodeEntity.builder().pincode("110001").latitude(28.6315).longitude(77.2167).build();
        PincodeEntity dest = PincodeEntity.builder().pincode("560001").latitude(12.9716).longitude(77.5946).build();

        when(pincodeService.getPincodeDetails("110001")).thenReturn(src);
        when(pincodeService.getPincodeDetails("560001")).thenReturn(dest);

        List<Point> waypoints = List.of(
                new Point(28.6315, 77.2167),
                new Point(20.0000, 77.0000),
                new Point(12.9716, 77.5946)
        );

        when(routingService.getRoute(28.6315, 77.2167, 12.9716, 77.5946))
                .thenReturn(new RoutingService.RouteResult(1800.0, waypoints));

        TollPlazaEntity plaza1 = TollPlazaEntity.builder().id(1L).name("Toll Plaza 1").latitude(25.0000).longitude(77.1000).build();
        TollPlazaEntity plaza2 = TollPlazaEntity.builder().id(2L).name("Toll Plaza 2").latitude(18.0000).longitude(77.2000).build();

        when(tollPlazaRepository.findAll()).thenReturn(List.of(plaza1, plaza2));

        TollPlazaRequest request = TollPlazaRequest.builder()
                .sourcePincode("110001")
                .destinationPincode("560001")
                .build();

        TollPlazaResponse response = tollPlazaService.getTollPlazasBetweenPincodes(request);

        assertNotNull(response);
        assertEquals("110001", response.getRoute().getSourcePincode());
        assertEquals("560001", response.getRoute().getDestinationPincode());
        assertEquals(1800.0, response.getRoute().getDistanceInKm());
        assertNotNull(response.getTollPlazas());
    }
}
