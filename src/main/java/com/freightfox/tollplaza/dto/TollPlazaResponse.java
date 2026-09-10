package com.freightfox.tollplaza.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TollPlazaResponse {

    private RouteInfo route;
    private List<TollPlazaDto> tollPlazas;
}
