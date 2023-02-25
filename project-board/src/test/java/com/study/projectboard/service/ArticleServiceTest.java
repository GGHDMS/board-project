package com.study.projectboard.service;

import com.study.projectboard.domain.Article;
import com.study.projectboard.domain.Hashtag;
import com.study.projectboard.domain.UserAccount;
import com.study.projectboard.domain.constant.SearchType;
import com.study.projectboard.dto.ArticleDto;
import com.study.projectboard.dto.ArticleWithCommentsDto;
import com.study.projectboard.dto.HashtagDto;
import com.study.projectboard.dto.UserAccountDto;
import com.study.projectboard.repository.ArticleRepository;
import com.study.projectboard.repository.HashtagRepository;
import com.study.projectboard.repository.UserAccountRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@DisplayName("비즈니스 로직 - 게시판")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {


    @InjectMocks
    private ArticleService sut;
    @Mock
    private HashtagService hashtagService;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private HashtagRepository hashtagRepository;

    @DisplayName("검색어 없이 게시글을 검색하면, 게시글 페이지를 반환한다.")
    @Test
    public void noSearchParameter_searchingArticles_returnsArticleList() throws Exception {
        //given
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());

        //when
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);
        //then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("검색어와 함께 게시글을 검색하면, 게시글 페이지를 반환한다.")
    @Test
    void givenSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
        // Given
        SearchType searchType = SearchType.TITLE;
        String searchKeyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());

        // When
        Page<ArticleDto> articles = sut.searchArticles(searchType, searchKeyword, pageable);

        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitleContaining(searchKeyword, pageable);
    }

    @DisplayName("검색어 없이 해시태그 검색하면, 빈 페이지를 반환한다.")
    @Test
    void givenNoSearchParameters_whenHashtagSearchingArticles_thenReturnsEmptyPage() {
        // Given
        Pageable pageable = Pageable.ofSize(20);

        // When
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(null, pageable);

        // Then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(hashtagRepository).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("없는 해시태그를 검색하면, 빈 페이지를 반환한다.")
    @Test
    public void givenNonexistentHashtag_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
        //given
        String hashtagName = "없는 해시태그";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        //when
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtagName, pageable);

        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
    }


    @DisplayName("게시글을 해시태그 검색하면, 게시글 페이지를 반환한다.")
    @Test
    void givenHashtagSearchParameters_whenHashtagSearchingArticles_thenReturnsHashtagSearchPage() {
        // Given
        String hashtagName = "java";
        Pageable pageable = Pageable.ofSize(20);
        Article expectedArticle = createArticle();
        given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(expectedArticle), pageable, 1));

        // When
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtagName, pageable);

        // Then
        assertThat(articles).isEqualTo(new PageImpl<>(List.of(ArticleDto.from(expectedArticle)), pageable, 1));
        then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
    }


    @DisplayName("게시글 ID로 조회하면, 댓글 달긴 게시글을 반환한다")
    @Test
    void givenArticleId_whenSearchingArticleWithComments_thenReturnsArticleWithComments() {
        // Given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        // When
        ArticleWithCommentsDto dto = sut.getArticleWithComments(articleId);

        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );

        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("댓글 달린 게시글이 없으면, 예외를 던진다")
    @Test
    void givenNonexistentArticleId_whenSearchingArticleWithComments_thenReturnsThrowsException() {
        // Given
        Long articleId = 0L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        // When
        Throwable t = catchThrowable(() -> sut.getArticleWithComments(articleId));

        // Then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);


        then(articleRepository).should().findById(articleId);
    }


    @DisplayName("게시글을 조회하면, 게시글을 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
        // Given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        // When
        ArticleDto dto = sut.getArticle(articleId);

        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("게시글이 없으면, 예외를 던진다.")
    @Test
    void givenNonexistentArticleId_whenSearchingArticle_thenThrowsException() {
        // Given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        // When
        Throwable t = catchThrowable(() -> sut.getArticle(articleId));

        // Then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }


    @DisplayName("게시글 정보를 입력하면, 본문에서 해시태그 정보를 추출하여 해시태그 정보가 포함된 게시글을 생성한다.")
    @Test
    public void articleInfo_savingArticle_ExtractsHashtagsFromContentAndSavesArticleWithExtractedHashtags() throws Exception {
        //given
        ArticleDto dto = createArticleDto();
        Set<String> expectedHashtagNames = Set.of("java", "spring");
        Set<Hashtag> expectedHashtags = new HashSet<>();
        expectedHashtags.add(createHashtag("java"));


        given(userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId())).willReturn(createUserAccount());
        given(hashtagService.parseHashtagNames(dto.getContent())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        //when
        sut.saveArticle(dto);

        //then
        then(userAccountRepository).should().getReferenceById(dto.getUserAccountDto().getUserId());
        then(hashtagService).should().parseHashtagNames(dto.getContent());
        then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
        then(articleRepository).should().save(any(Article.class)); // 호출 되었냐 ?
    }


    @DisplayName("게시글의 수정 정보를 입력하면, 게시글을 수정한다.")
    @Test
    public void ModifiedArticleInfo_savingArticle_updatesArticle() throws Exception {
        //given
        Article article = createArticle();
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용 #springboot");
        Set<String> expectedHashtagNames = Set.of("springboot");
        Set<Hashtag> expectedHashtags = new HashSet<>();

        given(articleRepository.getReferenceById(dto.getId())).willReturn(article);
        given(userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId())).willReturn(dto.getUserAccountDto().toEntity());
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());
        given(hashtagService.parseHashtagNames(dto.getContent())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);

        //when
        sut.updateArticle(dto.getId(), dto);

        //then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", dto.getTitle())
                .hasFieldOrPropertyWithValue("content", dto.getContent())
                .extracting("hashtags", as(InstanceOfAssertFactories.COLLECTION))
                    .hasSize(1)
                        .extracting("hashtagName")
                        .containsExactly("springboot");

        then(articleRepository).should().getReferenceById(dto.getId());
        then(userAccountRepository).should().getReferenceById(dto.getUserAccountDto().getUserId());
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
        then(hashtagService).should().parseHashtagNames(dto.getContent());
        then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
    }

    @DisplayName("없는 게시글의 수정 정보를 입력하면, 경고 로그를 찍고 아무 것도 하지 않는다.")
    @Test
    void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
        // Given
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용");
        given(articleRepository.getReferenceById(dto.getId())).willThrow(EntityNotFoundException.class);

        // When
        sut.updateArticle(dto.getId(), dto);

        // Then
        then(articleRepository).should().getReferenceById(dto.getId());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(hashtagRepository).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 작성자가 아닌 사람이 수정 정보를 입력하면, 아무 것도 하지 않는다.")
    @Test
    void givenModifiedArticleInfoWithDifferentUser_whenUpdatingArticle_thenDoesNothing() {
        // Given
        Long differentArticleId = 22L;
        Article differentArticle = createArticle(differentArticleId);
        differentArticle.setUserAccount(createUserAccount("Heo"));
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용");
        given(articleRepository.getReferenceById(differentArticleId)).willReturn(differentArticle);
        given(userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId())).willReturn(dto.getUserAccountDto().toEntity());

        // When
        sut.updateArticle(differentArticleId, dto);

        // Then
        then(articleRepository).should().getReferenceById(differentArticleId);
        then(userAccountRepository).should().getReferenceById(dto.getUserAccountDto().getUserId());
        then(hashtagService).shouldHaveNoInteractions();
    }


    @DisplayName("게시글 ID를 입력하면, 게시글을 삭제한다.")
    @Test
    public void articleID_deletingArticle_deletesArticle() throws Exception {
        //given
        Long articleId = 1L;
        String userId = "hsm";
        given(articleRepository.getReferenceById(articleId)).willReturn(createArticle());
        willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(articleId, userId);
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());

        //when
        sut.deleteArticle(articleId, userId);

        //then
        then(articleRepository).should().getReferenceById(articleId);
        then(articleRepository).should().deleteByIdAndUserAccount_UserId(articleId, userId);
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
    }

    @DisplayName("게시글 수를 조회하면, 게시글 수를 반환한다")
    @Test
    void nothing_countingArticles_returnsArticleCount() {
        //given
        Long expected = 0L;
        
        given(articleRepository.count()).willReturn(expected);

        //when
        long articleCount = sut.getArticleCount();

        //then
        assertThat(expected).isEqualTo(articleCount);
        then(articleRepository).should().count();
    }

    @DisplayName("해시태그를 조회하면, 유티크 해시태그 리스트를 반환한다")
    @Test
    void given_whenSearch_thenReturnsHashtagsList() {
        // Given
        Article article = createArticle();
        List<String> expectedHashtags = List.of("java", "spring", "boot");
        given(hashtagRepository.findAllHashtagNames()).willReturn(expectedHashtags);

        // When
        List<String> actualHashtags = sut.getHashtags();

        // Then
        assertThat(actualHashtags).isEqualTo(expectedHashtags);
        then(hashtagRepository).should().findAllHashtagNames();

    }


    private UserAccount createUserAccount() {
        return createUserAccount("hsm");
    }

    private UserAccount createUserAccount(String userId) {
        return UserAccount.of(
                userId,
                "password",
                "hsm@email.com",
                "Hsm",
                null
        );
    }

    private Article createArticle() {
        return createArticle(1L);
    }

    private Article createArticle(Long id) {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content"
        );

        article.addHashtags(Set.of(
                createHashtag(1L, "java"),
                createHashtag(2L, "spring")
        ));
        ReflectionTestUtils.setField(article, "id", id);


        return article;
    }

    private Hashtag createHashtag(String hashtagName) {
        return createHashtag(1L, hashtagName);
    }

    private Hashtag createHashtag(Long id, String hashtagName) {
        Hashtag hashtag = Hashtag.of(hashtagName);
        ReflectionTestUtils.setField(hashtag, "id", id);

        return hashtag;
    }

    private HashtagDto createHashtagDto() {
        return HashtagDto.of("java");
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content");
    }

    private ArticleDto createArticleDto(String title, String content) {
        return ArticleDto.of(
                1L,
                createUserAccountDto(),
                title,
                content,
                null,
                LocalDateTime.now(),
                "Hsm",
                LocalDateTime.now(),
                "Hsm");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                1L,
                "hsm",
                "password",
                "hsm@mail.com",
                "Hsm",
                "This is memo",
                LocalDateTime.now(),
                "hsm",
                LocalDateTime.now(),
                "hsm"
        );
    }
}