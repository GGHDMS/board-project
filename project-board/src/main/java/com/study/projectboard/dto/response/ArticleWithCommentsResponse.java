package com.study.projectboard.dto.response;

import com.study.projectboard.dto.ArticleWithCommentsDto;
import com.study.projectboard.dto.HashtagDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ArticleWithCommentsResponse {
    Long id;
    String title;
    String content;
    Set<String> hashtags;
    LocalDateTime createdAt;
    String email;
    String nickname;
    String userId;

    Set<ArticleCommentResponse> articleCommentsResponse;

    private ArticleWithCommentsResponse(Long id, String title, String content, Set<String> hashtags, LocalDateTime createdAt, String email, String nickname, String userId, Set<ArticleCommentResponse> articleCommentsResponse) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.hashtags = hashtags;
        this.createdAt = createdAt;
        this.email = email;
        this.nickname = nickname;
        this.userId = userId;
        this.articleCommentsResponse = articleCommentsResponse;
    }

    public static ArticleWithCommentsResponse of(Long id, String title, String content, Set<String> hashtags, LocalDateTime createdAt, String email, String nickname, String userId, Set<ArticleCommentResponse> articleCommentResponses) {
        return new ArticleWithCommentsResponse(id, title, content, hashtags, createdAt, email, nickname, userId, articleCommentResponses);
    }

    public static ArticleWithCommentsResponse from(ArticleWithCommentsDto dto) {
        String nickname = dto.getUserAccountDto().getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.getUserAccountDto().getUserId();
        }

        return new ArticleWithCommentsResponse(
                dto.getId(),
                dto.getTitle(),
                dto.getContent(),
                dto.getHashtagDtos().stream()
                        .map(HashtagDto::getHashtagName)
                        .collect(Collectors.toUnmodifiableSet()),
                dto.getCreatedAt(),
                dto.getUserAccountDto().getEmail(),
                nickname,
                dto.getUserAccountDto().getUserId(),
                dto.getArticleCommentDtos().stream()
                        .map(ArticleCommentResponse::from)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
}
