package com.study.projectboard.service;

import com.study.projectboard.domain.Article;
import com.study.projectboard.domain.UserAccount;
import com.study.projectboard.domain.constant.SearchType;
import com.study.projectboard.dto.ArticleDto;
import com.study.projectboard.dto.ArticleWithCommentsDto;
import com.study.projectboard.repository.ArticleRepository;
import com.study.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserAccountRepository userAccountRepository;

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
                map = articleRepository.findByHashtag("#" + searchKeyword, pageable).map(ArticleDto::from);
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
        articleRepository.save(articleDto.toEntity(userACcount));
    }

    @Transactional
    public void updateArticle(Long articleId, ArticleDto dto) {
        try {
            Article article = articleRepository.getReferenceById(articleId);
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId());
            if (article.getUserAccount().equals(userAccount)){
                if (dto.getTitle() != null) {
                    article.setTitle(dto.getTitle());
                }
                if (dto.getContent() != null) {
                    article.setContent(dto.getContent());
                }
                article.setHashtag(dto.getHashtag());
            }
        } catch (EntityNotFoundException e){
            log.warn("게시글 업데이트 실패, 게시글 수정에 필요한 정보를 찾을수 없습니다 - {}", e.getLocalizedMessage());
        }

    }

    @Transactional
    public void deleteArticle(Long articleId, String userId) {
        articleRepository.deleteByIdAndUserAccount_UserId(articleId, userId);
    }


    public long getArticleCount(){
        return articleRepository.count();
    }


    public Page<ArticleDto> searchArticlesViaHashtag(String hashtag, Pageable pageable) {
        if (hashtag == null || hashtag.isBlank()){
            return Page.empty(pageable);
        }
        return articleRepository.findByHashtag(hashtag, pageable).map(ArticleDto::from);
    }

    public List<String> getHashtags() {
        return articleRepository.findAllDistinctHashtags();
    }

}
