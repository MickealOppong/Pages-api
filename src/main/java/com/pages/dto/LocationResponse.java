package com.pages.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


public record LocationResponse(
        String city,
        String country,
        String countryCode,
         Double lat,
        Double lon
) {

}
