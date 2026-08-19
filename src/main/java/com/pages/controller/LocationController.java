package com.pages.controller;

import com.pages.dto.LocationRequest;
import com.pages.dto.LocationResponse;
import com.pages.dto.LocationResponseDto;
import com.pages.dto.ResponseDto;
import com.pages.service.GlobalAddressService;
import com.pages.service.LocationService;
import com.pages.util.GlobalAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;
    private final GlobalAddressService globalAddressService;

    public LocationController(LocationService locationService, GlobalAddressService globalAddressService) {
        this.locationService = locationService;
        this.globalAddressService = globalAddressService;
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
