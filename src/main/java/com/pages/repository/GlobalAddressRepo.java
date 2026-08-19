package com.pages.repository;

import com.pages.util.GlobalAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface GlobalAddressRepo extends JpaRepository<GlobalAddress,Long> {
    Optional<GlobalAddress> findFirstByCity(String city);
    Set<GlobalAddress> findByCountryCode(String countryCode);
    Set<GlobalAddress> findByCountry(String country);

    Optional<GlobalAddress> findFirstByLatitudeBetweenAndLongitudeBetween(Double lat1,Double lon1,Double lat2,Double lon2);

}
