package com.study.projectboard.service;

import com.study.projectboard.domain.Article;
import com.study.projectboard.domain.ArticleComment;
import com.study.projectboard.domain.Hashtag;
import com.study.projectboard.domain.UserAccount;
import com.study.projectboard.dto.ArticleCommentDto;
import com.study.projectboard.dto.UserAccountDto;
import com.study.projectboard.repository.ArticleCommentRepository;
import com.study.projectboard.repository.ArticleRepository;
import com.study.projectboard.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 댓글")
@ExtendWith(MockitoExtension.class)
class ArticleCommentsServiceTest {


    @InjectMocks
    private ArticleCommentsService sut;
    @Mock
    private ArticleCommentRepository articleCommentRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserAccountRepository userAccountRepository;


    @DisplayName("게시글 ID로 조회하면, 해당하는 댓글 리스트를 반환한다.")
    @Test
    public void articleID_searchingComments_returnsComments() throws Exception {
        //given
        Long articleId = 1L;
        ArticleComment expectedParentComment = createArticleComment(1L, "parent content");
        ArticleComment expectedChildComment = createArticleComment(2L,  "child content");
        expectedChildComment.setParentCommentId(expectedParentComment.getId()); // childComment 에 부모 댓글 id 추가
        given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(
                expectedParentComment,
                expectedChildComment
        ));

        //when
        List<ArticleCommentDto> actual = sut.searchArticleComments(articleId);

        //then
        assertThat(actual).hasSize(2); // 댓글 수는 2개
        assertThat(actual)
                .extracting("id", "articleId", "parentCommentId", "content")
                        .containsExactlyInAnyOrder(
                          tuple(1L, 1L, null, "parent content"),
                          tuple(2L, 1L, 1L, "child content")
                        );
        then(articleCommentRepository).should().findByArticle_Id(articleId);
    }



    @DisplayName("댓글 정보를 입력하면, 댓글을 저장한다.")
    @Test
    public void commentInfo_savingComment_savesComment() throws Exception { // 부모 댓글을 저장한다.
        //given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.getArticleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId())).willReturn(createUserAccount());
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null); // save 호출 될거다

        //when
        sut.saveArticleComment(dto);

        //then
        then(articleRepository).should().getReferenceById(dto.getArticleId());
        then(userAccountRepository).should().getReferenceById(dto.getUserAccountDto().getUserId());
        then(articleCommentRepository).should(never()).getReferenceById(anyLong());
        then(articleCommentRepository).should().save(any(ArticleComment.class));
        // 호출 되었냐 ?
    }

    @DisplayName("댓글 저장을 시도했는데 맞는 게시글이 없으면, 경고 로그를 찍고 아무것도 안 한다.")
    @Test
    void givenNonexistentArticle_whenSavingArticleComment_thenLogsSituationAndDoesNothing() {
        // Given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.getArticleId())).willThrow(EntityNotFoundException.class);

        // When
        sut.saveArticleComment(dto);

        // Then
        then(articleRepository).should().getReferenceById(dto.getArticleId());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(articleCommentRepository).shouldHaveNoInteractions();
    }

//    댓글 수정은 대부분 프론트 영역에서 이루어진다.
//
//    @DisplayName("댓글 정보를 입력하면, 댓글을 수정한다.")
//    @Test
//    void givenArticleCommentInfo_whenUpdatingArticleComment_thenUpdatesArticleComment() {
//        // Given
//        String oldContent = "content";
//        String updatedContent = "댓글";
//        ArticleComment articleComment = createArticleComment(oldContent);
//        ArticleCommentDto dto = createArticleCommentDto(updatedContent);
//        given(articleCommentRepository.getReferenceById(dto.getId())).willReturn(articleComment);
//        given(userAccountRepository.getReferenceById(dto.getUserAccountDto().getUserId())).willReturn(articleComment.getUserAccount());
//
//        // When
//        sut.updateArticleComment(dto);
//
//        // Then
//        assertThat(articleComment.getContent())
//                .isNotEqualTo(oldContent)
//                .isEqualTo(updatedContent);
//        then(articleCommentRepository).should().getReferenceById(dto.getId());
//        then(userAccountRepository).should().getReferenceById(dto.getUserAccountDto().getUserId());
//    }
//


    @DisplayName("없는 댓글 정보를 수정하려고 하면, 경고 로그를 찍고 아무 것도 안 한다.")
    @Test
    void givenNonexistentArticleComment_whenUpdatingArticleComment_thenLogsWarningAndDoesNothing() {
        // Given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleCommentRepository.getReferenceById(dto.getId())).willThrow(EntityNotFoundException.class);

        // When
        sut.updateArticleComment(dto);

        // Then
        then(articleCommentRepository).should().getReferenceById(dto.getId());
    }

    @DisplayName("부모 댓글 ID와 댓글 정보를 입력하면, 대댓글을 저장한다.")
    @Test
    public void saveCommentReplyWithParentIdAndContent() throws Exception{
        //given
        Long parentCommentId = 1L;
        ArticleComment parent = createArticleComment(parentCommentId, "댓글");
        ArticleCommentDto child = createArticleCommentDto(parentCommentId, "대댓글");
        given(articleRepository.getReferenceById(child.getArticleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(child.getUserAccountDto().getUserId())).willReturn(createUserAccount());
        given(articleCommentRepository.getReferenceById(child.getParentCommentId())).willReturn(parent);

        //when
        sut.saveArticleComment(child);
        
        //then
        assertThat(child.getParentCommentId()).isNotNull();
        then(articleRepository).should().getReferenceById(child.getParentCommentId());
        then(userAccountRepository).should().getReferenceById(child.getUserAccountDto().getUserId());
        then(articleCommentRepository).should().getReferenceById(child.getParentCommentId()); // 부모 댓글 찾기
        then(articleCommentRepository).should(never()).save(any(ArticleComment.class)); //절대로 발생하면 안된다.

    }



    @DisplayName("댓글 ID를 입력하면, 댓글을 삭제한다.")
    @Test
    void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
        // Given
        Long articleCommentId = 1L;
        String userId = "hsm";
        willDoNothing().given(articleCommentRepository).deleteByIdAndUserAccount_UserId(articleCommentId, userId);

        // When
        sut.deleteArticleComment(articleCommentId, userId);

        // Then
        then(articleCommentRepository).should().deleteByIdAndUserAccount_UserId(articleCommentId, userId);
    }

    private ArticleCommentDto createArticleCommentDto(String content) {
        return createArticleCommentDto(null, content);
    }

    private ArticleCommentDto createArticleCommentDto(Long parentCommentId, String content) {
        return createArticleCommentDto(1L, parentCommentId, content);
    }

    private ArticleCommentDto createArticleCommentDto(Long id, Long parentCommentId, String content) {
        return ArticleCommentDto.of(
                id, // commentId
                1L,
                createUserAccountDto(),
                parentCommentId,
                content,
                LocalDateTime.now(),
                "hsm",
                LocalDateTime.now(),
                "hsm"
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
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

    private ArticleComment createArticleComment(Long id, String content) {
        ArticleComment articleComment = ArticleComment.of(
                createArticle(),
                createUserAccount(),
                content
        );
        ReflectionTestUtils.setField(articleComment, "id", id);

        return articleComment;
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "hsm",
                "password",
                "hsm@email.com",
                "Hsm",
                null
        );
    }

    private Article createArticle() {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content"
        );
        ReflectionTestUtils.setField(article, "id", 1L); // id로 정렬 및 동등성 검사를 해야되기 때문에 넣어 준다.
        article.addHashtags(Set.of(createHashtag(article)));
        return article;
    }


    private Hashtag createHashtag(Article article) {
        return Hashtag.of("java");
    }

}