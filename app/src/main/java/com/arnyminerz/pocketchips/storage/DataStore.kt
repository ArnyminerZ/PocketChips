package com.arnyminerz.pocketchips.storage

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arnyminerz.pocketchips.BuildConfig

const val DATA_STORE_NAME = BuildConfig.APPLICATION_ID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(DATA_STORE_NAME)

/** Stores the name the user has chosen. Defaults to device name. */
val PREF_NAME: Pair<Preferences.Key<String>, String> =
    Pair(stringPreferencesKey("name"), (Build.MANUFACTURER + " " + Build.PRODUCT))
