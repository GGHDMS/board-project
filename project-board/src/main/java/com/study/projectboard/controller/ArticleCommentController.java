package com.study.projectboard.controller;

import com.study.projectboard.dto.request.ArticleCommentRequest;
import com.study.projectboard.dto.security.BoardPrincipal;
import com.study.projectboard.service.ArticleCommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class ArticleCommentController {

    private final ArticleCommentsService articleCommentsService;

    @PostMapping("/new")
    public String newArticleComment(
            @ModelAttribute ArticleCommentRequest articleCommentRequest,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        articleCommentsService.saveArticleComment(articleCommentRequest.toDto(boardPrincipal.toDto()));
        return "redirect:/articles/" + articleCommentRequest.getArticleId();
    }

    @PostMapping("/{articleCommentId}/delete")
    public String deleteArticleComment(
            @PathVariable Long articleCommentId,
            @RequestParam Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        String username = boardPrincipal.getUsername();
        articleCommentsService.deleteArticleComment(articleCommentId, username);
        return "redirect:/articles/" + articleId;
    }


}
