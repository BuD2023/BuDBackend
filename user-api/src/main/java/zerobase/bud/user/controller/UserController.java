package zerobase.bud.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.bud.domain.Member;
import zerobase.bud.post.dto.ScrapDto;
import zerobase.bud.post.service.ScrapService;
import zerobase.bud.user.dto.FollowDto;
import zerobase.bud.user.dto.UserDto;
import zerobase.bud.user.service.UserService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final ScrapService scrapService;

    @PostMapping("/users/{userId}/follows")
    private ResponseEntity<URI> follow(@PathVariable Long userId,
                                       @AuthenticationPrincipal Member member) {
        userService.follow(userId, member);
        return ResponseEntity.created(URI.create("/users/" + userId)).build();
    }

    @GetMapping("/users/follows")
    private ResponseEntity<List<FollowDto>> readMyFollowings(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyFollowings(member));
    }

    @GetMapping("/users/{userId}/follows")
    private ResponseEntity<List<FollowDto>> readFollowings(@PathVariable Long userId,
                                                           @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readFollowings(userId, member));
    }

    @GetMapping("/users/followers")
    private ResponseEntity<List<FollowDto>> readMyFollowers(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyFollowers(member));
    }

    @GetMapping("/users/{userId}/followers")
    private ResponseEntity<List<FollowDto>> readFollowers(@PathVariable Long userId,
                                                          @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readFollowers(userId, member));
    }

    @GetMapping("/users/{userId}")
    private ResponseEntity<UserDto> readProfile(@PathVariable Long userId,
                                                @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readProfile(userId, member));
    }

    @GetMapping("/users")
    private ResponseEntity<UserDto> readMyProfile(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyProfile(member));
    }

    @GetMapping("/users/posts/scraps")
    public ResponseEntity<Slice<ScrapDto>> searchScraps(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal Member member
    ) {

        return ResponseEntity.ok(scrapService.searchScrap(pageable, member));
    }

    @DeleteMapping("/users/posts/scraps/{scrapId}")
    public ResponseEntity<Long> removeScrap(@PathVariable Long scrapId) {
        return ResponseEntity.ok(scrapService.removeScrap(scrapId));
    }
}
