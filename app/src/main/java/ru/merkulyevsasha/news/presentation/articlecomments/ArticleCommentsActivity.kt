package ru.merkulyevsasha.news.presentation.articlecomments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_articlecomments.editTextComment
import kotlinx.android.synthetic.main.activity_articlecomments.layoutAddCommentButton
import kotlinx.android.synthetic.main.activity_articlecomments.progressbar
import kotlinx.android.synthetic.main.fragment_articles.recyclerView
import kotlinx.android.synthetic.main.row_comment_article.view.imageViewDislike
import kotlinx.android.synthetic.main.row_comment_article.view.imageViewLike
import kotlinx.android.synthetic.main.row_comment_article.view.imageViewThumb
import kotlinx.android.synthetic.main.row_comment_article.view.layoutButtonDislike
import kotlinx.android.synthetic.main.row_comment_article.view.layoutButtonLike
import kotlinx.android.synthetic.main.row_comment_article.view.layoutButtonShare
import kotlinx.android.synthetic.main.row_comment_article.view.newsDateSource
import kotlinx.android.synthetic.main.row_comment_article.view.newsTitle
import kotlinx.android.synthetic.main.row_comment_article.view.textViewDislike
import kotlinx.android.synthetic.main.row_comment_article.view.textViewLike
import ru.merkulyevsasha.RequireServiceLocator
import ru.merkulyevsasha.ServiceLocator
import ru.merkulyevsasha.core.domain.ArticleCommentsInteractor
import ru.merkulyevsasha.core.models.Article
import ru.merkulyevsasha.core.models.ArticleOrComment
import ru.merkulyevsasha.news.R
import ru.merkulyevsasha.news.presentation.common.ColorThemeResolver
import ru.merkulyevsasha.news.presentation.common.newsadapter.ArticleCallbackClickHandler
import java.text.SimpleDateFormat
import java.util.*

class ArticleCommentsActivity : AppCompatActivity(), ArticleCommentsView, RequireServiceLocator {

    companion object {
        private const val ARTICLE_ID = "ARTICLE_ID"
        @JvmStatic
        fun show(context: Context, articleId: Int) {
            val intent = Intent(context, ArticleCommentsActivity::class.java)
            intent.putExtra(ARTICLE_ID, articleId)
            context.startActivity(intent)
        }
    }

    private lateinit var serviceLocator: ServiceLocator
    private var presenter: ArticleCommentsPresenterImpl? = null
    private lateinit var colorThemeResolver: ColorThemeResolver

    private lateinit var adapter: CommentsViewAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var articleId = 0

    override fun setServiceLocator(serviceLocator: ServiceLocator) {
        this.serviceLocator = serviceLocator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Normal)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_articlecomments)

        colorThemeResolver = ColorThemeResolver(TypedValue(), theme)

//        layoutButtonLike.setOnClickListener {
//            presenter?.onLikeClicked(articleId)
//        }
//        layoutButtonComment.setOnClickListener {
//            presenter?.onCommentClicked(articleId)
//        }
//        layoutButtonDislike.setOnClickListener {
//            presenter?.onDislikeClicked(articleId)
//        }
//        layoutButtonShare.setOnClickListener {
//            presenter?.onShareClicked(articleId)
//        }

        articleId = intent.getIntExtra(ArticleCommentsActivity.ARTICLE_ID, 0)
        if (articleId <= 0) {
            finish()
            return
        }

        initRecyclerView()

        val interactor = serviceLocator.get(ArticleCommentsInteractor::class.java)
        presenter = ArticleCommentsPresenterImpl(interactor)
        presenter?.bindView(this)
        presenter?.onFirstLoad(articleId)

        layoutAddCommentButton.setOnClickListener {
            presenter?.onAddCommentClicked(articleId, editTextComment.text.toString())
        }
    }

    override fun onPause() {
        presenter?.unbindView()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        presenter?.bindView(this)
    }

    override fun onDestroy() {
        presenter?.onDestroy()
        presenter = null
        serviceLocator.release(ArticleCommentsInteractor::class.java)
        super.onDestroy()
    }

    override fun showProgress() {
        progressbar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressbar.visibility = View.GONE
    }

    override fun showError() {
        Toast.makeText(this, "Ooops!", Toast.LENGTH_LONG).show()
    }

    override fun showComments(items: List<ArticleOrComment>) {
        adapter.setItems(items)
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        adapter = CommentsViewAdapter(
            this,
            presenter,
            colorThemeResolver,
            ArrayList()
        )
        recyclerView.adapter = adapter
    }

    class CommentsViewAdapter constructor(
        private val context: Context,
        private val callbackClickHandler: ArticleCallbackClickHandler?,
        private val colorThemeResolver: ColorThemeResolver,
        private val items: MutableList<ArticleOrComment>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val ARTICLE_DETAIL_VIEW_TYPE = 1
            const val COMMENT_VIEW_TYPE = 2
        }

        private val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) ARTICLE_DETAIL_VIEW_TYPE else COMMENT_VIEW_TYPE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == ARTICLE_DETAIL_VIEW_TYPE) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.row_comment_article, parent, false)
                return ArticleViewHolder(view)
            }
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_comment, parent, false)
            return CommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ArticleViewHolder) bindArticleViewHolder(holder, position)
            else bindCommentViewHolder(holder, position)
        }

        private fun bindCommentViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        }

        private fun bindArticleViewHolder(holder: ArticleViewHolder, position: Int) {
            val item = items[position] as Article

            val source = item.sourceName
            val title = item.title.trim()
            val url = item.pictureUrl

            val pubDate = item.pubDate
            holder.itemView.newsDateSource.text = String.format("%s %s", format.format(pubDate), source)

            holder.itemView.imageViewThumb.setImageResource(0)
            if (url.isNullOrEmpty()) {
                holder.itemView.imageViewThumb.visibility = View.GONE
            } else {
                holder.itemView.imageViewThumb.visibility = View.VISIBLE
                Glide.with(context).load(url).into(holder.itemView.imageViewThumb)
            }

            holder.itemView.newsTitle.text = title

            holder.itemView.textViewLike.text = item.usersLikeCount.toString()
            holder.itemView.textViewDislike.text = item.usersDislikeCount.toString()
//            holder.itemView.textViewComment.text = item.usersCommentCount.toString()

            colorThemeResolver.setArticleActivityColor(item.isUserLiked, holder.itemView.textViewLike, holder.itemView.imageViewLike)
            colorThemeResolver.setArticleActivityColor(item.isUserDisliked, holder.itemView.textViewDislike, holder.itemView.imageViewDislike)
//            colorThemeResolver.setArticleActivityColor(item.isUserCommented, holder.itemView.textViewComment, holder.itemView.imageViewComment)

            holder.itemView.layoutButtonLike.setOnClickListener {
                callbackClickHandler?.onLikeClicked(item)
            }

            holder.itemView.layoutButtonDislike.setOnClickListener {
                callbackClickHandler?.onDislikeClicked(item)
            }

            holder.itemView.layoutButtonShare.setOnClickListener {
                callbackClickHandler?.onShareClicked(item)
            }

        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun setItems(items: List<ArticleOrComment>) {
            this.items.clear()
            this.items.addAll(items)
            notifyDataSetChanged()
        }

    }

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}