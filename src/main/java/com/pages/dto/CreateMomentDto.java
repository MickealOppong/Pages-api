package com.pages.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomentDto {

    @NotNull(message = "Please provide user id")
    private Long userId;

    @NotBlank(message = "Caption is required")
    @Size(max = 500, message = "Caption cannot exceed 250 characters")
    private String content;

    @NotBlank(message = "Visibility is required")
    private String visibility;

    @NotBlank(message = "Moment type is required")
    private String activity;


    @NotBlank(message = "Media orientation is required,add a media file")
    private String mediaOrientation;
}
