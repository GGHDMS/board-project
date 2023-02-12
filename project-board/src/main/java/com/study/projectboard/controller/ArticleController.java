package com.study.projectboard.controller;

import com.study.projectboard.domain.constant.FormStatus;
import com.study.projectboard.domain.constant.SearchType;
import com.study.projectboard.dto.UserAccountDto;
import com.study.projectboard.dto.request.ArticleRequest;
import com.study.projectboard.dto.response.ArticleResponse;
import com.study.projectboard.dto.response.ArticleWithCommentsResponse;
import com.study.projectboard.service.ArticleService;
import com.study.projectboard.service.PaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * /articles
 * /articles/{article-id}
 * /articles/search
 * /articles/search-hashtag
 */


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/articles")
@Controller
public class ArticleController {

    private final ArticleService articleService;
    private final PaginationService paginationService;

    @GetMapping
    public String articles(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Page<ArticleResponse> articles = articleService.searchArticles(searchType, searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());

        model.addAttribute("articles", articles);
        model.addAttribute("paginationBarNumbers", barNumbers);
        model.addAttribute("searchTypes", SearchType.values());

        return "articles/index";
    }

    @GetMapping("/{articleId}")
    public String articles(@PathVariable Long articleId, Model model) {
        ArticleWithCommentsResponse article = ArticleWithCommentsResponse.from(articleService.getArticleWithComments(articleId));

        model.addAttribute("article", article);
        model.addAttribute("articleComments", article.getArticleCommentsResponse());
        model.addAttribute("totalCount", articleService.getArticleCount());

        return "articles/detail";
    }


    @GetMapping("/search-hashtag")
    public String searchHashtag(
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Page<ArticleResponse> articles = articleService.searchArticlesViaHashtag(searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());
        List<String> hashtags = articleService.getHashtags();

        model.addAttribute("articles", articles);
        model.addAttribute("hashtags", hashtags);
        model.addAttribute("paginationBarNumbers", barNumbers);
        model.addAttribute("searchType", SearchType.HASHTAG);
        return "articles/search-hashtag";
    }

    @GetMapping("/form")
    public String articleForm(Model model){
        model.addAttribute("formStatus", FormStatus.CREATE);

        return "articles/form";
    }

    @PostMapping("/form")
    public String newArticle(@ModelAttribute ArticleRequest articleRequest){
        articleService.saveArticle
                (articleRequest.toDto(UserAccountDto.of(
                        1L, "hsm", "asdf1234",  "hsm@mail.com", "Hsm", "I am Hsm."
                )));
        return "redirect:/articles";

    }

    @GetMapping("/{articleId}/form")
    public String updateArticleForm(@PathVariable Long articleId, Model model){
        ArticleResponse article = ArticleResponse.from(articleService.getArticle(articleId));
        model.addAttribute("article", article);
        model.addAttribute("formStatus", FormStatus.UPDATE);

        return "articles/form";
    }

    @PostMapping("/{articleId}/form")
    public String updateArticle(@PathVariable Long articleId, @ModelAttribute ArticleRequest articleRequest){
        articleService.updateArticle(articleId, articleRequest.toDto(UserAccountDto.of(
                1L, "hsm", "asdf1234",  "hsm@mail.com", "Hsm", "I am Hsm."
        )));

        return "redirect:/articles/" + articleId;
    }

    @PostMapping("{articleId}/delete")
    public String deleteArticle(@PathVariable Long articleId){
        articleService.deleteArticle(articleId);

        return "redirect:/articles";
    }


}
