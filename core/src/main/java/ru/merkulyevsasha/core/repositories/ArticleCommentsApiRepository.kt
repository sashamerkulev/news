package ru.merkulyevsasha.core.repositories

import io.reactivex.Completable
import io.reactivex.Single
import ru.merkulyevsasha.core.models.ArticleComment
import java.util.*

interface ArticleCommentsApiRepository {
    fun getArticleComments(articleId: Int, lastArticleCommentsReadDate: Date): Single<List<ArticleComment>>
    fun addArticleComment(articleId: Int, comment: String): Single<ArticleComment>
    fun likeArticleComment(commentId: Int): Single<ArticleComment>
    fun dislikeArticleComment(commentId: Int): Single<ArticleComment>
    fun deleteArticleComment(commentId: Int): Completable
}
