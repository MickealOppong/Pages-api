package com.pages.dto;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class LoginResponse {

    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    private String profileImage;
    private TokenDto tokenDto;

}

