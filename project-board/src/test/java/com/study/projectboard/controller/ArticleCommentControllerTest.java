package com.study.projectboard.controller;

import com.study.projectboard.config.TestSecurityConfig;
import com.study.projectboard.dto.ArticleCommentDto;
import com.study.projectboard.dto.request.ArticleCommentRequest;
import com.study.projectboard.service.ArticleCommentsService;
import com.study.projectboard.util.FormDataEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("View 컨트롤러 - 댓글")
@Import({TestSecurityConfig.class, FormDataEncoder.class})
@WebMvcTest(ArticleCommentController.class)
class ArticleCommentControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private FormDataEncoder formDataEncoder;

    @MockBean
    private ArticleCommentsService articleCommentsService;

    @WithUserDetails(value = "hsmTest", userDetailsServiceBeanName = "userDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][post] 댓글 등록 - 정상 호출")
    @Test
    void givenNewArticleCommentInfo_whenRequesting_thenSavesNewArticleComment() throws Exception {
        // Given
        Long articleId = 1L;
        ArticleCommentRequest articleCommentRequest = ArticleCommentRequest.of(articleId, "new content");
        willDoNothing().given(articleCommentsService).saveArticleComment(any(ArticleCommentDto.class));

        // When & Then
        mvc.perform(
                        post("/comments/new")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(articleCommentRequest))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/" + articleId))
                .andExpect(redirectedUrl("/articles/" + articleId));
        then(articleCommentsService).should().saveArticleComment(any(ArticleCommentDto.class));
    }

    @WithUserDetails(value = "hsmTest", userDetailsServiceBeanName = "userDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][post] 댓글 삭제 - 정상 호출")
    @Test
    void givenArticleCommentId_whenRequesting_thenDeleteArticleComment() throws Exception {
        // Given
        long articleId = 1L;
        Long articleCommentId = 1L;
        String userId = "hsmTest";
        willDoNothing().given(articleCommentsService).deleteArticleComment(articleCommentId, userId);

        // When & Then
        mvc.perform(
                        post("/comments/" + articleId + "/delete")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(Map.of("articleId", articleId)))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/"+articleId))
                .andExpect(redirectedUrl("/articles/"+articleId));

        then(articleCommentsService).should().deleteArticleComment(articleCommentId, userId);
    }


    @WithUserDetails(value = "hsmTest", userDetailsServiceBeanName = "userDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][post] 대댓글 등록 - 정상 호출")
    @Test
    void addCommentReply() throws Exception {
        // Given
        Long articleId = 1L;
        ArticleCommentRequest articleCommentRequest = ArticleCommentRequest.of(articleId, 1L, "test comment"); // 게시글 id, 부모 댓글 id, content 입력
        willDoNothing().given(articleCommentsService).saveArticleComment(any(ArticleCommentDto.class));

        // When & Then
        mvc.perform(
                        post("/comments/new")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(articleCommentRequest))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/" + articleId))
                .andExpect(redirectedUrl("/articles/" + articleId));
        then(articleCommentsService).should().saveArticleComment(any(ArticleCommentDto.class));
    }





}