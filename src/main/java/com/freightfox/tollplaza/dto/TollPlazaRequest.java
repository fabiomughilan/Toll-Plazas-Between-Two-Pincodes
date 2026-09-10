package com.freightfox.tollplaza.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TollPlazaRequest {

    @NotBlank(message = "sourcePincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid source pincode format. Must be a 6-digit Indian pincode.")
    private String sourcePincode;

    @NotBlank(message = "destinationPincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid destination pincode format. Must be a 6-digit Indian pincode.")
    private String destinationPincode;
}
