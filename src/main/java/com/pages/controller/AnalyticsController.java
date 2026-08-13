package com.pages.controller;

import com.pages.service.AnalyticsService;
import com.pages.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AppUserDetailsService appUserDetailsService;


    @PatchMapping("/view")
    public ResponseEntity<Void> recordView(
            @RequestParam Long postId,
            @AuthenticationPrincipal Jwt jwt // Pulls secure context principal
    ) {
        // Mock method placeholder: Extract your internal AppUser Long database ID from your UserDetails service implementation
        Long viewerId = getUserIdFromPrincipal(jwt);
        // Dispatches the background tracking sequence instantly
        analyticsService.recordPostView(postId, viewerId);

        // 204 No Content tells your React app the operation was accepted without holding up the UI thread
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromPrincipal(Jwt jwt) {
        // Implementation detail depending on your Security Config (e.g., custom user wrapper entity return)
       String username= jwt.getSubject();
       return appUserDetailsService.getAppUserByUsername(username).getId();
    }
}
