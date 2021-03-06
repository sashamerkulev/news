package ru.merkulyevsasha.netrepository.network.articles

import io.reactivex.Single
import ru.merkulyevsasha.core.models.Article
import ru.merkulyevsasha.core.preferences.KeyValueStorage
import ru.merkulyevsasha.core.repositories.ArticlesApiRepository
import ru.merkulyevsasha.netrepository.network.base.BaseApiRepository
import ru.merkulyevsasha.netrepository.network.mappers.ArticleMapper
import ru.merkulyevsasha.network.data.ArticlesApi
import java.text.SimpleDateFormat
import java.util.*

class ArticlesApiRepositoryImpl(
    sharedPreferences: KeyValueStorage,
    baseUrl: String,
    debugMode: Boolean
) : BaseApiRepository(sharedPreferences, baseUrl, debugMode), ArticlesApiRepository {

    private val articlesMapper = ArticleMapper()

    private val api: ArticlesApi = retrofit.create(ArticlesApi::class.java)

    private val format = "yyyy-MM-dd'T'HH:mm:ss"
    private val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())

    override fun getArticles(lastArticleReadDate: Date, rssSourceNameMap : Map<String, String>): Single<List<Article>> {
        return api.getArticles(simpleDateFormat.format(lastArticleReadDate))
            .flattenAsFlowable { it }
            .map { articlesMapper.map(it, rssSourceNameMap) }
            .toList()
    }

    override fun likeArticle(articleId: Int, rssSourceNameMap : Map<String, String>): Single<Article> {
        return api.likeArticle(articleId)
            .map { articlesMapper.map(it, rssSourceNameMap) }
    }

    override fun dislikeArticle(articleId: Int, rssSourceNameMap : Map<String, String>): Single<Article> {
        return api.dislikeArticle(articleId)
            .map { articlesMapper.map(it, rssSourceNameMap) }
    }

    override fun getArticle(articleId: Int, rssSourceNameMap : Map<String, String>): Single<Article> {
        return api.getArticle(articleId)
            .map { articlesMapper.map(it, rssSourceNameMap) }
    }
}