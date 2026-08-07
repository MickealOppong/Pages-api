package com.pages.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
    private String content;
    private String status;
    private Long likesCount;
    private Integer viewsCount;
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
