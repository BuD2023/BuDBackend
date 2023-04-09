package zerobase.bud.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.domain.Member;
import zerobase.bud.user.service.UserService;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/users/{userId}")
    private ResponseEntity follow(@PathVariable Long userId,
                                  @AuthenticationPrincipal Member member){
        Long id = userService.follow(userId, member);
        return ResponseEntity.ok().build();
    }
}
