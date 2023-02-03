package com.study.projectboard.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleCommentUpdateDto {
    private String content; // 본문
    private LocalDateTime modifiedAt;
    private String modifiedBy;

    public static ArticleCommentUpdateDto of(String content, LocalDateTime modifiedAt, String modifiedBy) {
        return new ArticleCommentUpdateDto(content, modifiedAt, modifiedBy);
    }

    private ArticleCommentUpdateDto(String content, LocalDateTime modifiedAt, String modifiedBy) {
        this.content = content;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
    }


}
