package com.study.projectboard.dto;

import com.study.projectboard.domain.Hashtag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class HashtagWithArticlesDtos {
    private Long id;
    private Set<ArticleDto> articleDtos;
    private String hashtagName;
    LocalDateTime createdAt;
    String createdBy;
    LocalDateTime modifiedAt;
    String modifiedBy;

    private HashtagWithArticlesDtos(Long id, Set<ArticleDto> articleDtos, String hashtagName, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        this.id = id;
        this.articleDtos = articleDtos;
        this.hashtagName = hashtagName;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
    }

    public static HashtagWithArticlesDtos of(Set<ArticleDto> articleDtos, String hashtagName){
        return new HashtagWithArticlesDtos(null, articleDtos, hashtagName, null, null, null, null);
    }


    public static HashtagWithArticlesDtos of(Long id, Set<ArticleDto> articleDtos, String hashtagName, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy){
        return new HashtagWithArticlesDtos(id, articleDtos, hashtagName, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static HashtagWithArticlesDtos from(Hashtag entity){
        return new HashtagWithArticlesDtos(
                entity.getId(),
                entity.getArticles().stream()
                        .map(ArticleDto::from)
                        .collect(Collectors.toUnmodifiableSet()),
                entity.getHashtagName(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getModifiedAt(),
                entity.getModifiedBy()
        );
    }

    public Hashtag toEntity(){
        return Hashtag.of(hashtagName);
    }
}
