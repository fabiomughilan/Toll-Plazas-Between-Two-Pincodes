package com.freightfox.tollplaza.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.freightfox.tollplaza.entity.PincodeEntity;
import com.freightfox.tollplaza.exception.InvalidPincodeException;
import com.freightfox.tollplaza.repository.PincodeRepository;

@ExtendWith(MockitoExtension.class)
class PincodeServiceTest {

    @Mock
    private PincodeRepository pincodeRepository;

    private PincodeService pincodeService;

    @BeforeEach
    void setUp() {
        pincodeService = new PincodeService(pincodeRepository);
    }

    @Test
    @DisplayName("Should return pincode details when pincode exists in DB")
    void testGetPincodeDetailsSuccess() {
        PincodeEntity entity = PincodeEntity.builder()
                .pincode("110001")
                .latitude(28.6315)
                .longitude(77.2167)
                .district("Delhi")
                .state("Delhi")
                .build();

        when(pincodeRepository.findByPincode("110001")).thenReturn(Optional.of(entity));

        PincodeEntity result = pincodeService.getPincodeDetails("110001");
        assertNotNull(result);
        assertEquals("110001", result.getPincode());
        assertEquals(28.6315, result.getLatitude());
    }

    @Test
    @DisplayName("Should throw InvalidPincodeException for malformed pincodes")
    void testGetPincodeDetailsInvalidFormat() {
        assertThrows(InvalidPincodeException.class, () -> pincodeService.getPincodeDetails("ABC123"));
        assertThrows(InvalidPincodeException.class, () -> pincodeService.getPincodeDetails("12345"));
        assertThrows(InvalidPincodeException.class, () -> pincodeService.getPincodeDetails("000000"));
    }

    @Test
    @DisplayName("Should throw InvalidPincodeException when pincode not found in DB or API")
    void testGetPincodeDetailsNotFound() {
        when(pincodeRepository.findByPincode(anyString())).thenReturn(Optional.empty());
        assertThrows(InvalidPincodeException.class, () -> pincodeService.getPincodeDetails("999999"));
    }
}
