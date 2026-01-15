package com.ipca.lojasocial

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LojaSocialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
