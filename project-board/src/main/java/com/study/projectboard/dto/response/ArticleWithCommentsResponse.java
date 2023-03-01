package com.study.projectboard.dto.response;

import com.study.projectboard.dto.ArticleCommentDto;
import com.study.projectboard.dto.ArticleWithCommentsDto;
import com.study.projectboard.dto.HashtagDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class ArticleWithCommentsResponse { // 게시글 + 댓글 정보
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
                organizeChildComments(dto.getArticleCommentDtos())
//                dto.getArticleCommentDtos().stream()
//                        .map(ArticleCommentResponse::from)
//                        .collect(Collectors.toCollection(LinkedHashSet::new)) // 이 코드는 댓글과 대댓글에 관련 없이 그냥 전달
        );
    }

    private static Set<ArticleCommentResponse> organizeChildComments(Set<ArticleCommentDto> dtos) { // db는 댓글과, 대댓글 이 하나의 테이블에 있다 그렇기 때문에 계층적 구조로 바꿔줘야함 우린 dto 가 계층을 바꾸게 할거임, service 내부에서 로직을 구현하는 방법도 있다.
        Map<Long, ArticleCommentResponse> map = dtos.stream()
                .map(ArticleCommentResponse::from)
                .collect(Collectors.toMap(ArticleCommentResponse::getId, Function.identity())); // id로 접근 할 수 있게 mapping 해준다.

        map.values().stream()
                .filter(ArticleCommentResponse::hasParentComment) // 자식 댓글만 가져온다.
                .forEach(comment -> {
                    ArticleCommentResponse parentComment = map.get(comment.getParentCommentId()); // 자식 comment 의 부모 댓글 id 를 가져온다.
                    parentComment.childComments.add(comment);
                });

        return map.values().stream()
                .filter(comment -> !comment.hasParentComment()) // 부모 comment 인가?
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                                .comparing(ArticleCommentResponse::getCreatedAt) // 부모 댓글들을 정렬
                                .reversed() // 내림차순 정렬을 하기 위해서
                                .thenComparing(ArticleCommentResponse::getId) // 오름차순 정렬
                        )
                ));
    }

}
