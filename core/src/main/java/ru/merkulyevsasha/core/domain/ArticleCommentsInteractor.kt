package ru.merkulyevsasha.core.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.merkulyevsasha.core.models.ArticleComment

interface ArticleCommentsInteractor {
    fun getArticleComments(articleId: Long): Single<List<ArticleComment>>
    fun commentArticle(articleId: Long, comment: String): Single<ArticleComment>
    fun likeArticleComment(commentId: Int): Single<ArticleComment>
    fun dislikeArticleComment(commentId: Int): Single<ArticleComment>
    fun deleteArticleComment(commentId: Int): Completable
}
