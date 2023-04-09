package zerobase.bud.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.MemberException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.user.domain.Follow;
import zerobase.bud.user.repository.FollowRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final FollowRepository followRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public Long follow(Long memberId, Member member) {
        Member targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        Optional<Follow> optionalFollow =
                followRepository.findByTargetAndAndMember(targetMember, member);

        if (optionalFollow.isPresent()) {
            followRepository.delete(optionalFollow.get());
        } else {
            followRepository.save(Follow.builder()
                    .target(targetMember)
                    .member(member)
                    .build()
            );
        }
        return targetMember.getId();
    }
}
