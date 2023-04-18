package zerobase.bud.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import zerobase.bud.common.exception.MemberException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.user.domain.Follow;
import zerobase.bud.user.dto.FollowDto;
import zerobase.bud.user.dto.UserDto;
import zerobase.bud.user.repository.FollowRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final FollowRepository followRepository;

    private final MemberRepository memberRepository;

    private final PostRepository postRepository;

    @Transactional
    public Long follow(Long memberId, Member member) {
        Member targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_REGISTERED_MEMBER));

        if (member.equals(targetMember)) {
            throw new MemberException(ErrorCode.CANNOT_FOLLOW_YOURSELF);
        }

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

    public UserDto readProfile(Long userId, Member member) {
        Member targetMember = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_REGISTERED_MEMBER));

        Long numberOfFollowers = followRepository.countByTarget(member);
        Long numberOfFollows = followRepository.countByMember(member);
        Long numberOfPosts = postRepository.countByMember(member);
        boolean isReader = member.equals(targetMember);
        boolean isFollowing = followRepository.existsByTargetAndMember(targetMember, member);

        return UserDto.of(targetMember, isReader, isFollowing,
                numberOfFollowers, numberOfFollows, numberOfPosts);
    }

    public UserDto readMyProfile(Member member) {
        Long numberOfFollowers = followRepository.countByTarget(member);
        Long numberOfFollows = followRepository.countByMember(member);
        Long numberOfPosts = postRepository.countByMember(member);

        return UserDto.of(member, numberOfFollowers, numberOfFollows, numberOfPosts);
    }

    @Transactional(readOnly = true)
    public List<FollowDto> readMyFollowings(Member member) {
        return followRepository.findByMember(member)
                .map(follow -> FollowDto.of(follow.getTarget()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FollowDto> readMyFollowers(Member member) {
        return followRepository.findByTarget(member)
                .map(follow -> FollowDto.of(follow.getMember()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FollowDto> readFollowings(Long userId, Member reader) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_REGISTERED_MEMBER));

        return followRepository.findByMember(member)
                .map(follow -> toFollowDto(reader, follow.getTarget()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FollowDto> readFollowers(Long userId, Member reader) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_REGISTERED_MEMBER));

        return followRepository.findByTarget(member)
                .map(follow -> toFollowDto(reader, follow.getMember()))
                .collect(Collectors.toList());
    }

    private FollowDto toFollowDto(Member reader, Member profileMember) {
        return FollowDto.of(profileMember, Objects.equals(reader.getId(), profileMember.getId()),
                followRepository.existsByTargetAndMember(profileMember, reader));
    }
}
