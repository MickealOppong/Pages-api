package com.pages.dto;

public record GeoapifyResult(
        String city,
        String country,
        String country_code,
        Double lat,
        Double lon,
        String formatted,
        String result_type
) {
}
