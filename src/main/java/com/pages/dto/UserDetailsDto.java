package com.pages.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class UserDetailsDto {

    private Long userId;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate date_of_birth;
    private String city;
    private String country;
    private String preference;
    private String username;

    //dating information
    private String drinking;
    private String pets;
    private String smoking;
    private String profession;
    private String education;
    private String language;
    private String height;

    private String lookingFor;
    @Column(length = 1024)
    private String aboutMe;
    @Column(length = 1024)
    private String aboutThem;
    private MultipartFile media;
    private String profileImage;
    private boolean hasMatchRequest ;
    private boolean rulesAccepted;
    private List<PostDto> postDtoList;

    private String planningStyle;
    private String socialEnergy;
    private String chronoType;
}
