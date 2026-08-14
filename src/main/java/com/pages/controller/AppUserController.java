package com.pages.controller;

import com.pages.dto.*;
import com.pages.exception.EntityNotFoundException;
import com.pages.model.AppUser;
import com.pages.service.AppUserDetailsService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/users")
public class AppUserController {

    private final AppUserDetailsService userDetailsService;

    public AppUserController(AppUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/profile/{id}")
    public ResponseDto<Object> UserProfileData(Long userId){
        return userDetailsService.getAppUserProfile(userId);
    }

    @GetMapping("/user/{id}")
    public ResponseDto<Object> UserData(Long userId){
        return userDetailsService.getAppUserData(userId);
    }


    @GetMapping("/view/profile/{id}")
    public ResponseDto<Object> UserData(Long userId,Long requestorUserId){
        return userDetailsService.getAppUserProfile(userId,requestorUserId);
    }

    @PatchMapping(value = "/update-data",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Boolean> updateUserData(@ModelAttribute UserDetailsUpdateDto userDetailsDto){
         return ResponseEntity.ok(userDetailsService.updateUserDetails(userDetailsDto));
    }


    @PutMapping("/change-password")
    public ResponseDto<Object> updatePassword(@AuthenticationPrincipal Jwt jwt, @RequestBody ChangePasswordDto changePasswordDto) {
        try {

            userDetailsService.changePassword(jwt.getSubject(), changePasswordDto);

            return ResponseDto.builder()
                    .data(null)
                    .message("Password successfully updated.")
                    .httpStatus(HttpStatus.OK)
                    .build();

        } catch (IllegalArgumentException e) {
            return ResponseDto.builder()
                    .data(true)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            return ResponseDto.builder()
                    .data(false)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseDto<Object> deleteMyAccount(@AuthenticationPrincipal Jwt jwt) {
        return userDetailsService.deleteMyAccount(jwt);
    }

        @PutMapping("/accept-rules")
        public ResponseDto<Boolean> acceptRules(@AuthenticationPrincipal Jwt jwt) {
         return userDetailsService.acceptRulesOnLogin(jwt);
        }



}
