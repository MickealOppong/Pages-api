package com.pages.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaDto {

    private String media;
    private String orientation;
}
