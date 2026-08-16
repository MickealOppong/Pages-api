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

        // 2. Fetch Pure Post History Data (Genuine Shared Moments / Hobbies)
        Set<String> userAAll = new HashSet<>(postRepo.findDistinctTypesByUserId(userAId));
        Set<String> userBAll = new HashSet<>(postRepo.findDistinctTypesByUserId(userBId));

        // Calculate pure shared moments BEFORE modifying collections with core traits or synergy keys
        Set<String> pureSharedMoments = new HashSet<>(userAAll);
        pureSharedMoments.retainAll(userBAll);

        // Each matching post history/moment type yields 10 points
        int score = 0;

        // 1. Pure Shared Moments: High Behavioral Weight (+10 per shared item, capped at 40)
        int momentsScore = pureSharedMoments.size() * 10;
        score += Math.min(momentsScore, 40);

        // 3. VIRTUALIZATION: Inject static attributes with specialized type prefixes
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

        // 4. COMPUTE TOTAL SHARED COMPONENTS OVERLAP (Including matching profile attributes)
        Set<String> sharedHobbies = new HashSet<>(userAAll);
        sharedHobbies.retainAll(userBAll);

        // Calculate specific points for matched profile attributes
        if (userA.getLookingFor() != null && sharedHobbies.contains("LOOKING_FOR_" + userA.getLookingFor().trim().toUpperCase())) score += 10;
        if (userA.getPets() != null && sharedHobbies.contains("PET_" + userA.getPets().trim().toUpperCase())) score += 10;
        if (userA.getEducation() != null && sharedHobbies.contains("EDUCATION_" + userA.getEducation().trim().toUpperCase())) score += 5;
        if (userA.getDrinking() != null && sharedHobbies.contains("DRINKING_" + userA.getDrinking().trim().toUpperCase())) score += 10;
        if (userA.getSmoking() != null && sharedHobbies.contains("SMOKING_" + userA.getSmoking().trim().toUpperCase())) score += 15;

        // 5. EVALUATE COMPLEMENTARY OPPOSITES SYNERGY
        // 1. Social Energy (Introvert / Extrovert / Ambivert / Flexible)
        if (userA.getSocialEnergy() != null && userB.getSocialEnergy() != null) {
            String seA = userA.getSocialEnergy().trim().toUpperCase();
            String seB = userB.getSocialEnergy().trim().toUpperCase();

            // CASE A: Both are flexible (Shared harmony)
            if ((seA.equals("AMBIVERT") || seA.equals("FLEXIBLE") || seA.equals("IT DEPENDS")) &&
                    (seB.equals("AMBIVERT") || seB.equals("FLEXIBLE") || seB.equals("IT DEPENDS"))) {
                score += 20; // 10 for shared trait + 10 for synergy balance
                sharedHobbies.add("SYNERGY_OPPOSITE_SOCIAL_ENERGY");
            }
            // CASE B: Classic polar opposites (Complementary chemistry)
            else if ((seA.equals("INTROVERT") && seB.equals("EXTROVERT")) ||
                    (seA.equals("EXTROVERT") && seB.equals("INTROVERT"))) {
                score += 15;
                sharedHobbies.add("SYNERGY_OPPOSITE_SOCIAL_ENERGY");
            }
        }

        // 2. Planning Style (Structured / Spontaneous / Flexible / It Depends)
        if (userA.getPlanningStyle() != null && userB.getPlanningStyle() != null) {
            String psA = userA.getPlanningStyle().trim().toUpperCase();
            String psB = userB.getPlanningStyle().trim().toUpperCase();

            // CASE A: Both are flexible adaptors
            if ((psA.equals("FLEXIBLE") || psA.equals("IT_DEPENDS") || psA.equals("IT DEPENDS")) &&
                    (psB.equals("FLEXIBLE") || psB.equals("IT_DEPENDS") || psB.equals("IT DEPENDS"))) {
                score += 20;
                sharedHobbies.add("SYNERGY_OPPOSITE_PLANNING_STYLE");
            }
            // CASE B: Classic polar opposites
            else if ((psA.equals("STRUCTURED PLANNER") && psB.equals("SPONTANEOUS")) ||
                    (psA.equals("SPONTANEOUS") && psB.equals("STRUCTURED PLANNER"))) {
                score += 15;
                sharedHobbies.add("SYNERGY_OPPOSITE_PLANNING_STYLE");
            }
        }

        // 3. Chronotype (Early Bird / Night Owl / Flexible)
        if (userA.getChronoType() != null && userB.getChronoType() != null) {
            String ctA = userA.getChronoType().trim().toUpperCase();
            String ctB = userB.getChronoType().trim().toUpperCase();

            // CASE A: Both have flexible internal clocks
            if ((ctA.equals("FLEXIBLE") || ctA.equals("IT DEPENDS")) &&
                    (ctB.equals("FLEXIBLE") || ctB.equals("IT DEPENDS"))) {
                score += 20;
                sharedHobbies.add("SYNERGY_OPPOSITE_CHRONO_TYPE");
            }
            // CASE B: Classic polar opposites
            else if ((ctA.equals("NIGHT OWL") && ctB.equals("EARLY BIRD")) ||
                    (ctA.equals("EARLY BIRD") && ctB.equals("NIGHT OWL"))) {
                score += 15;
                sharedHobbies.add("SYNERGY_OPPOSITE_CHRONO_TYPE");
            }
        }

        // 6. COMPUTE UNIQUE REMAINDERS
        // Elements added to sharedHobbies after intersections must be cleared out of unique maps
        Set<String> userAUnique = new HashSet<>(userAAll);
        userAUnique.removeAll(sharedHobbies);

        Set<String> userBUnique = new HashSet<>(userBAll);
        userBUnique.removeAll(sharedHobbies);

        return MatchResultDto.builder()
                .compatibilityScore(Math.min(score, 100))
                .sharedHobbies(sharedHobbies)
                .userAUniqueHobbies(userAUnique)
                .userBUniqueHobbies(userBUnique)
                .build();
    }



}

