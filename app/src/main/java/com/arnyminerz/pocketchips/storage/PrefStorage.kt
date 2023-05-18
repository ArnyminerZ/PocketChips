package com.arnyminerz.pocketchips.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Provides utility functions to get and set values into the device's preferences.
 *
 * Note: this is a singleton, access it with [PrefStorage.getInstance].
 */
class PrefStorage private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: PrefStorage? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: PrefStorage(context).also { instance = it }
        }
    }

    private val dataStore = context.dataStore

    fun <T> get(key: Preferences.Key<T>, default: T): Flow<T> = dataStore.data.map { it[key] ?: default }

    operator fun <T> get(key: Preferences.Key<T>): Flow<T?> = dataStore.data.map { it[key] }

    operator fun <T> get(key: Pair<Preferences.Key<T>, T>): Flow<T> = get(key.first, key.second)

    /**
     * Updates the value stored at the given key. If `null` is passed, the stored value (if any) is
     * removed.
     */
    suspend fun <T> set(key: Preferences.Key<T>, value: T?): PrefStorage {
        dataStore.edit {
            if (value == null)
                it.remove(key)
            else
                it[key] = value
        }
        return this
    }

    /**
     * Updates the value stored at the given key. If `null` is passed, the stored value (if any) is
     * removed.
     */
    suspend fun <T> set(key: Pair<Preferences.Key<T>, T>, value: T?): PrefStorage = set(key.first, value)
}
