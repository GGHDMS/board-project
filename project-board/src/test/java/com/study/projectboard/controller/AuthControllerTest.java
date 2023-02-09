package com.study.projectboard.controller;


import com.study.projectboard.config.SecurityConfig;
import com.study.projectboard.service.ArticleService;
import com.study.projectboard.service.PaginationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("View 컨트롤러 - 인증")
@Import(SecurityConfig.class)
@WebMvcTest(ArticleController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private  ArticleService articleService;
    @MockBean
    private  PaginationService paginationService;

    @DisplayName(("[View][GET] 로그인 페이지 - 정상 호출"))
    @Test
    public void nothing_TryingToLogin_returnLoginView() throws Exception{
        //given
        //when&then
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }



}
