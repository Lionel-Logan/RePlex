package com.replex.tv

import android.app.Application
import android.util.Log

/**
 * RePlex Application class
 */
class RePlexApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.i("RePlex", "RePlexApplication onCreate called")
    }
}
