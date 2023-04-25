package zerobase.bud.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

import static zerobase.bud.util.Constants.PROFILES;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;

    private final AwsS3Api awsS3Api;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_REGISTERED_MEMBER));
    }

    public boolean modifyInfo(Member member, MultipartFile file, String nickname, String introduceMessage, String job) {
        if(memberRepository.findByNickname(nickname).isPresent()) {
            throw new BudException(ErrorCode.ALREADY_USING_NICKNAME);
        }
        if(!ObjectUtils.isEmpty(nickname))
            member.setNickname(nickname);
        if(!ObjectUtils.isEmpty(file))
            member.setProfileImg(awsS3Api.uploadImage(file, PROFILES));
        if(!ObjectUtils.isEmpty(introduceMessage))
            member.setIntroduceMessage(introduceMessage);
        if(!ObjectUtils.isEmpty(job))
            member.setJob(job);

        memberRepository.save(member);
        return true;
    }

    public List<String> getLevelImage(Member member) {
        long level = member.getLevel().getLevelNumber();
        List<String> levelArray = new ArrayList<>();

        for(int i=1; i<=10; i++) {
            if(i <= level) {
                levelArray.add(awsS3Api.getImageUrl("levels/lv" + i + ".png"));
            } else {
                levelArray.add(awsS3Api.getImageUrl("levels/lv" + i + "L.png"));
            }
        }
        return levelArray;
    }
}
