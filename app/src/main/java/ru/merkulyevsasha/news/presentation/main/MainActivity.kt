package ru.merkulyevsasha.news.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.bottomNav
import kotlinx.android.synthetic.main.activity_main.drawer
import kotlinx.android.synthetic.main.activity_main.navigation
import ru.merkulyevsasha.apprate.AppRateRequester
import ru.merkulyevsasha.core.domain.SetupInteractor
import ru.merkulyevsasha.news.BuildConfig
import ru.merkulyevsasha.news.NewsApp
import ru.merkulyevsasha.news.R
import ru.merkulyevsasha.news.presentation.common.MainActivityRouterImpl
import ru.merkulyevsasha.news.presentation.common.ShowActionBarListener
import ru.merkulyevsasha.news.presentation.common.ToolbarCombinator

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener, MainView,
    ToolbarCombinator, ShowActionBarListener {

    companion object {
        @JvmStatic
        fun show(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var mainActivityRouter: MainActivityRouterImpl
    private lateinit var presenter: MainPresenter

    private val navigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_articles -> {
                    mainActivityRouter.showArticles()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_actions -> {
                    mainActivityRouter.showUserActivities()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_user -> {
                    mainActivityRouter.showUserInfo()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Normal)
        super.onCreate(savedInstanceState)

        mainActivityRouter = MainActivityRouterImpl(applicationContext, supportFragmentManager)

        setContentView(R.layout.activity_main)
        navigation.setNavigationItemSelectedListener(this)

        val serviceLocator = (application as NewsApp).getServiceLocator()
        val interactor = serviceLocator.get(SetupInteractor::class.java)
        presenter = MainPresenter(interactor)

        AppRateRequester.Run(this, BuildConfig.APPLICATION_ID)

        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener)

//        val adRequestBuilder = AdRequest.Builder()
//        if (BuildConfig.DEBUG_MODE) {
//            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//        }
//        adRequestBuilder.addTestDevice("349C53FFD0654BDC5FF7D3D9254FC8E6").build()
//        adView.loadAd(adRequestBuilder.build())

        if (savedInstanceState == null) {
            mainActivityRouter.showArticles()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onPause() {
//        if (adView != null) {
//            adView.pause()
//        }
        presenter.unbindView()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
//        adView.resume()
        presenter.bindView(this)
    }

    override fun onDestroy() {
//        if (adView != null) {
//            adView.destroy()
//        }
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
//        if (supportFragmentManager.fragments.size <= 1) {
        finish()
//        } else {
//            super.onBackPressed()
//        }
    }

    override fun combine(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onShowActionBar(show: Boolean) {
        if (show) {
            supportActionBar?.show()
            bottomNav.visibility = View.VISIBLE
        } else {
            supportActionBar?.hide()
            bottomNav.visibility = View.GONE
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

}
