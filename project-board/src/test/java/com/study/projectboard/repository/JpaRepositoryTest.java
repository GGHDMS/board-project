package com.study.projectboard.repository;

import com.study.projectboard.domain.Article;
import com.study.projectboard.domain.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA 연결 테스트")
@Import(JpaRepositoryTest.TestJpaConfig.class)
@DataJpaTest //transactional
class JpaRepositoryTest {
    
    @Autowired private ArticleRepository articleRepository;
    @Autowired private ArticleCommentRepository articleCommentRepository;
    @Autowired private UserAccountRepository userAccountRepository;


    @DisplayName("select 테스트")
    @Test
    public void testDataSelectingWorkFine() throws Exception{
        //given
        
        //when
        List<Article> articles = articleRepository.findAll();

        //then
        assertThat(articles).isNotNull().hasSize(123);
    }

    @DisplayName("insert 테스트")
    @Test
    public void testDataInsertingWorkFine() throws Exception{
        //given
        long previousCount = articleRepository.count();
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("hsm3", "pw", null, null, null));
        Article article = Article.of(userAccount, "new article", "new content", "#spring");

        //when
        articleRepository.save(article);
        //then
        assertThat(articleRepository.count()).isEqualTo(previousCount + 1);
    }

    @DisplayName("update 테스트")
    @Test
    public void testDataUpdatingWorkFine() throws Exception{
        //given
        Article article = articleRepository.findById(1L).orElseThrow();
        String updateHashtag = "#springboot";
        article.setHashtag(updateHashtag);
        
        //when
        Article saveArticle = articleRepository.saveAndFlush(article);

        //then
        assertThat(saveArticle).hasFieldOrPropertyWithValue("hashtag", updateHashtag);
    }

    @DisplayName("delete 테스트")
    @Test
    public void testDataDeletingWorkFine() throws Exception{
        //given
        Article article = articleRepository.findById(1L).orElseThrow();
        long previousArticleCount = articleRepository.count();
        long previousCommentCount = articleCommentRepository.count();
        long deletedCommentsSize = article.getArticleComments().size();

        //when
        articleRepository.delete(article);

        //then
        assertThat(articleRepository.count()).isEqualTo(previousArticleCount-1);
        assertThat(articleCommentRepository.count()).isEqualTo(previousCommentCount - deletedCommentsSize);
    }


    @EnableJpaAuditing
    @TestConfiguration
    public static class TestJpaConfig {
        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("hsm");
        }
    }
}