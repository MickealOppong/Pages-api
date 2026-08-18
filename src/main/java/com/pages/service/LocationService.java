package com.pages.service;

import com.pages.dto.GeoapifyResponse;
import com.pages.dto.LocationResponse;
import com.pages.dto.LocationResponseDto;
import com.pages.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LocationService {

    private final RestClient restClient;


    private final String apiKey;

    public LocationService( @Value("${geoapify.api-key}") String apiKey) {
        this.restClient = RestClient.builder().build();
        this.apiKey = apiKey;
    }


    public LocationResponse detectCity(double latitude, double longitude,String locale) {

        Map<?, ?> response;

        try {

            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("nominatim.openstreetmap.org")
                            .path("/reverse")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("lang", locale)
                            .queryParam("format", "json")
                            .queryParam("addressdetails", "1")
                            .build()
                    )
                    .header(
                            "User-Agent",
                            "Spotkac/1.0"
                    )
                    .retrieve()
                    .body(Map.class);

        } catch (HttpClientErrorException.TooManyRequests e) {

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Location service is temporarily unavailable."
            );
        }

        if (response == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Location service returned no response."
            );
        }

        Map<?, ?> address =
                (Map<?, ?>) response.get("address");

        if (address == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Could not determine location."
            );
        }

        String city = getCity(address);

        if (city == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Could not determine city."
            );
        }

        String country =
                (String) address.get("country");

        String countryCode =
                address.get("country_code") != null
                        ? ((String) address.get("country_code"))
                        .toUpperCase()
                        : null;

        return new LocationResponse(
                city,
                country,
                countryCode,
                longitude,
                latitude
        );
    }

    public LocationResponseDto detectCityByCoordinates(double latitude, double longitude, String locale) {
        Map<?, ?> response;

        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.geoapify.com")
                            .path("/v1/geocode/reverse")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("lang", locale)
                            .queryParam("apiKey", apiKey)
                            .build()
                    )
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException.TooManyRequests e) {
            return LocationResponseDto.builder()
                    .httpStatus(HttpStatus.TOO_MANY_REQUESTS.value())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            return LocationResponseDto.builder()
                    .httpStatus(HttpStatus.BAD_GATEWAY.value())
                    .message(e.getMessage())
                    .build();
        }

        if (response == null) {
            return LocationResponseDto.builder()
                    .httpStatus(HttpStatus.BAD_GATEWAY.value())
                    .message("Bad gateway")
                    .build();
        }

        // 1. Geoapify wraps results inside a GeoJSON "features" array
        List<?> features = (List<?>) response.get("features");
        if (features == null || features.isEmpty()) {

            return LocationResponseDto.builder()
                    .httpStatus(HttpStatus.UNPROCESSABLE_CONTENT.value())
                    .message("Could not determine location from coordinates.")
                    .build();
        }

        // 2. Extract properties from the first feature result
        Map<?, ?> firstResult = (Map<?, ?>) features.get(0);
        Map<?, ?> properties = (Map<?, ?>) firstResult.get("properties");
        if (properties == null) {

            return LocationResponseDto.builder()
                    .httpStatus(HttpStatus.UNPROCESSABLE_CONTENT.value())
                    .message("Could not determine location properties.")
                    .build();
        }

        // 3. Extract properties safely using your custom fallback sequence
        String city = getCity(properties);
        if (city == null) {
            return LocationResponseDto.builder()
                    .httpStatus(HttpStatus.UNPROCESSABLE_CONTENT.value())
                    .message("Could not determine city name.")
                    .build();
        }

        String country = (String) properties.get("country");

        // 4. Geoapify provides 'country_code' directly in lowercase
        String countryCode = properties.get("country_code") != null
                ? ((String) properties.get("country_code")).toUpperCase()
                : null;

        return LocationResponseDto.builder()
                .message("Done")
                .locationResponse(new LocationResponse(
                        city,
                        country,
                        countryCode,
                        longitude,
                        latitude
                ))
                .httpStatus(HttpStatus.OK.value())
                .build();
    }


    private String getCity(Map<?, ?> address) {

        Object city = address.get("city");

        if (city != null) {
            return city.toString();
        }

        Object town = address.get("town");

        if (town != null) {
            return town.toString();
        }

        Object municipality =
                address.get("municipality");

        if (municipality != null) {
            return municipality.toString();
        }

        Object village =
                address.get("village");

        if (village != null) {
            return village.toString();
        }

        return null;
    }


    public LocationResponseDto searchCities(String query,String locale) {

        // Safety fallback initialization
        String cleanLanguage;

        if (locale != null && !locale.isBlank()) {
            // 1. Remove regional codes (e.g., converts "en-GB" or "de-DE" to "en" or "de")
            String baseLang = locale.split("-")[0].toLowerCase();

            // 2. Map 3-letter codes to standard 2-letter ISO variants
            if ("twi".equals(baseLang)) {
                cleanLanguage = "tw";
            } else if (baseLang.length() == 2) {
                cleanLanguage = baseLang;
            } else {
                cleanLanguage = "en";
            }
        }
        else{
            cleanLanguage = "en";
        }
         try{
             GeoapifyResponse response = restClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .scheme("https")
                             .host("api.geoapify.com")
                             .path("/v1/geocode/search")
                             .queryParam("text", query.trim())
                             .queryParam("type", "city")
                             .queryParam("limit", 10)
                             .queryParam("lang", cleanLanguage)
                             .queryParam("format", "json")
                             .queryParam("apiKey", apiKey)
                             .build()
                     )
                     .retrieve()
                     .body(GeoapifyResponse.class);

             return  LocationResponseDto.builder()
                     .locationResponseList(response.results()
                             .stream()
                             .filter(result -> result.city() != null)
                             .map(result -> new LocationResponse(
                                     result.city(),
                                     result.country(),
                                     result.country_code() != null
                                             ? result.country_code().toUpperCase()
                                             : null,
                                     result.lat(),
                                     result.lon()
                             ))
                             .toList())
                     .httpStatus(HttpStatus.OK.value())
                     .message("Done")
                     .build();
         }catch (Exception e){
            return LocationResponseDto.builder()
                     .httpStatus(HttpStatus.FORBIDDEN.value())
                     .locationResponse(null)
                     .message(e.getMessage())
                     .build();

         }

    }
}
