package com.study.projectboard.repository;

import com.study.projectboard.domain.Article;
import com.study.projectboard.domain.ArticleComment;
import com.study.projectboard.domain.Hashtag;
import com.study.projectboard.domain.UserAccount;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA 연결 테스트")
@Import(JpaRepositoryTest.TestJpaConfig.class)
@DataJpaTest //transactional
class JpaRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleCommentRepository articleCommentRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private HashtagRepository hashtagRepository;


    @DisplayName("select 테스트")
    @Test
    public void testDataSelectingWorkFine() throws Exception {
        //given

        //when
        List<Article> articles = articleRepository.findAll();

        //then
        assertThat(articles).isNotNull().hasSize(123);
    }

    @DisplayName("insert 테스트")
    @Test
    public void testDataInsertingWorkFine() throws Exception {
        //given
        long previousCount = articleRepository.count();
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("hsm3", "pw", null, null, null));
        Article article = Article.of(userAccount, "new article", "new content");
        article.addHashtags(Set.of(Hashtag.of("spring")));

        //when
        articleRepository.save(article);
        //then
        assertThat(articleRepository.count()).isEqualTo(previousCount + 1);
    }

    @DisplayName("update 테스트")
    @Test
    public void testDataUpdatingWorkFine() throws Exception {
        //given
        Article article = articleRepository.findById(1L).orElseThrow();
        Hashtag updateHashtag = Hashtag.of("springboot");
        article.clearHashtags();
        article.addHashtags(Set.of(updateHashtag));

        //when
        Article saveArticle = articleRepository.saveAndFlush(article);

        //then
        assertThat(saveArticle.getHashtags())
                .hasSize(1)
                .extracting("hashtagName", String.class)
                .containsExactly(updateHashtag.getHashtagName());
    }

    @DisplayName("delete 테스트")
    @Test
    public void testDataDeletingWorkFine() throws Exception {
        //given
        Article article = articleRepository.findById(1L).orElseThrow();
        long previousArticleCount = articleRepository.count();
        long previousCommentCount = articleCommentRepository.count();
        long deletedCommentsSize = article.getArticleComments().size();

        //when
        articleRepository.delete(article);

        //then
        assertThat(articleRepository.count()).isEqualTo(previousArticleCount - 1);
        assertThat(articleCommentRepository.count()).isEqualTo(previousCommentCount - deletedCommentsSize);
    }

    @DisplayName("대댓글 조회 테스트")
    @Test
    public void getReplies() throws Exception {
        //given

        //when
        Optional<ArticleComment> parentComment = articleCommentRepository.findById(1L);

        //then
        assertThat(parentComment).get() //get() Optional null 확인 하면서 꺼내온다
                .hasFieldOrPropertyWithValue("parentCommentId", null) // 부모 댓글이므로 null 이 나와야 된다.
                .extracting("childComments", InstanceOfAssertFactories.COLLECTION) //타입이 Collection 이다.
                .hasSize(4);
    }

    @DisplayName("댓글에 대댓글 삽입 테스트")
    @Test
    public void insertReplyToComment() throws Exception {
        //given
        ArticleComment parentComment = articleCommentRepository.getReferenceById(1L); // 1L 댓글에 삽입할 생각
        ArticleComment childComment = ArticleComment.of(
                parentComment.getArticle(),
                parentComment.getUserAccount(),
                "대댓글"
        );

        //when
        parentComment.addChildComment(childComment); // 테스트 대상
        articleCommentRepository.flush(); // query 를 확인하기 위해서

        //then
        assertThat(articleCommentRepository.findById(1L)).get()
                .hasFieldOrPropertyWithValue("parentCommentId", null)
                .extracting("childComments", InstanceOfAssertFactories.COLLECTION)
                .hasSize(5);
    }

    @DisplayName("댓글 삭제와 대댓글 전체 연동 삭제 테스트")
    @Test
    public void deleteCommentAndRepliesByCommentId() throws Exception {
        //given
        ArticleComment parentComment = articleCommentRepository.getReferenceById(1L);
        long previousArticleCommentCount = articleCommentRepository.count();

        //when
        articleCommentRepository.deleteById(1L); // 기본적이 jpa 테스트

        //then
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - 5);
    }


    @DisplayName("댓글 삭제와 대댓글 전체 연동 삭제 테스트 - 댓글 ID + 유저 ID")
    @Test
    public void deleteCommentAndRepliesByCommentIdAndUserId() throws Exception {
        //given
        long previousArticledCommentCount = articleCommentRepository.count(); // 댓글의 개수를 count

        //when
        articleCommentRepository.deleteByIdAndUserAccount_UserId(1L, "hsm"); // 실제 비즈니스 로직 테스트

        //then
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticledCommentCount - 5);
    }


    @DisplayName("[Querydsl] 전체 hashtag 리스트에서 이름만 조회하기")
    @Test
    public void Nothing_QueryingHashtags_HashtagNames() throws Exception {
        //given

        //when
        List<String> hashtagNames = hashtagRepository.findAllHashtagNames();

        //then
        assertThat(hashtagNames).hasSize(19);
    }

    @DisplayName("[Querydsl] 해시태그로 페이징된 게시글 검색하기")
    @Test
    public void HashtagNamesAndPaging_QueryingArticles_ArticlePage() throws Exception {
        //given
        List<String> hashtagNames = List.of("blue", "crimson", "fuscia");
        Pageable pageable = PageRequest.of(0, 5, Sort.by(
                Sort.Order.desc("hashtags.hashtagName"),
                Sort.Order.asc("title")
        ));
        //when
        Page<Article> articlePage = articleRepository.findByHashtagNames(hashtagNames, pageable);

        //then
        assertThat(articlePage.getContent()).hasSize(articlePage.getSize());
        assertThat(articlePage.getContent().get(0).getTitle()).isEqualTo("Fusce posuere felis sed lacus.");
        assertThat(articlePage.getContent().get(0).getHashtags())
                .extracting("hashtagName", String.class)
                .containsExactly("fuscia");
        assertThat(articlePage.getTotalElements()).isEqualTo(17);
        assertThat(articlePage.getTotalPages()).isEqualTo(4);

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