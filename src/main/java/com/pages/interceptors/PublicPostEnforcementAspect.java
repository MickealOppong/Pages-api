package com.pages.interceptors;

import com.nimbusds.jwt.JWT;
import com.pages.dto.ResponseDto;
import com.pages.enums.Visibility;
import com.pages.exception.InsufficientPublicPresenceException;
import com.pages.model.AppUser;
import com.pages.model.Post;
import com.pages.repository.AppUserRepo;
import com.pages.repository.PostRepo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
@Slf4j
@Aspect
@Component
public class PublicPostEnforcementAspect {

    @Autowired
    private PostRepo postRepository;
    @Autowired
    private AppUserRepo appUserRepo;

    // Keep the signature completely clean of controller-specific annotations
    @Before("@annotation(com.pages.interfaces.RequiresPublicPost)")
    public void verifyUserHasPublicPresence(JoinPoint joinPoint) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new SecurityException("Unauthorized access or invalid token.");
        }

        Jwt jwt  = (Jwt )authentication.getPrincipal();
        if(jwt==null){
            throw new SecurityException("Unauthorized access or invalid token.");
        }
        String username = jwt.getSubject();
        AppUser appUser = appUserRepo.findByUsername(username).orElse(null);



        if (appUser != null) {


            // Check if they already have an existing public post in the database
            boolean hasPublicPost = postRepository.existsByAppUserIdAndVisibility(appUser.getId(), Visibility.PUBLIC.name());

            // CRITICAL FIX: If they do NOT have a public post yet, check what they are doing right now
            if (!hasPublicPost) {
                throw new InsufficientPublicPresenceException(
                        "Before you can send match request, your profile needs at least one Public Moment.\n" +
                                "\n" +
                                "Public Moments help people discover who you are.\n" +
                                "\n" +
                                "Match-only Moments are only visible after you've connected, so they don't unlock connection features.");
                }

                // If they have no public posts, and this action isn't creating one (e.g., they are liking, or posting privately), BLOCK IT.

            }
        }

}

