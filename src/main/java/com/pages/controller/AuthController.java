package com.pages.controller;

import com.pages.dto.*;
import com.pages.exception.InvalidOperationException;
import com.pages.model.AppUser;
import com.pages.model.RefreshToken;
import com.pages.service.AppUserDetailsService;
import com.pages.service.GlobalAddressService;
import com.pages.service.RefreshTokenService;
import com.pages.service.TokenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final GlobalAddressService globalAddressService;



    public AuthController(AuthenticationManager authenticationManager, AppUserDetailsService userDetailsService,
                          RefreshTokenService refreshTokenService, TokenService tokenService, PasswordEncoder passwordEncoder, GlobalAddressService globalAddressService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.globalAddressService = globalAddressService;
    }

    @PutMapping("/reset")
    public ResponseDto<Object> verify(@RequestBody ResetPasswordDto dto ){
        return userDetailsService.resetPasswordViaProfileFacts(dto);
    }



    @PostMapping("/register")
    public ResponseDto<Object> createCustomer(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest){

        if(userDetailsService.alreadyExist(userRegistrationRequest.getEmail())){
            throw new InvalidOperationException("Email already in use, choose a different email.");

        }
        if(!userRegistrationRequest.isTermsChecked()){
           throw new InvalidOperationException("You must agree to terms and conditions.");

        }

        // save user data
        AppUser newUser=
                new AppUser(userRegistrationRequest.getFirstName(), userRegistrationRequest.getLastName(), userRegistrationRequest.getEmail(), userRegistrationRequest.getGender(),
                        userRegistrationRequest.getDob(), passwordEncoder.encode(userRegistrationRequest.getPassword()), userRegistrationRequest.getCity()
                        , userRegistrationRequest.getCountry(), userRegistrationRequest.getCountryCode(), true);

        userDetailsService.add(newUser);

        // save location data to global address
        globalAddressService.addToAddress(userRegistrationRequest.getCity(), userRegistrationRequest.getCountry(),userRegistrationRequest.getCountryCode(),
                userRegistrationRequest.getLatitude(),userRegistrationRequest.getLongitude());

        return ResponseDto.builder()
                .message("User registered successfully.")
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/login")
    public ResponseDto<?> login(@RequestBody UserCredentials credentials){

        try{
            Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(credentials.username(),credentials.password());
            Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);

            if(authenticationResponse.isAuthenticated()){

                SecurityContextHolder.getContext().setAuthentication(authenticationResponse);

                AppUser appUser = userDetailsService.getAppUserByUsername(credentials.username());

                RefreshToken refreshToken =refreshTokenService.createToken(appUser);


                TokenDto tokenDto = TokenDto.builder()
                        .token(tokenService.token(authenticationResponse).orElse(null))
                        .refreshToken(refreshToken.getRefreshToken())
                        .expiredAt(refreshToken.getExpiredAt())
                        .issuedAt(refreshToken.getIssuedAt())
                        .build();
                LoginResponse userDto = LoginResponse.builder()
                        .tokenDto(tokenDto)
                        .username(appUser.getUsername())
                        .userId(appUser.getId())
                        .firstName(appUser.getFirstName())
                        .lastName(appUser.getLastName())
                        .build();

                return ResponseDto.builder()
                        .data(userDto)
                        .message("Success")
                        .httpStatus(HttpStatus.ACCEPTED)
                        .build();
            }
            return ResponseDto.builder()
                    .data(null)
                    .message("Could not authenticate username or password")
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .build();

        }catch (UsernameNotFoundException ex) {
            return ResponseDto.builder()
                    .data(null)
                    .message("Username does not exist.")
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .build();

        } catch (BadCredentialsException ex) {
            return ResponseDto.builder()
                    .data(null)
                    .message("Incorrect password.")
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .build();

        } catch (Exception ex) {
            return ResponseDto.builder()
                    .data(null)
                    .message("An unexpected error occurred.")
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .build();
        }

    }

    @DeleteMapping("/logout")
    public ResponseEntity<Boolean> logoutUser(String refreshToken){
        try{
            return ResponseEntity.ok(refreshTokenService.removeToken(refreshToken));
        }catch (Exception  e){
            return ResponseEntity.badRequest().body(false);
        }
    }


}

