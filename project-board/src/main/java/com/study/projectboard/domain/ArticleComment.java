package com.study.projectboard.domain;

import lombok.*;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;


@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(columnList = "content"),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy"),
})
@Entity
public class ArticleComment extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Article article; //게시글(ID)

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @ToString.Exclude //userAccount 가 무조건 있다
    private UserAccount userAccount;

    @ToString.Exclude
    @OrderBy("createdAt ASC") // 대댓글은 먼저 생성된 것부터 순차적으로 정렬
    @OneToMany(mappedBy = "parentCommentId", cascade = CascadeType.ALL) //부모가 지워지면 자식이 전부 지워지게
    private Set<ArticleComment> childComments = new LinkedHashSet<>(); //jpa 에서는 final 을 추천하지 않는다.

    @Setter
    @Column(updatable = false)
    private Long parentCommentId; //부모 댓글 ID ArticleComment parentComment 이런식으로 하면 양방향이 가능, 여기서는 단방향으로 설정

    @Setter
    @Column(nullable = false, length = 500)
    private String content; // 본문

    private ArticleComment(Article article, UserAccount userAccount, Long parentCommentId, String content) {
        this.article = article;
        this.userAccount = userAccount;
        this.parentCommentId = parentCommentId;
        this.content = content;
    }

    public static ArticleComment of(Article article, UserAccount userAccount, String content) { // of 를 사용 하기 때문에 domain 이 변하더라도 영향 범위를 최소화 할 수 있다.
        return new ArticleComment(article, userAccount, null, content); // 처음 작성시 부모 댓글과의 연관 관계를 작성하지 않겠다.
    }

    public void addChildComment(ArticleComment childComment) {
        childComment.setParentCommentId(this.getId()); // 자식에 부모 id 초기화
        this.getChildComments().add(childComment); // 부모 set 에 자식 articleComment 추가
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleComment)) return false;
        ArticleComment that = (ArticleComment) o;
        return this.getId() != null && this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
