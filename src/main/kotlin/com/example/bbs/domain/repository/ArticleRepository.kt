package com.example.bbs.domain.repository

import com.example.bbs.domain.entity.Article
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleRepository : JpaRepository<Article, Int> {
}
