package com.freightfox.tollplaza.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pincodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PincodeEntity {

    @Id
    @Column(name = "pincode", length = 10, nullable = false)
    private String pincode;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "district")
    private String district;

    @Column(name = "state")
    private String state;
}
