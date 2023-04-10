package zerobase.bud.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.domain.Member;
import zerobase.bud.user.dto.UserDto;
import zerobase.bud.user.service.UserService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/users/{userId}/follows")
    private ResponseEntity follow(@PathVariable Long userId,
                                  @AuthenticationPrincipal Member member){
        userService.follow(userId, member);
        return ResponseEntity.created(URI.create("/users/"+userId)).build();
    }

    @GetMapping("/users/{userId}")
    private ResponseEntity readProfile(@PathVariable Long userId,
                                       @AuthenticationPrincipal Member member){
        return ResponseEntity.ok(userService.readProfile(userId, member));
    }

    @GetMapping("/users")
    private ResponseEntity readMyProfile(@AuthenticationPrincipal Member member){
        return ResponseEntity.ok(userService.readMyProfile(member));
    }
}
