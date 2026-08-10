package com.pages.controller;

import com.pages.dto.MatchRequestDto;
import com.pages.dto.ResponseDto;
import com.pages.interfaces.RequiresPublicPost;
import com.pages.service.Match_requestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/request")
public class MatchRequestsController {

    private final Match_requestService matchRequestsService;


    public MatchRequestsController(Match_requestService matchRequestsService) {
        this.matchRequestsService = matchRequestsService;
    }



    @RequiresPublicPost
    @PostMapping("/like")
    public ResponseDto<Object> addLike(Long senderId,Long receiverId,Long postId){
        return matchRequestsService.addToLike(senderId,receiverId,postId);
    }

    @PatchMapping ("/accept/{matchId}")
    public boolean acceptRequest(Long matchId, Long currentUserId) {
        return matchRequestsService.acceptRequestById(matchId,currentUserId);
    }

    @GetMapping("/matches")
    public List<MatchRequestDto> MyMatches(Long userId){
        return matchRequestsService.getMyMatchesDashboard(userId);
    }

    @GetMapping("/match")
    public MatchRequestDto MyMatch(Long matchId,Long currentUserId){
        return matchRequestsService.getMatchById(matchId,currentUserId);
    }


    @GetMapping("/likes")
    public List<MatchRequestDto> MyLikes(Long userId){
        return matchRequestsService.getUserLikes(userId);
    }

    @DeleteMapping("/remove/{matchId}")
    public ResponseDto<Object> rejectRequest(Long matchId){
      return matchRequestsService.deleteLike(matchId);
    }


}
