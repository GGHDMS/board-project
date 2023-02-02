package com.study.projectboard.service;

import com.study.projectboard.domain.type.SearchType;
import com.study.projectboard.dto.ArticleDto;
import com.study.projectboard.dto.ArticleWithCommentsDto;
import com.study.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        return Page.empty();
    }

    public ArticleWithCommentsDto getArticle(Long articleId) {
        return null;
    }


    @Transactional
    public void saveArticle(ArticleDto articleDto) {
//        articleRepository.save(Article.of("title", "contents", "hashtag"));
    }

    @Transactional
    public void updateArticle(ArticleDto dto) {

    }

    @Transactional
    public void deleteArticle(Long articleId) {

    }
}
