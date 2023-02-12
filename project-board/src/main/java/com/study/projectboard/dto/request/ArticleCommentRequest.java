package com.study.projectboard.dto.request;


import com.study.projectboard.dto.ArticleCommentDto;
import com.study.projectboard.dto.UserAccountDto;
import lombok.Data;

@Data
public class ArticleCommentRequest {
    Long articleId;
    String content;

    private ArticleCommentRequest(Long articleId, String content) {
        this.articleId = articleId;
        this.content = content;
    }

    public static ArticleCommentRequest of(Long articleId, String content){
        return new ArticleCommentRequest(articleId, content);
    }

    public ArticleCommentDto toDto(UserAccountDto userAccountDto){
        return ArticleCommentDto.of(
                articleId,
                userAccountDto,
                content
        );
    }



}
