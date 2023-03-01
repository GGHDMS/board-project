package com.study.projectboard.dto.request;


import com.study.projectboard.dto.ArticleCommentDto;
import com.study.projectboard.dto.UserAccountDto;
import lombok.Data;

@Data
public class ArticleCommentRequest {
    Long articleId;
    Long parentCommentId;
    String content;

    private ArticleCommentRequest(Long articleId, Long parentCommentId, String content) {
        this.articleId = articleId;
        this.parentCommentId = parentCommentId;
        this.content = content;
    }

    public static ArticleCommentRequest of(Long articleId, String content){ // 부모 댓글
        return ArticleCommentRequest.of(articleId, null,  content);
    }

    public static ArticleCommentRequest of(Long articleId, Long parentCommentId, String content){ //대댓글
        return new ArticleCommentRequest(articleId, parentCommentId, content);
    }

    public ArticleCommentDto toDto(UserAccountDto userAccountDto){
        return ArticleCommentDto.of(
                articleId,
                userAccountDto,
                parentCommentId,
                content
        );
    }



}
