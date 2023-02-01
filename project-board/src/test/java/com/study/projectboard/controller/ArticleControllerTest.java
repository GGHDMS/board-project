package com.study.projectboard.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("View 컨트롤러 - 게시글")
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired private MockMvc mvc;


    @DisplayName(("[View][GET] 게시글 리스트 (게시판) 페이지 - 정상 호출"))
    @Test
    public void nothing_requestingArticlesView_returnArticlesView() throws Exception{
        //given
        //when&then
        mvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"));
    }

    @Disabled("개발중")
    @DisplayName(("[View][GET] 게시글 상세 페이지 - 정상 호출"))
    @Test
    public void nothing_requestingArticleView_returnArticleView() throws Exception{
        //given
        //when&then
        mvc.perform(get("/articles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/detail"))
                .andExpect(model().attributeExists("article"))
                .andExpect(model().attributeExists("articleComments"));
    }

    @Disabled("개발중")
    @DisplayName(("[View][GET] 게시글 검색 페이지 - 정상 호출"))
    @Test
    public void nothing_requestingArticleSearchView_returnArticleSearchView() throws Exception{
        //given
        //when&then
        mvc.perform(get("/articles/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search"));
    }

    @Disabled("개발중")
    @DisplayName(("[View][GET] 게시글 해시태그 검색 페이지 - 정상 호출"))
    @Test
    public void nothing_requestingArticleHashtagSearchView_returnArticleHashtagSearchView() throws Exception{
        //given
        //when&then
        mvc.perform(get("/articles/search-hashtag"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/hashtag"));

    }


}