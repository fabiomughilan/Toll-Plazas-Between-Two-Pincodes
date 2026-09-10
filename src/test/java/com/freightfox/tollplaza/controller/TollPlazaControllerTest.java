package com.freightfox.tollplaza.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightfox.tollplaza.dto.RouteInfo;
import com.freightfox.tollplaza.dto.TollPlazaDto;
import com.freightfox.tollplaza.dto.TollPlazaRequest;
import com.freightfox.tollplaza.dto.TollPlazaResponse;
import com.freightfox.tollplaza.exception.InvalidPincodeException;
import com.freightfox.tollplaza.exception.SamePincodeException;
import com.freightfox.tollplaza.service.TollPlazaService;

@WebMvcTest(TollPlazaController.class)
class TollPlazaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TollPlazaService tollPlazaService;

    @Test
    @DisplayName("POST /api/v1/toll-plazas should return 200 OK with route and toll plazas")
    void testGetTollPlazasSuccess() throws Exception {
        TollPlazaRequest request = TollPlazaRequest.builder()
                .sourcePincode("110001")
                .destinationPincode("560001")
                .build();

        RouteInfo routeInfo = RouteInfo.builder()
                .sourcePincode("110001")
                .destinationPincode("560001")
                .distanceInKm(2100)
                .build();

        TollPlazaDto plaza1 = TollPlazaDto.builder()
                .name("Toll Plaza 1")
                .latitude(28.7041)
                .longitude(77.1025)
                .distanceFromSource(200)
                .build();

        TollPlazaResponse response = TollPlazaResponse.builder()
                .route(routeInfo)
                .tollPlazas(List.of(plaza1))
                .build();

        given(tollPlazaService.getTollPlazasBetweenPincodes(any(TollPlazaRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/toll-plazas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.sourcePincode").value("110001"))
                .andExpect(jsonPath("$.route.destinationPincode").value("560001"))
                .andExpect(jsonPath("$.route.distanceInKm").value(2100))
                .andExpect(jsonPath("$.tollPlazas[0].name").value("Toll Plaza 1"))
                .andExpect(jsonPath("$.tollPlazas[0].distanceFromSource").value(200));
    }

    @Test
    @DisplayName("POST /api/v1/toll-plazas should return 400 Bad Request when pincodes are identical")
    void testSamePincodeError() throws Exception {
        TollPlazaRequest request = TollPlazaRequest.builder()
                .sourcePincode("110001")
                .destinationPincode("110001")
                .build();

        given(tollPlazaService.getTollPlazasBetweenPincodes(any(TollPlazaRequest.class)))
                .willThrow(new SamePincodeException("Source and destination pincodes cannot be the same"));

        mockMvc.perform(post("/api/v1/toll-plazas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Source and destination pincodes cannot be the same"));
    }

    @Test
    @DisplayName("POST /api/v1/toll-plazas should return 400 Bad Request when pincode is invalid")
    void testInvalidPincodeError() throws Exception {
        TollPlazaRequest request = TollPlazaRequest.builder()
                .sourcePincode("000000")
                .destinationPincode("560001")
                .build();

        given(tollPlazaService.getTollPlazasBetweenPincodes(any(TollPlazaRequest.class)))
                .willThrow(new InvalidPincodeException("Invalid source or destination pincode"));

        mockMvc.perform(post("/api/v1/toll-plazas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid source or destination pincode"));
    }
}
