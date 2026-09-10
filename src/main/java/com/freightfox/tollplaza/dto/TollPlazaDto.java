package com.freightfox.tollplaza.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TollPlazaDto {

    private String name;
    private double latitude;
    private double longitude;
    private double distanceFromSource;
}
