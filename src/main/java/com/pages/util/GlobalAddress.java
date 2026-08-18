package com.pages.util;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
public class GlobalAddress {

    @Id @GeneratedValue
    private Long id;
    private String city;
    private String country;
    private String countryCode;
    private double latitude;
    private double longitude;

    public GlobalAddress(){}
    public GlobalAddress(Long id, String city, String country, String countryCode, double latitude, double longitude) {
        this.id = id;
        this.city = city;
        this.country = country;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
