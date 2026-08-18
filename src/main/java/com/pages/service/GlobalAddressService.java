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

    public void updateAddress(String city,String country,Double lat,Double lon,String countryCode){
       GlobalAddress globalAddress= globalAddressRepo.findByCity(city).orElse(null);
       if(globalAddress ==null){
            GlobalAddress newAddress = GlobalAddress.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .countryCode(countryCode)
                    .city(city)
                    .country(country)
                    .build();
            globalAddressRepo.save(newAddress);
       }else{
           if(country!=null){
               globalAddress.setCountry(country);
           }
           if(countryCode!=null){
               globalAddress.setCountryCode(countryCode);
           }
           if(lat!=null){
               globalAddress.setLatitude(lat);
           }
           if(lon !=null){
               globalAddress.setLongitude(lon);
           }
           globalAddressRepo.save(globalAddress);
       }
    }
}
