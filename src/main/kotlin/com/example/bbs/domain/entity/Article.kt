package com.example.bbs.domain.entity

import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Article(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,
    var name: String = "",
    var title: String = "",
    var contents: String = "",
    @Column(name = "article_key")
    var articleKey: String = "",
    @Column(name = "register_at")
    var registerAt: Date = Date(),
    @Column(name = "update_at")
    var updateAt: Date = Date()
)

