package zerobase.bud.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.domain.Member;
import zerobase.bud.member.service.MemberService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/modifyInfo")
    public ResponseEntity<Boolean> modifyMemberInfo(@AuthenticationPrincipal Member member,
                                              @RequestPart(required = false) MultipartFile file,
                                              @RequestPart(required = false) String nickname,
                                              @RequestPart(required = false) String introduceMessage,
                                              @RequestPart String job) {
        return ResponseEntity.ok(memberService.modifyInfo(member, file, nickname, introduceMessage, job));
    }

    @GetMapping("/getLevelImage")
    public ResponseEntity<List<String>> getLevelImage(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(memberService.getLevelImage(member));
    }

    @GetMapping("/random-image")
    public ResponseEntity<String> getRandomProfileImage(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(memberService.getRandomProfileImage(member));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawMember(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(memberService.withdrawMember(member));
    }
}
