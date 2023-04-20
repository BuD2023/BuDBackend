package zerobase.bud.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.dto.NotificationInfoDto;
import zerobase.bud.notification.service.NotificationInfoService;
import zerobase.bud.post.dto.SearchMyPagePost;
import zerobase.bud.post.dto.SearchScrap;
import zerobase.bud.post.service.PostService;
import zerobase.bud.post.service.ScrapService;
import zerobase.bud.user.dto.FollowDto;
import zerobase.bud.user.dto.UserDto;
import zerobase.bud.user.service.UserService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ScrapService scrapService;
    private final NotificationInfoService notificationInfoService;
    private final PostService postService;

    @PostMapping("/{userId}/follows")
    private ResponseEntity<URI> follow(@PathVariable Long userId,
                                       @AuthenticationPrincipal Member member) {
        userService.follow(userId, member);
        return ResponseEntity.created(URI.create("/users/" + userId)).build();
    }

    @GetMapping("/follows")
    private ResponseEntity<List<FollowDto>> readMyFollowings(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyFollowings(member));
    }

    @GetMapping("/{userId}/follows")
    private ResponseEntity<List<FollowDto>> readFollowings(@PathVariable Long userId,
                                                           @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readFollowings(userId, member));
    }

    @GetMapping("/followers")
    private ResponseEntity<List<FollowDto>> readMyFollowers(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyFollowers(member));
    }

    @GetMapping("/{userId}/followers")
    private ResponseEntity<List<FollowDto>> readFollowers(@PathVariable Long userId,
                                                          @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readFollowers(userId, member));
    }

    @GetMapping("/{userId}")
    private ResponseEntity<UserDto> readProfile(@PathVariable Long userId,
                                                @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readProfile(userId, member));
    }

    @GetMapping
    private ResponseEntity<UserDto> readMyProfile(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyProfile(member));
    }

    @GetMapping("/posts/scraps")
    public ResponseEntity<Page<SearchScrap.Response>> searchScraps(
            @PageableDefault(size = 5, sort = "POST_DATE", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal Member member
    ) {

        return ResponseEntity.ok(scrapService.searchScrap(member, pageable));
    }

    @DeleteMapping("/posts/scraps/{scrapId}")
    public ResponseEntity<Long> removeScrap(@PathVariable Long scrapId) {
        return ResponseEntity.ok(scrapService.removeScrap(scrapId));
    }

    @GetMapping("/{myPageUserId}/posts")
    public ResponseEntity<Page<SearchMyPagePost.Response>> searchMyPosts(
            @AuthenticationPrincipal Member member,
            @PathVariable Long myPageUserId,
            @PageableDefault(size = 5, sort = "DATE", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(postService.searchMyPagePosts(member,
                myPageUserId, pageable));
    }

    @PutMapping("/{userId}/notification-info")
    public ResponseEntity<String> changeNotificationAvailable(
        @RequestBody NotificationInfoDto notificationInfoDto,
        @AuthenticationPrincipal Member member
    ){
        return ResponseEntity.ok(notificationInfoService.changeNotificationAvailable(
            notificationInfoDto, member
        ));
    }
}
