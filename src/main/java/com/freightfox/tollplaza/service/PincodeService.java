package com.freightfox.tollplaza.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightfox.tollplaza.entity.PincodeEntity;
import com.freightfox.tollplaza.exception.InvalidPincodeException;
import com.freightfox.tollplaza.repository.PincodeRepository;

@Service
public class PincodeService {

    private static final Logger log = LoggerFactory.getLogger(PincodeService.class);

    private final PincodeRepository pincodeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PincodeService(PincodeRepository pincodeRepository) {
        this.pincodeRepository = pincodeRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public PincodeEntity getPincodeDetails(String pincode) {
        if (pincode == null || !pincode.matches("^[1-9][0-9]{5}$")) {
            throw new InvalidPincodeException("Invalid source or destination pincode");
        }

        // 1. Check local database
        Optional<PincodeEntity> dbPincode = pincodeRepository.findByPincode(pincode);
        if (dbPincode.isPresent()) {
            return dbPincode.get();
        }

        // 2. Fallback to OpenStreetMap / Nominatim API for unknown Indian pincodes
        try {
            log.info("Pincode {} not in database, querying Nominatim external API...", pincode);
            String url = "https://nominatim.openstreetmap.org/search?postalcode=" + pincode + "&country=India&format=json&limit=1";
            
            // Add custom User-Agent as required by Nominatim usage policy
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "TollPlazaService/1.0 (Freightfox-Assignment)");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    JsonNode firstResult = root.get(0);
                    double lat = firstResult.get("lat").asDouble();
                    double lon = firstResult.get("lon").asDouble();
                    String displayName = firstResult.has("display_name") ? firstResult.get("display_name").asText() : "";

                    PincodeEntity newEntity = PincodeEntity.builder()
                            .pincode(pincode)
                            .latitude(lat)
                            .longitude(lon)
                            .district(displayName)
                            .state("India")
                            .build();

                    pincodeRepository.save(newEntity);
                    log.info("Successfully fetched and cached pincode {} from Nominatim: ({}, {})", pincode, lat, lon);
                    return newEntity;
                }
            }
        } catch (Exception e) {
            log.warn("External Nominatim geocoding failed for pincode {}: {}", pincode, e.getMessage());
        }

        throw new InvalidPincodeException("Invalid source or destination pincode");
    }
}
