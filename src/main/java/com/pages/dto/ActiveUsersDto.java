package com.pages.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveUsersDto {

    private Long userId;
    private String firstName;
    private String dob;
    private String profession;

}
