package com.pages.controller;

import com.pages.dto.DeleteResponseDto;
import com.pages.dto.FilterDto;
import com.pages.dto.PostDto;
import com.pages.dto.ResponseDto;
import com.pages.enums.NotificationType;
import com.pages.interfaces.RequiresPublicPost;
import com.pages.model.Post;
import com.pages.service.Match_requestService;
import com.pages.service.NotificationService;
import com.pages.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;

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

    @PostMapping("/broadcast")
    public ResponseDto<Object> addPost(String activity, String content, String visibility,MultipartFile image, String mediaOrientation,String userId){
      return postService.save(activity,content,visibility,image,mediaOrientation,Long.parseLong(userId));
    }

    @PatchMapping("/accept-request")
    public Boolean acceptMatchRequest(Long senderId,Long receiverId){
        return matchRequestService.acceptRequest(senderId,receiverId);
    }

}
