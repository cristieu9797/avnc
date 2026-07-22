/*
 * Copyright (c) 2021  Gaurav Ujjwal.
 *
 * SPDX-License-Identifier:  GPL-3.0-or-later
 *
 * See COPYING.txt for more details.
 */

package com.gaurav.avnc

import android.app.Application
import android.util.Log
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import com.gaurav.avnc.model.ServerProfile
import com.gaurav.avnc.model.db.MainDb
import com.gaurav.avnc.util.AppPreferences
import com.squareup.leakcanary.core.BuildConfig
import kotlinx.coroutines.*


class App : Application() {

    @Keep
    lateinit var prefs: AppPreferences

    protected val db by lazy { MainDb.getInstance(this) }

    protected val serverProfileDao by lazy { db.serverProfileDao }

    override fun onCreate() {
        super.onCreate()
        configureLeakCanary()

        prefs = AppPreferences(this)
        prefs.ui.theme.observeForever { updateNightMode(it) }

        val coroutineScope = CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            val profiles = serverProfileDao.getList()
            Log.e("TAGDEBUG", "onCreate: " + profiles.size )
            if(profiles.isEmpty()) {
                var profile = ServerProfile()
                profile.host = "10.42.102.244"
                profile.name = "Vehicle"

                serverProfileDao.save(profile)
            }
        }



    }

    private fun updateNightMode(theme: String) {
        val nightMode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun configureLeakCanary() {
        if (BuildConfig.DEBUG) {
            Class.forName("com.gaurav.avnc.LeakCanaryInitializer")
                    .getMethod("initialize", Application::class.java)
                    .invoke(null, this)
        }
    }
}