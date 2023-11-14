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
import androidx.lifecycle.viewModelScope
import com.gaurav.avnc.model.ServerProfile
import com.gaurav.avnc.model.db.MainDb
import com.gaurav.avnc.util.AppPreferences
import com.gaurav.avnc.viewmodel.PrefsViewModel
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import java.io.IOException

class App : Application() {

    @Keep
    lateinit var prefs: AppPreferences

    protected val db by lazy { MainDb.getInstance(this) }

    protected val serverProfileDao by lazy { db.serverProfileDao }

    override fun onCreate() {
        super.onCreate()

        prefs = AppPreferences(this)
        prefs.ui.theme.observeForever { updateNightMode(it) }

        val coroutineScope = CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            val profiles = serverProfileDao.getList()
            Log.e("TAGDEBUG", "onCreate: " + profiles.size )
            if(profiles.isEmpty()) {
                var profile = ServerProfile()
                profile.host = "10.42.102.244"
                profile.viewOnly = true
                profile.name = "Vehicle"

                serverProfileDao.insert(profile)
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
}