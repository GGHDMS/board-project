package com.study.projectboard.controller;

import com.study.projectboard.dto.UserAccountDto;
import com.study.projectboard.dto.request.ArticleCommentRequest;
import com.study.projectboard.service.ArticleCommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class ArticleCommentController {

    private final ArticleCommentsService articleCommentsService;

    @PostMapping("/new")
    public String newArticleComment(@ModelAttribute ArticleCommentRequest articleCommentRequest) {
        articleCommentsService.saveArticleComment(articleCommentRequest.toDto(UserAccountDto.of(
                1L, "hsm", "asdf1234", "hsm@mail.com", "Hsm", "I am Hsm."
        )));
        return "redirect:/articles/" + articleCommentRequest.getArticleId();
    }

    @PostMapping("/{articleCommentId}/delete")
    public String deleteArticleComment(@PathVariable Long articleCommentId, @RequestParam Long articleId) {
        articleCommentsService.deleteArticleComment(articleCommentId);
        return "redirect:/articles/" + articleId;
    }


}
