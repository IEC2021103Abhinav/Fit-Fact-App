package com.androiddevs.Fitfact

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MainApplication :Application(){
//    Main application is inherit from Application,mark this application is injectable with daggerhilt
//    by annotations @HiltAndroidApp
//    all the dependencies in the app modules are created in this create function
//    the create function is already controlled by this dagger
//    they will exist whole life of our app
//    when the app is uninstall then the dependencies is uninstall
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
//    just enable debug with Timber library
    }

}