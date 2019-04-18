package ru.merkulyevsasha.news.presentation.splash

import io.reactivex.android.schedulers.AndroidSchedulers
import ru.merkulyevsasha.core.domain.SetupInteractor
import ru.merkulyevsasha.news.presentation.base.BasePresenter
import timber.log.Timber

class SplashPresenterImpl(private val setupInteractor: SetupInteractor) : BasePresenter<SplashView>() {
    fun onSetup(setupId: String, getFirebaseId: () -> String) {
        compositeDisposable.add(
            setupInteractor.registerSetup(setupId, getFirebaseId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { view?.showMainScreen() },
                    {
                        Timber.e(it)
                        view?.showFatalError()
                    }))
    }
}