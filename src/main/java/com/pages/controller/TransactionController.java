package com.pages.controller;

import com.pages.dto.*;
import com.pages.exception.EntityNotFoundException;
import com.pages.model.AppUser;
import com.pages.service.Match_requestService;
import com.pages.service.PostService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trans")
public class TransactionController {

    private final PostService postService;
    private final Match_requestService matchRequestService;


    public TransactionController(PostService postService, Match_requestService matchRequestService) {
        this.postService = postService;
        this.matchRequestService = matchRequestService;

    }

    @GetMapping("/broadcast/{id}")
    public ResponseDto<Object> getAllPostsByUserId(String userId){
        return postService.postsByUser(Long.parseLong(userId));
    }



    @GetMapping("/broadcasts")
    public ResponseDto<Object> getAllPosts(
            @RequestParam Long userId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String lookingFor,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false, defaultValue = "0") Integer fromAge, // Defaults to 0 if missing
            @RequestParam(required = false, defaultValue = "0") Integer toAge,   // Defaults to 0 if missing
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page
    ) {
        try {
            // We pass a standard size constraint of 30 per page layout block directly to your service
            return postService.getDiscovery(userId, city, lookingFor, activity, fromAge, toAge, page, gender, 30);
        } catch (Exception e) {
            log.info("{}", e.getMessage());
            return null;
        }
    }


    @DeleteMapping("/broadcast/delete/{id}")
    public ResponseDto<Object> deletePost(Long postId){
     try{
         postService.deletePostById(postId);
         return ResponseDto.builder()
                 .message("Deleted")
                 .httpStatus(HttpStatus.OK)
                 .build();
     }catch (Exception ex){
         return ResponseDto.builder()
                 .message(ex.getMessage())
                 .httpStatus(HttpStatus.BAD_REQUEST)
                 .build();
     }
    }

    @PostMapping(value= "/broadcast",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<Object> addPost( @Valid   @ModelAttribute CreateMomentDto createMomentDto,
                                         @RequestPart MultipartFile media){
        if (media == null || media.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }

        if (media.getSize() > 30 * 1024 * 1024) {
            throw new IllegalArgumentException("Image must be smaller than 30MB");
        }

        String contentType = media.getContentType();

        if (!List.of("image/jpeg", "image/png", "image/webp","video/mp4","/video/webm")
                .contains(contentType)) {
            throw new IllegalArgumentException("Invalid image type");
        }
      return postService.save(createMomentDto,media);
    }

    @PatchMapping("/accept-request")
    public Boolean acceptMatchRequest(Long senderId,Long receiverId){
        return matchRequestService.acceptRequest(senderId,receiverId);
    }

}
