package com.pages.dto;

public record LocationRequest(
        double latitude,
        double longitude,
        String locale
) {}
