package com.pages.service;

import com.pages.repository.GlobalAddressRepo;
import com.pages.util.GlobalAddress;
import org.springframework.stereotype.Service;

@Service
public class GlobalAddressService {

    private final GlobalAddressRepo globalAddressRepo;

    public GlobalAddressService(GlobalAddressRepo globalAddressRepo) {
        this.globalAddressRepo = globalAddressRepo;
    }

    public void addToAddress(String city,String country,String countryCode,double latitude,double longitude){
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
