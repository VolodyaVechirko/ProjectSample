package com.vvechirko.projectsample

import android.app.Application
import android.content.Context

class App: Application() {
    companion object {

        private lateinit var instance: App

        fun appContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initKoin()
    }
}