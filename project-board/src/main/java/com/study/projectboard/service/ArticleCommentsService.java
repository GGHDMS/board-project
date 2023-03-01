package com.study.projectboard.service;

import com.study.projectboard.domain.Article;
import com.study.projectboard.domain.ArticleComment;
import com.study.projectboard.domain.UserAccount;
import com.study.projectboard.dto.ArticleCommentDto;
import com.study.projectboard.repository.ArticleCommentRepository;
import com.study.projectboard.repository.ArticleRepository;
import com.study.projectboard.repository.UserAccountRepository;
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
    private final UserAccountRepository userAccountRepository;

    public List<ArticleCommentDto> searchArticleComments(Long articleId) {
        return articleCommentRepository.findByArticle_Id(articleId).stream().map(ArticleCommentDto::from).collect(Collectors.toList());
    }

    @Transactional
    public void saveArticleComment(ArticleCommentDto dto) {
        try {
            Article article = articleRepository.getReferenceById(dto.getArticleId());
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId());
            ArticleComment articleComment = dto.toEntity(article, userAccount);

            if (dto.getParentCommentId() != null) { // 대댓글인지 확인
                ArticleComment parentComment = articleCommentRepository.getReferenceById(dto.getParentCommentId());
                parentComment.addChildComment(articleComment);
            } else {
                articleCommentRepository.save(articleComment); // 부모 댓글일 때는 직접 save 호출
            }
        } catch (EntityNotFoundException e) {
            log.info("댓글 저장 실패, 댓글 저장에 필요한 정보를 찾을수 없습니다. - {}", e.getLocalizedMessage());
        }
    }

    @Transactional
    public void updateArticleComment(ArticleCommentDto dto) {
        try {
            ArticleComment articleComment = articleCommentRepository.getReferenceById(dto.getId());
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId());
            if(articleComment.getUserAccount().equals(userAccount)){
                if (dto.getContent() != null) {
                    articleComment.setContent(dto.getContent());
                }
            }
        } catch (EntityNotFoundException e) {
            log.info("댓글 업데이트 실패, 댓글 수정에 필요한 정보를 찾을수 없습니다. - {}", e.getLocalizedMessage());
        }

    }

    @Transactional
    public void deleteArticleComment(Long articleCommentId, String userId) {
        articleCommentRepository.deleteByIdAndUserAccount_UserId(articleCommentId, userId);
    }
}
