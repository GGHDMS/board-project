package com.study.projectboard.dto.response;

import com.study.projectboard.dto.ArticleCommentDto;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ArticleCommentResponse {
    Long id;
    String content;
    LocalDateTime createdAt;
    String email;
    String nickname;
    String userId;

    public ArticleCommentResponse(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.email = email;
        this.nickname = nickname;
        this.userId = userId;
    }

    public static ArticleCommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId) {
        return new ArticleCommentResponse(id, content, createdAt, email, nickname, userId);
    }

    public static ArticleCommentResponse from(ArticleCommentDto dto) {
        String nickname = dto.getUserAccountDto().getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.getUserAccountDto().getUserId();
        }

        return new ArticleCommentResponse(
                dto.getId(),
                dto.getContent(),
                dto.getCreatedAt(),
                dto.getUserAccountDto().getEmail(),
                nickname,
                dto.getUserAccountDto().getUserId()
        );
    }
}
