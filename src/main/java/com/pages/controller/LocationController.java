package com.pages.controller;

import com.pages.dto.LocationRequest;
import com.pages.dto.LocationResponse;
import com.pages.dto.LocationResponseDto;
import com.pages.dto.ResponseDto;
import com.pages.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/detect")
    public LocationResponseDto detectLocation(@RequestBody LocationRequest request) {

        return locationService.detectCityByCoordinates(request.latitude(), request.longitude(),request.locale());
    }

    @GetMapping("/search")
    public LocationResponseDto searchCities(@RequestParam String city,@RequestParam String locale) {
        return locationService.searchCities(city,locale);
    }
}
