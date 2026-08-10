package com.pages.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PostDto{

    private Long postId;
    private Long userId;
    private String firstName;

    @NotNull(message = "Please provide a description with max 250 words")
    @NotBlank(message = "Please provide a description with max 250 words")
    private String content;

    private String status;
    private Long likesCount;
    private Integer viewsCount;

    @NotNull(message = "Field cannot be null")
    @NotBlank(message = "Field cannot be empty")
    private String visibility;
    private LocalDate date_of_birth;
    private String type;
    private String media;
    private String profileImage;
    private String location;
    private Instant createdAt;
    private Instant modifiedAt;
    private boolean isViewAllowed;
    private String lookingFor;
    private String mediaOrientation;
    private String height;
    private String profession;


}
