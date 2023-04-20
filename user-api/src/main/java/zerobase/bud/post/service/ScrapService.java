package zerobase.bud.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.domain.Scrap;
import zerobase.bud.post.dto.ScrapDto;
import zerobase.bud.post.repository.ScrapRepository;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_SCRAP_ID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public boolean isScrap(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        var isAdd = new AtomicReference<Boolean>(false);

        scrapRepository.findByPostIdAndMemberId(postId, member.getId()).ifPresentOrElse(
                        scrap -> removeScrap(scrap, post),
                        () -> isAdd.set(addScrap(post, member)));

        postRepository.save(post);

        return isAdd.get();
    }

    @Transactional
    public Long removeScrap(Long scrapId) {
        Scrap scrap = scrapRepository.findById(scrapId)
                .orElseThrow(() -> new BudException(NOT_FOUND_SCRAP_ID));

        removeScrap(scrap, scrap.getPost());

        return scrapId;
    }

    @Transactional(readOnly = true)
    public Slice<ScrapDto> searchScrap(Pageable pageable, Member member) {

        Slice<Scrap> scraps = scrapRepository.findAllByMemberIdAndPostPostStatus(pageable,
                member.getId(), PostStatus.ACTIVE);

        return new SliceImpl<>(
                scraps.stream()
                        .map(scrap -> ScrapDto.of(scrap,
                                imageRepository.findAllByPostId(scrap.getPost().getId())))
                        .collect(Collectors.toList()),
                scraps.getPageable(),
                scraps.hasNext()
        );
    }

    private void removeScrap(Scrap scrap, Post post) {
        scrapRepository.delete(scrap);
        post.scrapCountDown();
    }

    private boolean addScrap(Post post, Member member) {
        scrapRepository.save(Scrap.builder()
                .post(post)
                .member(member)
                .build());

        post.scrapCountUp();

        return true;
    }
}
