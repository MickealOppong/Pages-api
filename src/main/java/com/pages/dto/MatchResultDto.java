package com.pages.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
public class MatchResultDto {

    private int compatibilityScore;
    private Set<String> sharedHobbies;        // Center Overlap Circle
    private Set<String> userAUniqueHobbies;   // Left Circle (You)
    private Set<String> userBUniqueHobbies;   // Right Circle (Them)

}
