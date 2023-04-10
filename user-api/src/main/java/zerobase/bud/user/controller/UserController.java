package zerobase.bud.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.domain.Member;
import zerobase.bud.user.service.UserService;

import java.net.URI;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/users/{userId}/follows")
    private ResponseEntity follow(@PathVariable Long userId,
                                  @AuthenticationPrincipal Member member) {
        userService.follow(userId, member);
        return ResponseEntity.created(URI.create("/users/" + userId)).build();
    }

    @GetMapping("/users/follows")
    private ResponseEntity readMyFollowings(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyFollowings(member));
    }

    @GetMapping("/users/{userId}/follows")
    private ResponseEntity readFollowings(@PathVariable Long userId,
                                          @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readFollowings(userId, member));
    }

    @GetMapping("/users/followers")
    private ResponseEntity readMyFollowers(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyFollowers(member));
    }

    @GetMapping("/users/{userId}/followers")
    private ResponseEntity readFollowers(@PathVariable Long userId,
                                         @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readFollowers(userId, member));
    }

    @GetMapping("/users/{userId}")
    private ResponseEntity readProfile(@PathVariable Long userId,
                                       @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readProfile(userId, member));
    }

    @GetMapping("/users")
    private ResponseEntity readMyProfile(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(userService.readMyProfile(member));
    }
}
