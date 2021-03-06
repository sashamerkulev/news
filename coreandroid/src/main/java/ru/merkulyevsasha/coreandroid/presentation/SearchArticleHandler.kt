package ru.merkulyevsasha.coreandroid.presentation

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.merkulyevsasha.core.domain.ArticlesInteractor
import ru.merkulyevsasha.core.models.Article

class SearchArticleHandler(
    private val articlesInteractor: ArticlesInteractor,
    private val byUserActivities: Boolean,
    private val showProgress: () -> Unit,
    private val hideProgress: () -> Unit,
    private val success: (List<Article>) -> Unit,
    private val failure: () -> Unit
) {
    fun onSearchArticles(searchText: String?): Disposable {
        return articlesInteractor.searchArticles(searchText, byUserActivities)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showProgress() }
            .doAfterTerminate { hideProgress() }
            .subscribe(
                { success(it) },
                {
                    failure()
                })
    }
}