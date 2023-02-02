package com.study.projectboard.service;

import com.study.projectboard.domain.Article;
import com.study.projectboard.domain.ArticleComment;
import com.study.projectboard.domain.UserAccount;
import com.study.projectboard.dto.ArticleCommentDto;
import com.study.projectboard.dto.ArticleUpdateDto;
import com.study.projectboard.dto.UserAccountDto;
import com.study.projectboard.repository.ArticleCommentRepository;
import com.study.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @DisplayName("게시글 ID로 조회하면, 댓글 리스트를 반환한다.")
    @Test
    public void articleID_searchingComments_returnsComments() throws Exception {
        //given
        Long articleId = 1L;
        ArticleComment expected = createArticleComment("content");
        given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(expected));
        //when
        List<ArticleCommentDto> actual = sut.searchArticleComments(articleId);
        //then
        assertThat(actual)
                .hasSize(1)
                .first().hasFieldOrPropertyWithValue("content", expected.getContent());
        then(articleCommentRepository).should().findByArticle_Id(articleId);
    }


    @DisplayName("댓글 정보를 입력하면, 댓글을 생성한다.")
    @Test
    public void commentInfo_savingComment_savesComment() throws Exception {
        //given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.getArticleId())).willReturn(createArticle());
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null); // save 호출 될거다

        //when
        sut.saveArticleComment(dto);

        //then
        then(articleRepository).should().getReferenceById(dto.getArticleId());
        then(articleCommentRepository).should().save(any(ArticleComment.class)); // 호출 되었냐 ?
    }


    @DisplayName("댓글 ID와 수정정보를 입력하면, 댓글을 수정한다.")
    @Test
    public void commentIDAndModifiedInfo_savingComment_updatesComment() throws Exception {
        //given
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null); // save 호출 될거다


        //when
        sut.updateArticleComment(1L, ArticleUpdateDto.of("updateTitle", "updateContent", "updateHashtag"));

        //then
        then(articleCommentRepository).should().save(any(ArticleComment.class)); // 호출 되었냐 ?
    }

    @DisplayName("댓글 ID를 입력하면, 댓글을 삭제한다.")
    @Test
    public void articleID_deletingArticle_deletesArticle() throws Exception {
        //given
        willDoNothing().given(articleCommentRepository).delete(any(ArticleComment.class));// save 호출 될거다

        //when
        sut.deleteArticleComment(1L);

        //then
        then(articleCommentRepository).should().delete(any(ArticleComment.class)); // 호출 되었냐 ?
    }



    private ArticleCommentDto createArticleCommentDto(String content) {
        return ArticleCommentDto.of(
                1L,
                1L,
                createUserAccountDto(),
                content,
                LocalDateTime.now(),
                "hsum",
                LocalDateTime.now(),
                "hsm"
        );
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

    private ArticleComment createArticleComment(String content) {
        return ArticleComment.of(
                Article.of(createUserAccount(), "title", "content", "hashtag"),
                createUserAccount(),
                content
        );
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
        return Article.of(
                createUserAccount(),
                "title",
                "content",
                "#java"
        );
    }

}