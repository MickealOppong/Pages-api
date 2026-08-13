package com.pages.service;

import com.pages.dto.*;
import com.pages.enums.Request_Status;
import com.pages.exception.EntityNotFoundException;
import com.pages.exception.InvalidOperationException;
import com.pages.model.*;
import com.pages.repository.*;
import com.pages.util.Media;
import com.pages.util.Notification;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.RollbackOn;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepo appUserRepo;
    private final AppUserRoleRepo appUserRoleRepo;
    private final MediaService mediaService;
    private final PostRepo postRepo;
    private final Match_requestRepo matchRequestRepo;
    private final PasswordEncoder passwordEncoder;
    private final ChatMessageRepo chatMessageRepo;
    private final PostViewRepo postViewRepo;


    @Autowired
    private NotificationRepo notificationRepo;




    public AppUserDetailsService(AppUserRepo appUserRepo, AppUserRoleRepo appUserRoleRepo, MediaService mediaService,
                                 PostRepo postRepo, Match_requestRepo matchRequestRepo, @Lazy PasswordEncoder  passwordEncoder, ChatMessageRepo chatMessageRepo, PostViewRepo postViewRepo){
        this.appUserRepo = appUserRepo;
        this.appUserRoleRepo = appUserRoleRepo;
        this.mediaService = mediaService;

        this.postRepo = postRepo;
        this.matchRequestRepo = matchRequestRepo;
        this.passwordEncoder = passwordEncoder;
        this.chatMessageRepo = chatMessageRepo;
        this.postViewRepo = postViewRepo;
    }


    @Override
    public UserDetails loadUserByUsername( String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(""));
        return new User(appUser.getUsername(),appUser.getPassword(),appUser.getAuthorities());
    }




    @Transactional
    public ResponseDto<Object> deleteMyAccount(Jwt jwt) {

        String username = jwt.getSubject();

        AppUser user = appUserRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        Long userId = user.getId();

        // delete post media here (or after commit if external storage)
        List<String> mediaToDelete=postRepo.findMediaPathsByUserId(userId);

        //log.info("{}",mediaToDelete);
        //delete profile media
       Media profileMedia = user.getMedia();

        //delete all notifications
        notificationRepo.deleteALlByRecipientOrTriggerUser(userId);

        //delete all messages
        chatMessageRepo.deleteUserChatHistory(userId);

        //delete all matches
        matchRequestRepo.deleteUserMatchHistory(userId);


        //post view delete
        postViewRepo.deleteByPostOwnerId(userId);
        postViewRepo.deleteByViewerId(userId);

        //delete post
        postRepo.deleteAllPostByAppUserId(userId);



        appUserRepo.delete(user);


        try {

            if(!mediaToDelete.isEmpty()) {
                mediaService.deleteAll(mediaToDelete);
            }

            if(profileMedia!=null) {
                mediaService.delete(profileMedia.getPath());
            }
        }catch (IOException ignored){

        }



        return ResponseDto.builder()
                .httpStatus(HttpStatus.OK)
                .message("Account deleted successfully")
                .data(true)
                .build();
    }



    public void changePassword(String username, ChangePasswordDto dto) {

        AppUser user = appUserRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User profile not found."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("The current password you entered is incorrect.");
        }


        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match.");
        }


        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as your old password.");
        }

        String encryptedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(encryptedPassword);
       appUserRepo.save(user); // Triggers transactional database flush
    }

    public void add(AppUser appUser){

        AppUserRole userRole = appUserRoleRepo.findByRole("ROLE_USER").orElseThrow(()->new EntityNotFoundException("Role does not exist"));
        //set user Role
        appUser.setUserRole(Set.of(userRole));
        appUserRepo.save(appUser);
    }

    // MVP update to check online status
    @Transactional
    public void updateLastActive(Long userId) {
        appUserRepo.findById(userId).ifPresent(user -> {
            user.setLastActive(Instant.now());
            appUserRepo.save(user); // Flushes the updated timestamp to the database
        });
    }

    public AppUser getAppUserByUsername(String username){
        return appUserRepo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username+ " "+"does not exist"));
    }

    public boolean alreadyExist(String username){
       return appUserRepo.findByUsername(username).isPresent();
    }

    public ResponseDto<Object> getAppUserProfile(Long userId,Long requestUserId){

       try{
          Optional<AppUser> appUser = appUserRepo.findById(userId);
         //  Optional<AppUser> requestUser = appUserRepo.findById(requestUserId);



          if(appUser.isPresent()){
                AppUser user = appUser.get();
             // AppUser requestor = requestUser.get();

              List<PostDto> postDtoList = new ArrayList<>();

              //check whether there is a match between userid and requestor userId

          boolean match= matchRequestRepo.existsMatchRequest(userId,Request_Status.ACCEPTED.name());

          boolean matchRequest= matchRequestRepo.existsMatchRequest(userId, Request_Status.PENDING.name());


          for(Post post:  postRepo.findAllByAppUserId(userId)){

            MediaDto media = mediaService.getImageAndType(post.getMedia().getId());

              PostDto postDto = PostDto.builder()
                      .postId(post.getPostId())
                      .content(post.getContent())
                      .type(post.getType())
                      .isViewAllowed(match)
                      .visibility(post.getVisibility())
                      .media(media.getMedia())
                      .mediaOrientation(media.getOrientation())
                      .build();
              postDtoList.add(postDto);
          }

          //profile image
              String media = Optional.of(user)
                      .map(AppUser::getMedia)
                      .map(Media::getId)
                      .map(mediaService::getImage)
                      .orElse(null);

              UserDetailsDto userDetailsDto = UserDetailsDto.builder()
                      .userId(user.getId())
                      .firstName(user.getFirstName())
                      .lastName(user.getLastName())
                      .language(user.getLanguage())
                      .city(user.getCity())
                      .aboutMe(user.getAboutMe())
                      .aboutThem(user.getAboutThem())
                      .country(user.getCountry())
                      .pets(user.getPets())
                      .date_of_birth(user.getDate_of_birth())
                      .drinking(user.getDrinking())
                      .smoking(user.getSmoking())
                      .education(user.getEducation())
                      .hasMatchRequest(matchRequest)
                      .height(user.getHeight())
                      .gender(user.getGender())
                      .preference(user.getPreference())
                      .lookingFor(user.getLookingFor())
                      .postDtoList(postDtoList)
                      .profileImage(media)
                      .profession(user.getProfession())
                      .build();




              return ResponseDto.builder()
                      .data(userDetailsDto)
                      .message("Success")
                      .httpStatus(HttpStatus.OK)
                      .build();
          }


           return ResponseDto.builder()
                   .data(null)
                   .message("User does not exist")
                   .httpStatus(HttpStatus.NOT_FOUND)
                   .build();
       }catch (Exception e){
           return ResponseDto.builder()
                   .data(null)
                   .message(e.getMessage())
                   .httpStatus(HttpStatus.BAD_REQUEST)
                   .build();
       }
    }


    public boolean resetPasswordViaProfileFacts(ResetPasswordDto dto) {
        // 1. Locate account by email input criteria
        AppUser user = appUserRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No registered account found with that email address."));

        // 2. THE SECURITY GUARD: Validate unguessable profile values instead of sending an email link
        boolean cityMatches = user.getCity().equalsIgnoreCase(dto.getLocation().trim());
        boolean dobMatches = user.getDate_of_birth().equals(dto.getDate_of_birth());

        if (!cityMatches || !dobMatches) {
            throw new IllegalArgumentException("Verification parameters do not match our system security logs.");
        }

        // 3. Confirm target password string layouts align perfectly
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password fields do not match.");
        }

        // 4. Cryptographically hash new token string and flush row permanently
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        appUserRepo.save(user);
        return true;
    }

    public ResponseDto<Object> getAppUserProfile(Long userId){

        try{
            Optional<AppUser> appUser = appUserRepo.findById(userId);



            if(appUser.isPresent()){
                AppUser user = appUser.get();

                UserDetailsDto userDetailsDto = UserDetailsDto.builder()
                        .userId(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .language(user.getLanguage())
                        .city(user.getCity())
                        .aboutMe(user.getAboutMe())
                        .aboutThem(user.getAboutThem())
                        .country(user.getCountry())
                        .pets(user.getPets())
                        .date_of_birth(user.getDate_of_birth())
                        .drinking(user.getDrinking())
                        .smoking(user.getSmoking())
                        .height(user.getHeight())
                        .education(user.getEducation())
                        .gender(user.getGender())
                        .preference(user.getPreference())
                        .lookingFor(user.getLookingFor())
                        .username(user.getUsername())
                        .postDtoList(null)
                        .profileImage(user.getMedia()!=null? mediaService.getImage(user.getMedia().getId()):null)
                        .profession(user.getProfession())
                        .build();

                return ResponseDto.builder()
                        .data(userDetailsDto)
                        .message("Success")
                        .httpStatus(HttpStatus.OK)
                        .build();
            }


            return ResponseDto.builder()
                    .data(null)
                    .message("User does not exist")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }catch (Exception e){
            return ResponseDto.builder()
                    .data(null)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    public ResponseDto<Object> getAppUserData(Long userId){

        try{
            Optional<AppUser> appUser = appUserRepo.findById(userId);

            if(appUser.isPresent()){
                AppUser user = appUser.get();

                UserDetailsDto userDetailsDto = UserDetailsDto.builder()
                        .userId(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .profileImage(user.getMedia()!=null? mediaService.getImage(user.getMedia().getId()):null)
                        .build();

                return ResponseDto.builder()
                        .data(userDetailsDto)
                        .message("Success")
                        .httpStatus(HttpStatus.OK)
                        .build();
            }


            return ResponseDto.builder()
                    .data(null)
                    .message("User does not exist")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }catch (Exception e){
            return ResponseDto.builder()
                    .data(null)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    public AppUser getAppUserById(Long userId){
       return appUserRepo.findById(userId).orElseThrow(()->new UsernameNotFoundException(userId+" not found"));
    }

    public boolean updateUserDetails(UserDetailsDto userDetailsDto){

        try{
           Optional<AppUser> retrievedUser= appUserRepo.findById(userDetailsDto.getUserId());
            if(retrievedUser.isPresent()){
                AppUser appUser = retrievedUser.get();


                   if(userDetailsDto.getMedia()!=null){
                       Media media = mediaService.saveImage(userDetailsDto.getMedia());
                       appUser.setMedia(media);
                   }
                   if(userDetailsDto.getGender()!=null){
                       appUser.setGender(userDetailsDto.getGender());
                   }
                   if(userDetailsDto.getCountry()!=null){
                       appUser.setCountry(userDetailsDto.getCountry());
                   }
                   if(userDetailsDto.getCity()!=null){
                       appUser.setCity(userDetailsDto.getCity());
                   }
                   if(userDetailsDto.getAboutThem()!=null){
                       appUser.setAboutThem(userDetailsDto.getAboutThem());
                   }
                   if(userDetailsDto.getAboutMe()!=null){
                       appUser.setAboutMe(userDetailsDto.getAboutMe());
                   }
                   if(userDetailsDto.getPreference()!=null){
                       appUser.setPreference(userDetailsDto.getPreference());
                   }
                   if(userDetailsDto.getEducation()!=null){
                       appUser.setEducation(userDetailsDto.getEducation());
                   }
                   if(userDetailsDto.getLanguage()!=null){
                       appUser.setLanguage(userDetailsDto.getLanguage());
                   }
                   if(userDetailsDto.getProfession()!=null){
                       appUser.setProfession(userDetailsDto.getProfession());
                   }

                   if(userDetailsDto.getSmoking()!=null){
                       appUser.setSmoking(userDetailsDto.getSmoking());
                   }
                if(userDetailsDto.getHeight()!=null){
                    appUser.setHeight(userDetailsDto.getHeight());
                }

                   if(userDetailsDto.getDrinking()!=null){
                       appUser.setDrinking(userDetailsDto.getDrinking());
                   }

                   if(userDetailsDto.getPets()!=null){
                       appUser.setPets(userDetailsDto.getPets());
                   }
                   if(userDetailsDto.getLookingFor()!=null){
                       appUser.setLookingFor(userDetailsDto.getLookingFor());
                   }
                   if(userDetailsDto.getDrinking()!=null){
                       appUser.setDrinking(userDetailsDto.getDrinking());
                   }

                   updateLastActive(appUser.getId());

                appUserRepo.save(appUser);
                return true;

            }
            return false;

        }catch (Exception e){
           return false;
        }
    }
}
