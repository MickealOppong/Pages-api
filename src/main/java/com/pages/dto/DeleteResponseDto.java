package com.pages.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class DeleteResponseDto {

    private boolean deleted;
    private String message;
    private HttpStatus httpStatus;
}
