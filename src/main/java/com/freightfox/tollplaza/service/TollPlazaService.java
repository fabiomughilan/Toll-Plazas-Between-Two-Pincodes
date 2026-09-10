package com.freightfox.tollplaza.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.freightfox.tollplaza.config.CacheConfig;
import com.freightfox.tollplaza.dto.RouteInfo;
import com.freightfox.tollplaza.dto.TollPlazaDto;
import com.freightfox.tollplaza.dto.TollPlazaRequest;
import com.freightfox.tollplaza.dto.TollPlazaResponse;
import com.freightfox.tollplaza.entity.PincodeEntity;
import com.freightfox.tollplaza.entity.TollPlazaEntity;
import com.freightfox.tollplaza.exception.SamePincodeException;
import com.freightfox.tollplaza.repository.TollPlazaRepository;
import com.freightfox.tollplaza.util.SpatialRouteUtil;

@Service
public class TollPlazaService {

    private static final Logger log = LoggerFactory.getLogger(TollPlazaService.class);

    private final PincodeService pincodeService;
    private final RoutingService routingService;
    private final TollPlazaRepository tollPlazaRepository;

    public TollPlazaService(PincodeService pincodeService, RoutingService routingService, TollPlazaRepository tollPlazaRepository) {
        this.pincodeService = pincodeService;
        this.routingService = routingService;
        this.tollPlazaRepository = tollPlazaRepository;
    }

    @Cacheable(value = CacheConfig.TOLL_PLAZA_CACHE, key = "#request.sourcePincode + '-' + #request.destinationPincode")
    public TollPlazaResponse getTollPlazasBetweenPincodes(TollPlazaRequest request) {
        String srcPin = request.getSourcePincode();
        String destPin = request.getDestinationPincode();

        log.info("Processing toll plaza query between source {} and destination {}", srcPin, destPin);

        // 1. Validation: Same source and destination
        if (srcPin.trim().equalsIgnoreCase(destPin.trim())) {
            throw new SamePincodeException("Source and destination pincodes cannot be the same");
        }

        // 2. Fetch Pincode geographic details
        PincodeEntity srcEntity = pincodeService.getPincodeDetails(srcPin);
        PincodeEntity destEntity = pincodeService.getPincodeDetails(destPin);

        // 3. Compute Route & Waypoints
        RoutingService.RouteResult routeResult = routingService.getRoute(
                srcEntity.getLatitude(), srcEntity.getLongitude(),
                destEntity.getLatitude(), destEntity.getLongitude()
        );

        // 4. Fetch all toll plazas & match along route corridor
        List<TollPlazaEntity> allPlazas = tollPlazaRepository.findAll();
        List<TollPlazaDto> matchedPlazas = new ArrayList<>();

        for (TollPlazaEntity plaza : allPlazas) {
            boolean isOnRoute = SpatialRouteUtil.isTollPlazaOnRoute(
                    plaza.getLatitude(),
                    plaza.getLongitude(),
                    routeResult.getWaypoints()
            );

            if (isOnRoute) {
                double distFromSource = SpatialRouteUtil.calculateDistanceFromSource(
                        plaza.getLatitude(),
                        plaza.getLongitude(),
                        routeResult.getWaypoints()
                );

                // Ignore tolls located after total route distance or before origin
                if (distFromSource >= 0 && distFromSource <= routeResult.getDistanceInKm() + 50) {
                    matchedPlazas.add(TollPlazaDto.builder()
                            .name(plaza.getName())
                            .latitude(plaza.getLatitude())
                            .longitude(plaza.getLongitude())
                            .distanceFromSource(distFromSource)
                            .build());
                }
            }
        }

        // 5. Sort matched toll plazas sequentially by distanceFromSource
        matchedPlazas.sort(Comparator.comparingDouble(TollPlazaDto::getDistanceFromSource));

        // 6. Build response
        RouteInfo routeInfo = RouteInfo.builder()
                .sourcePincode(srcPin)
                .destinationPincode(destPin)
                .distanceInKm(routeResult.getDistanceInKm())
                .build();

        return TollPlazaResponse.builder()
                .route(routeInfo)
                .tollPlazas(matchedPlazas)
                .build();
    }
}
