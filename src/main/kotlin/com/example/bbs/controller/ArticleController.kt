package com.example.bbs.controller

import com.example.bbs.domain.entity.Article
import com.example.bbs.domain.repository.ArticleRepository
import com.example.bbs.request.ArticleRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.Date

@Controller
class ArticleController {

    private val MESSAGE_REGISTER_NORMAL = "正常に投稿できました"
    private val MESSAGE_ARTICLE_DOES_NOT_EXISTS = "対象の記事が見つかりませんでした"
    private val MESSAGE_UPDATE_NORMAL = "正常に更新しました"
    private val MESSAGE_ARTICLE_KEY_UNMATCH = "投稿keyが一致しません"
    private val ALERT_CLASS_ERROR = "alert-error"
    private val MESSAGE_DELETE_NORMAL = "正常に削除しました"
    private val PAGE_SIZE = 10

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @PostMapping("/")
    fun registerArticle(
        @Validated @ModelAttribute articleRequest: ArticleRequest,
        result: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result)
            redirectAttributes.addFlashAttribute("request", articleRequest)
            return "redirect:/"
        }
        articleRepository.save(
            Article(
                articleRequest.id,
                articleRequest.name,
                articleRequest.title,
                articleRequest.contents,
                articleRequest.articleKey
            )
        )
        redirectAttributes.addFlashAttribute("message", MESSAGE_REGISTER_NORMAL)
        return "redirect:/"
    }

    @GetMapping("/")
    fun getArticleList(
        @ModelAttribute articleRequest: ArticleRequest,
        @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
        model: Model
    ): String {
        val pageable: Pageable = PageRequest.of(
            page,
            this.PAGE_SIZE,
//            Sort(Sort.Direction.DESC, mutableListOf("updateAt")).and(Sort(Sort.Direction.ASC, mutableListOf("id")))
        )
        if (model.containsAttribute("errors")) {
            val key = BindingResult.MODEL_KEY_PREFIX + "articleRequest"
            model.addAttribute(key, model.asMap()["errors"])
        }
        if (model.containsAttribute("request")) {
            model.addAttribute("articleRequest", model.asMap()["request"])
        }
        val articles = articleRepository.findAll(pageable)
        model.addAttribute("articles", articles)
        return "index"
    }

    @GetMapping("/edit/{id}")
    fun getArticleEdit(
        @PathVariable id: Int,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        return if (articleRepository.existsById(id)) {
            if (model.containsAttribute("request")) {
                model.addAttribute("article", model.asMap()["request"])
            } else {
                model.addAttribute("article", articleRepository.findById(id).get())
            }
            if (model.containsAttribute("errors")) {
                val key = BindingResult.MODEL_KEY_PREFIX + "article"
                model.addAttribute(key, model.asMap()["errors"])
            }
            "edit"
        } else {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            "redirect:/"
        }
    }

    @GetMapping("/delete/confirm/{id}")
    fun getDeleteConfirm(
        @PathVariable id: Int,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (!articleRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/"
        }
        model.addAttribute("article", articleRepository.findById(id).get())
        val key = BindingResult.MODEL_KEY_PREFIX + "article"
        if (model.containsAttribute("errors")) {
            model.addAttribute(key, model.asMap()["errors"])
        }
        return "delete_confirm"
    }

    @PostMapping("/update")
    fun updateArticle(
        @Validated articleRequest: ArticleRequest,
        result: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result)
            redirectAttributes.addFlashAttribute("request", articleRequest)
            return "redirect:/edit/${articleRequest.id}"
        }
        if (!articleRepository.existsById(articleRequest.id)) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/"
        }
        val article = articleRepository.findById(articleRequest.id).get()
        if (articleRequest.articleKey != article.articleKey) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_KEY_UNMATCH)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/edit/${articleRequest.id}"
        }
        article.name = articleRequest.name
        article.title = articleRequest.title
        article.contents = articleRequest.contents
        article.updateAt = Date()
        articleRepository.save(article)

        redirectAttributes.addFlashAttribute("message", MESSAGE_UPDATE_NORMAL)
        return "redirect:/"
    }

    @PostMapping("/delete")
    fun deleteArticle(
        @Validated @ModelAttribute articleRequest: ArticleRequest,
        result: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result)
            redirectAttributes.addFlashAttribute("request", articleRequest)
            return "redirect:/delete/confirm/${articleRequest.id}"
        }
        if (!articleRepository.existsById(articleRequest.id)) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/"
        }
        val article = articleRepository.findById(articleRequest.id).get()
        if (articleRequest.articleKey != article.articleKey) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_KEY_UNMATCH)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/delete/confirm/${article.id}"
        }
        articleRepository.deleteById(articleRequest.id)
        redirectAttributes.addFlashAttribute("message", MESSAGE_DELETE_NORMAL)
        return "redirect:/"
    }
}
