package ru.merkulyevsasha.news.presentation.routers

import androidx.fragment.app.FragmentManager
import ru.merkulyevsasha.articlecomments.ArticleCommentsFragment
import ru.merkulyevsasha.articledetails.ArticleDetailsFragment
import ru.merkulyevsasha.core.routers.MainActivityRouter
import ru.merkulyevsasha.news.R
import ru.merkulyevsasha.news.presentation.main.MainFragment

class MainActivityRouterImpl(fragmentManager: FragmentManager) : BaseRouter(R.id.container, fragmentManager), MainActivityRouter {

    override fun showMain() {
        val tag = MainFragment.TAG
        val fragment = findOrCreateFragment(tag) { MainFragment.newInstance() }
        replaceFragment(tag, fragment)
    }

    override fun showArticleDetails(articleId: Int) {
        val tag = ArticleDetailsFragment.TAG
        val fragment = findOrCreateFragment(tag) { ArticleDetailsFragment.newInstance(articleId) }
        replaceFragment(tag, fragment, true)
    }

    override fun showArticleComments(articleId: Int) {
        val tag = ArticleCommentsFragment.TAG
        val fragment = findOrCreateFragment(tag) { ArticleCommentsFragment.newInstance(articleId) }
        replaceFragment(tag, fragment, true)
    }

}