package com.pages.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class LocationResponseDto {

    private String message;
    private int httpStatus;
    private LocationResponse locationResponse;
    private List<LocationResponse> locationResponseList;
}
