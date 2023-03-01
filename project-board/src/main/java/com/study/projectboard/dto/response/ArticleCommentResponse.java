package com.study.projectboard.dto.response;

import com.study.projectboard.dto.ArticleCommentDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;


@Data
public class ArticleCommentResponse { // 댓글에 대한 정보
    Long id;
    String content;
    LocalDateTime createdAt;
    String email;
    String nickname;
    String userId;
    Long parentCommentId;
    Set<ArticleCommentResponse> childComments;


    public ArticleCommentResponse(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId, Long parentCommentId, Set<ArticleCommentResponse> childComments) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.email = email;
        this.nickname = nickname;
        this.userId = userId;
        this.parentCommentId = parentCommentId;
        this.childComments = childComments;
    }

    public static ArticleCommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId) {
        return ArticleCommentResponse.of(id, content, createdAt, email, nickname, userId, null);
    }

    public static ArticleCommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId, Long parentCommentId) {
        Comparator<ArticleCommentResponse> childCommentComparator = Comparator // childComments 에 정렬된 set 이 들어가야 되기 때문에 사용했다. set 의 규칙을 정할 수 있다.
                .comparing(ArticleCommentResponse::getCreatedAt) // 기본 오른차순, 내림하고 싶으면 .reversed
                .thenComparing(ArticleCommentResponse::getId);
        return new ArticleCommentResponse(id, content, createdAt, email, nickname, userId, parentCommentId, new TreeSet<>(childCommentComparator));
    }

    public static ArticleCommentResponse from(ArticleCommentDto dto) {
        String nickname = dto.getUserAccountDto().getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.getUserAccountDto().getUserId();
        }

        return ArticleCommentResponse.of( //Dto 로 부터 생성
                dto.getId(),
                dto.getContent(),
                dto.getCreatedAt(),
                dto.getUserAccountDto().getEmail(),
                nickname,
                dto.getUserAccountDto().getUserId(),
                dto.getParentCommentId()
        );
    }

    public boolean hasParentComment() {
        return getParentCommentId() != null;
    }
}
