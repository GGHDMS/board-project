package com.study.projectboard.service;

import com.study.projectboard.domain.ArticleComment;
import com.study.projectboard.dto.ArticleCommentDto;
import com.study.projectboard.repository.ArticleCommentRepository;
import com.study.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleCommentsService {

    private final ArticleCommentRepository articleCommentRepository;
    private final ArticleRepository articleRepository;

    public List<ArticleCommentDto> searchArticleComments(Long articleId) {
        return articleCommentRepository.findByArticle_Id(articleId).stream().map(ArticleCommentDto::from).collect(Collectors.toList());
    }

    @Transactional
    public void saveArticleComment(ArticleCommentDto dto) {
        try {
            articleCommentRepository.save(dto.toEntity(articleRepository.getReferenceById(dto.getArticleId())));
        } catch (EntityNotFoundException e) {
            log.info("댓글 저장 실패, 댓글의 게시글을 찾을 수 없습니다 - dto: {}", dto);
        }
    }

    @Transactional
    public void updateArticleComment(ArticleCommentDto dto) {
        try {
            ArticleComment articleComment = articleCommentRepository.getReferenceById(dto.getId());
            if (dto.getContent() != null) {
                articleComment.setContent(dto.getContent());
            }
        } catch (EntityNotFoundException e) {
            log.info("댓글 업데이트 실패, 댓글을 찾을 수 없습니다 - dto: {}", dto);
        }

    }

    @Transactional
    public void deleteArticleComment(Long articleCommentId) {
        articleCommentRepository.deleteById(articleCommentId);
    }
}
