package com.pages.util;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
}
