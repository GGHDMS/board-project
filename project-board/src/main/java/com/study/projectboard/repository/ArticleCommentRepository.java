package com.study.projectboard.repository;

import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.study.projectboard.domain.ArticleComment;
import com.study.projectboard.domain.QArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ArticleCommentRepository
        extends JpaRepository<ArticleComment, Long>,
        QuerydslPredicateExecutor<ArticleComment>,
        QuerydslBinderCustomizer<QArticleComment> {

    @Query("select a from ArticleComment a where a.article.id= :articleid")
    List<ArticleComment> findByArticle_Id(Long articleId);

    @Override
    default void customize(QuerydslBindings bindings, QArticleComment root){
        bindings.excludeUnlistedProperties(true); //원하지 않는 검색 제거
        bindings.including(root.content, root.createdAt, root.createdBy); //원하는 검색 포함
        bindings.bind(root.content).first(StringExpression::containsIgnoreCase);
        bindings.bind(root.createdAt).first(DateTimeExpression::eq);
        bindings.bind(root.createdBy).first(StringExpression::containsIgnoreCase);
    }
}

