package com.pages.service;

import com.pages.dto.MatchResultDto;
import com.pages.exception.EntityNotFoundException;
import com.pages.model.AppUser;
import com.pages.repository.AppUserRepo;
import com.pages.repository.PostRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class MatchEngineService {

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private AppUserRepo userRepository;

    public MatchResultDto evaluateFullCompatibility(Long userAId, Long userBId) {
        // 1. Fetch Core Profile Data
        AppUser userA = userRepository.findById(userAId)
                .orElseThrow(() -> new EntityNotFoundException("User A not found"));
        AppUser userB = userRepository.findById(userBId)
                .orElseThrow(() -> new EntityNotFoundException("User B not found"));

        // 2. Fetch Post History Data
        Set<String> userAAll = new HashSet<>(postRepo.findDistinctTypesByUserId(userAId));
        Set<String> userBAll = new HashSet<>(postRepo.findDistinctTypesByUserId(userBId));

        // 3. VIRTUALIZATION: Inject attributes with specialized type prefixes
        if (userA.getLookingFor() != null) userAAll.add("LOOKING_FOR_" + userA.getLookingFor().trim().toUpperCase());
        if (userB.getLookingFor() != null) userBAll.add("LOOKING_FOR_" + userB.getLookingFor().trim().toUpperCase());

        if (userA.getPets() != null) userAAll.add("PET_" + userA.getPets().trim().toUpperCase());
        if (userB.getPets() != null) userBAll.add("PET_" + userB.getPets().trim().toUpperCase());

        if (userA.getEducation() != null) userAAll.add("EDUCATION_" + userA.getEducation().trim().toUpperCase());
        if (userB.getEducation() != null) userBAll.add("EDUCATION_" + userB.getEducation().trim().toUpperCase());

        if (userA.getDrinking() != null) userAAll.add("DRINKING_" + userA.getDrinking().trim().toUpperCase());
        if (userB.getDrinking() != null) userBAll.add("DRINKING_" + userB.getDrinking().trim().toUpperCase());

        if (userA.getSmoking() != null) userAAll.add("SMOKING_" + userA.getSmoking().trim().toUpperCase());
        if (userB.getSmoking() != null) userBAll.add("SMOKING_" + userB.getSmoking().trim().toUpperCase());

        // 4. COMPUTE SHARED COMPONENTS (Center Overlap)
        Set<String> sharedHobbies = new HashSet<>(userAAll);
        sharedHobbies.retainAll(userBAll);

        // 5. COMPUTE UNIQUE REMAINDERS (Outer Left & Right Panels)
        Set<String> userAUnique = new HashSet<>(userAAll);
        userAUnique.removeAll(sharedHobbies);

        Set<String> userBUnique = new HashSet<>(userBAll);
        userBUnique.removeAll(sharedHobbies);

        // 6. Calculate Dynamic Score: Query the exact prefixed strings inside the shared collection
        int score = 0;
        if (userA.getLookingFor() != null && sharedHobbies.contains("LOOKING_FOR_" + userA.getLookingFor().trim().toUpperCase())) score += 40;
        if (userA.getPets() != null && sharedHobbies.contains("PET_" + userA.getPets().trim().toUpperCase())) score += 15;
        if (userA.getEducation() != null && sharedHobbies.contains("EDUCATION_" + userA.getEducation().trim().toUpperCase())) score += 15;
        if (userA.getDrinking() != null && sharedHobbies.contains("DRINKING_" + userA.getDrinking().trim().toUpperCase())) score += 15;
        if (userA.getSmoking() != null && sharedHobbies.contains("SMOKING_" + userA.getSmoking().trim().toUpperCase())) score += 15;


        // Define helper arrays inside evaluation blocks
        if (userA.getSocialEnergy() != null && userB.getSocialEnergy() != null) {
            String seA = userA.getSocialEnergy().trim().toUpperCase();
            String seB = userB.getSocialEnergy().trim().toUpperCase();

            if ((seA.equals("INTROVERT") && seB.equals("EXTROVERT")) ||
                    (seA.equals("EXTROVERT") && seB.equals("INTROVERT"))) {
                score += 20;
                sharedHobbies.add("SYNERGY_OPPOSITE_SOCIAL_ENERGY");
            }
        }

        if (userA.getPlanningStyle() != null && userB.getPlanningStyle() != null) {
            String psA = userA.getPlanningStyle().trim().toUpperCase();
            String psB = userB.getPlanningStyle().trim().toUpperCase();

            if ((psA.equals("STRUCTURED PLANNER") && psB.equals("SPONTANEOUS")) ||
                    (psA.equals("SPONTANEOUS") && psB.equals("STRUCTURED PLANNER"))) {
                score += 20;
                sharedHobbies.add("SYNERGY_OPPOSITE_PLANNING_STYLE");
            }
        }

        if (userA.getChronoType() != null && userB.getChronoType() != null) {
            String psA = userA.getChronoType().trim().toUpperCase();
            String psB = userB.getChronoType().trim().toUpperCase();

            if ((psA.equals("NIGHT OWL") && psB.equals("EARLY BIRD")) ||
                    (psA.equals("EARLY BIRD") && psB.equals("NIGHT OWL"))) {
                score += 20;
                sharedHobbies.add("SYNERGY_OPPOSITE_CHRONO_TYPE");
            }
        }



        // Add 10 points for every standard shared moment category remaining
        long sharedMomentsCount = sharedHobbies.stream()
                .filter(tag -> !tag.startsWith("LOOKING_FOR_") &&
                        !tag.startsWith("PET_") &&
                        !tag.startsWith("EDUCATION_") &&
                        !tag.startsWith("DRINKING_") &&
                        !tag.startsWith("SMOKING_"))
                .count();
        score += (int) (sharedMomentsCount * 10);

        return MatchResultDto.builder()
                .compatibilityScore(Math.min(score, 100))
                .sharedHobbies(sharedHobbies)
                .userAUniqueHobbies(userAUnique)
                .userBUniqueHobbies(userBUnique)
                .build();
    }


}

