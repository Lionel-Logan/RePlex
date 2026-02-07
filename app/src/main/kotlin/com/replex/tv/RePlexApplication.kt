package com.replex.tv

import android.app.Application
import timber.log.Timber

/**
 * RePlex Application class
 */
class RePlexApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
        
        Timber.d("RePlex Application started")
    }
}
