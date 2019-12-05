package ru.merkulyevsasha.coreandroid.base

import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenterImpl<T : BaseView> {

    protected var view: T? = null
    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val commandViewHolder = CommandViewHolder()

    fun bindView(view: T) {
        this.view = view
        commandViewHolder.execute()
    }

    fun unbindView() {
        this.view = null
    }

    fun onDestroy() {
        compositeDisposable.dispose()
    }

    fun addCommand(f: () -> Unit) {
        commandViewHolder.addCommand(f)
    }

    inner class CommandViewHolder {
        private val commands = mutableListOf<ViewCommand>()

        fun execute() {
            commands.forEach {
                it.execute()
                commands.remove(it)
            }
        }

        fun addCommand(f: () -> Unit) {
            if (view == null) {
                commands.add(object : ViewCommand {
                    override fun execute() {
                        f()
                    }
                })
            } else {
                f()
            }
        }

    }
}

interface ViewCommand {
    fun execute()
}