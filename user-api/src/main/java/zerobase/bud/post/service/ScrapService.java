package zerobase.bud.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.dto.SearchScrap;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.ScrapRepositoryQuerydslImpl;
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

    private final ScrapRepositoryQuerydslImpl scrapRepositoryQuerydsl;

    public boolean addScrap(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        var isAdd = new AtomicReference<Boolean>(false);

        scrapRepository.findByPostIdAndMemberId(postId, member.getId()).ifPresentOrElse(
                        scrap -> removeScrap(scrap, post),
                        () -> isAdd.set(addScrap(post, member)));

        postRepository.save(post);

        return isAdd.get();
    }

    public Long removeScrap(Long scrapId) {
        Scrap scrap = scrapRepository.findById(scrapId)
                .orElseThrow(() -> new BudException(NOT_FOUND_SCRAP_ID));

        removeScrap(scrap, scrap.getPost());

        return scrapId;
    }

    public Page<SearchScrap.Response> searchScrap(Member member, Pageable pageable) {

        Page<ScrapDto> scrapDtos =
                scrapRepositoryQuerydsl.findAllByMemberIdAndPostStatus(
                        member.getId(),
                        pageable
                );

        return new PageImpl<>(
                scrapDtos.stream()
                        .map(scrapDto -> SearchScrap.Response.of(scrapDto,
                                imageRepository.findAllByPostId(
                                        scrapDto.getPost().getId())))
                        .collect(Collectors.toList()),
                scrapDtos.getPageable(),
                scrapDtos.getTotalElements()
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
