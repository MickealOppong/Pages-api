package com.pages.service;

import com.pages.exception.EntityNotFoundException;
import com.pages.model.AppUser;
import com.pages.repository.AppUserRepo;
import com.pages.repository.AppUserRoleRepo;
import com.pages.repository.GlobalAddressRepo;
import com.pages.util.GlobalAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GlobalAddressService {

    private final GlobalAddressRepo globalAddressRepo;
    private final AppUserRepo appUserRepo;


    public GlobalAddressService(GlobalAddressRepo globalAddressRepo, AppUserRepo appUserRepo) {
        this.globalAddressRepo = globalAddressRepo;
        this.appUserRepo = appUserRepo;
    }

    public void addToAddress(String city,String country,String countryCode,double latitude,double longitude){
        GlobalAddress address =globalAddressRepo.findFirstByCity(city).orElse(null);
        if(address ==null) {
            GlobalAddress globalAddress = GlobalAddress.builder()
                    .city(city)
                    .country(country)
                    .countryCode(countryCode)
                    .longitude(longitude)
                    .latitude(latitude)
                    .build();
            globalAddressRepo.save(globalAddress);
        }
    }

    public void updateAddress(String city, String country, Double lat, Double lon, String countryCode) {
        // 1. Guard clause: Ensure coordinates are valid before processing
        if (lat == null || lon == null || lat == 0.0 || lon == 0.0) {
            return;
        }

        // 2. Look up by a tight coordinate range (approx. 5-10km bounding box)
        // This catches "Warszawa", "Warsaw", and "Warschau" as the same entry!
        double margin = 0.08; // Roughly 8-9 kilometers tolerance
        GlobalAddress globalAddress = globalAddressRepo
                .findFirstByLatitudeBetweenAndLongitudeBetween(lat - margin, lat + margin, lon - margin, lon + margin)
                .orElse(null);

        if (globalAddress != null) {
            // City exists! Update empty fields, but prefer keeping whatever language was stored first
            if (globalAddress.getCountryCode() == null || globalAddress.getCountryCode().isBlank()) {
                globalAddress.setCountryCode(countryCode);
            }
            // Always snap the coordinates to the most precise ones provided
            globalAddress.setLatitude(lat);
            globalAddress.setLongitude(lon);

            globalAddressRepo.save(globalAddress);
        } else {
            // 3. Brand new city coordinates -> Add it to the database registry safely
            if (city != null && !city.isBlank() && country != null && !country.isBlank()) {
                addToAddress(city, country, countryCode, lat, lon);
            }
        }
    }


    public Set<String> getAllCities(Jwt jwt){
        String username = jwt.getSubject();

        AppUser user = appUserRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        return globalAddressRepo.findByCountryCode(user.getCountryCode()).stream().map(GlobalAddress::getCity).collect(Collectors.toSet());
    }
}
