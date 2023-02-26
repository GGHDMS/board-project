package com.study.projectboard.service;

import com.study.projectboard.domain.Article;
import com.study.projectboard.domain.Hashtag;
import com.study.projectboard.domain.UserAccount;
import com.study.projectboard.domain.constant.SearchType;
import com.study.projectboard.dto.ArticleDto;
import com.study.projectboard.dto.ArticleWithCommentsDto;
import com.study.projectboard.repository.ArticleRepository;
import com.study.projectboard.repository.HashtagRepository;
import com.study.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleService {

    private final HashtagService hashtagService;
    private final ArticleRepository articleRepository;
    private final UserAccountRepository userAccountRepository;
    private final HashtagRepository hashtagRepository; // 리팩토링 여지가 있다.

    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return articleRepository.findAll(pageable).map(ArticleDto::from);
        }

        Page<ArticleDto> map = Page.empty();
        switch (searchType) {
            case TITLE:
                map = articleRepository.findByTitleContaining(searchKeyword, pageable).map(ArticleDto::from);
                break;
            case CONTENT:
                map = articleRepository.findByContentContaining(searchKeyword, pageable).map(ArticleDto::from);
                break;
            case ID:
                map = articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(ArticleDto::from);
                break;
            case NICKNAME:
                map = articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(ArticleDto::from);
                break;
            case HASHTAG:
                map = articleRepository.findByHashtagNames(Arrays.stream(searchKeyword.split(" ")).collect(Collectors.toList()), pageable).map(ArticleDto::from);
                break;
        }

        return map;
    }

    public ArticleWithCommentsDto getArticleWithComments(Long articleId) {
        return articleRepository.findById(articleId)
                .map(ArticleWithCommentsDto::from)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
    }

    public ArticleDto getArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .map(ArticleDto::from)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
    }

    @Transactional
    public void saveArticle(ArticleDto articleDto) {
        UserAccount userACcount = userAccountRepository.getReferenceById(articleDto.getUserAccountDto().getUserId());
        Set<Hashtag> hashtags = renewHashtagsFromContent(articleDto.getContent());

        Article article = articleDto.toEntity((userACcount));
        article.addHashtags(hashtags);

        articleRepository.save(article);
    }


    @Transactional
    public void updateArticle(Long articleId, ArticleDto dto) {
        try {
            Article article = articleRepository.getReferenceById(articleId);
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId());
            if (article.getUserAccount().equals(userAccount)) { // 현재 게시글 작성자와 실제 작성자가 일치 할 때
                if (dto.getTitle() != null) {
                    article.setTitle(dto.getTitle());
                }
                if (dto.getContent() != null) {
                    article.setContent(dto.getContent());
                }
                Set<Long> hashtagIds = article.getHashtags().stream()
                        .map(Hashtag::getId)
                        .collect(Collectors.toUnmodifiableSet());

                article.clearHashtags(); // 기존 해시태그를 삭제한다. 해시태그 DB 에는 영향을 주지 않는다. @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}) 때문에
                articleRepository.flush(); // 변경 내용을 DB에 반영, 반영 하지 않으면 renewHashtagsFromContent 의 결과 와 충동 할 수도 , 이런 식으로 하면 지울 필요 없는 것도 지우고 다시 추가 될 수도 있다.

                hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles); //해시태그를 어떠한 게시글에서도 사용하지 않을 때 delete

                Set<Hashtag> hashtags = renewHashtagsFromContent(dto.getContent());
                article.addHashtags(hashtags);
            }
        } catch (EntityNotFoundException e) {
            log.warn("게시글 업데이트 실패, 게시글 수정에 필요한 정보를 찾을수 없습니다 - {}", e.getLocalizedMessage());
        }

    }

    @Transactional
    public void deleteArticle(Long articleId, String userId) {
        Article article = articleRepository.getReferenceById(articleId);
        Set<Long> hashtagIds = article.getHashtags().stream()
                .map(Hashtag::getId)
                .collect(Collectors.toUnmodifiableSet());

        articleRepository.deleteByIdAndUserAccount_UserId(articleId, userId); // 게시글을 먼저 삭제한 후 해시태그를 검사해야 된다.
        articleRepository.flush(); //DB 에 반영

        hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles); //해시태그를 어떠한 게시글에서도 사용하지 않을 때 delete
    }


    public long getArticleCount() {
        return articleRepository.count();
    }


    public Page<ArticleDto> searchArticlesViaHashtag(String hashtagName, Pageable pageable) {
        if (hashtagName == null || hashtagName.isBlank()) {
            return Page.empty(pageable);
        }
        return articleRepository.findByHashtagNames(List.of(hashtagName), pageable).map(ArticleDto::from); // 추후 에서는 여러 해시태그 로 검색 할 수도 있다.
    }

    public List<String> getHashtags() {
        return hashtagRepository.findAllHashtagNames(); // TODO: HashtagService 로 이동을 고려해 보는것이 좋을듯.
    }

    private Set<Hashtag> renewHashtagsFromContent(String content) {
        Set<String> hashtagNamesInContent = hashtagService.parseHashtagNames(content); // 본문에서 찾아낸 해시태그
        Set<Hashtag> hashtags = hashtagService.findHashtagsByNames(hashtagNamesInContent); // 실제 db 에 존재하는 해시태그 -> entity
        Set<String> existingHashtagNames = hashtags.stream()
                .map(Hashtag::getHashtagName)
                .collect(Collectors.toUnmodifiableSet()); // entity 를 string 으로 변환

        hashtagNamesInContent.forEach(newHashtagName -> {
            if (!existingHashtagNames.contains(newHashtagName)) { //새로 들어온 해시태그가 실제 DB에 존재하지 않으면 DB에 추가한다.
                hashtags.add(Hashtag.of(newHashtagName));
            }
        });

        return hashtags;
    }


}
