package com.freightfox.tollplaza.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freightfox.tollplaza.entity.PincodeEntity;
import com.freightfox.tollplaza.entity.TollPlazaEntity;
import com.freightfox.tollplaza.repository.PincodeRepository;
import com.freightfox.tollplaza.repository.TollPlazaRepository;

@Service
public class DataSeederService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final PincodeRepository pincodeRepository;
    private final TollPlazaRepository tollPlazaRepository;

    public DataSeederService(PincodeRepository pincodeRepository, TollPlazaRepository tollPlazaRepository) {
        this.pincodeRepository = pincodeRepository;
        this.tollPlazaRepository = tollPlazaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedPincodes();
        seedTollPlazas();
    }

    private void seedPincodes() {
        try {
            ClassPathResource resource = new ClassPathResource("data/pincodes_india.csv");
            if (!resource.exists()) {
                log.warn("pincodes_india.csv resource not found!");
                return;
            }

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                Iterable<CSVRecord> records = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreSurroundingSpaces(true)
                        .build()
                        .parse(reader);

                List<PincodeEntity> pincodesToSave = new ArrayList<>();
                for (CSVRecord record : records) {
                    String pincode = record.get("pincode").trim();
                    double lat = Double.parseDouble(record.get("latitude").trim());
                    double lon = Double.parseDouble(record.get("longitude").trim());
                    String district = record.isMapped("district") ? record.get("district").trim() : "";
                    String state = record.isMapped("state") ? record.get("state").trim() : "";

                    pincodesToSave.add(PincodeEntity.builder()
                            .pincode(pincode)
                            .latitude(lat)
                            .longitude(lon)
                            .district(district)
                            .state(state)
                            .build());
                }

                // Batch Upsert
                pincodeRepository.saveAll(pincodesToSave);
                log.info("Successfully seeded/upserted {} Indian pincodes.", pincodesToSave.size());
            }
        } catch (Exception e) {
            log.error("Failed to seed pincodes from CSV: {}", e.getMessage(), e);
        }
    }

    private void seedTollPlazas() {
        try {
            ClassPathResource resource = new ClassPathResource("data/toll_plaza_india.csv");
            if (!resource.exists()) {
                log.warn("toll_plaza_india.csv resource not found!");
                return;
            }

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                Iterable<CSVRecord> records = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreSurroundingSpaces(true)
                        .build()
                        .parse(reader);

                List<TollPlazaEntity> plazasToSave = new ArrayList<>();
                for (CSVRecord record : records) {
                    String name = record.get("toll_name").trim();
                    double lat = Double.parseDouble(record.get("latitude").trim());
                    double lon = Double.parseDouble(record.get("longitude").trim());
                    String state = record.isMapped("geo_state") ? record.get("geo_state").trim() : "";

                    // Check for existing entity by name for true upsert
                    TollPlazaEntity entity = tollPlazaRepository.findByName(name)
                            .orElse(new TollPlazaEntity());

                    entity.setName(name);
                    entity.setLatitude(lat);
                    entity.setLongitude(lon);
                    entity.setHighway("");
                    entity.setState(state);

                    plazasToSave.add(entity);
                }

                tollPlazaRepository.saveAll(plazasToSave);
                log.info("Successfully seeded/upserted {} Toll Plazas.", plazasToSave.size());
            }
        } catch (Exception e) {
            log.error("Failed to seed toll plazas from CSV: {}", e.getMessage(), e);
        }
    }
}
