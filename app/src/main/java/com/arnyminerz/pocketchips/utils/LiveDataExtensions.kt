package com.arnyminerz.pocketchips.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.locks.Lock
import kotlin.concurrent.withLock

/** Fetches the current value of the map at [key] using the [lock]. */
fun <K, V> MutableLiveData<Map<K, V>>.get(lock: Lock, key: K) =
    lock.withLock { (this.value ?: emptyMap())[key] }

/** Updates the given value at [key] using the [lock]. */
fun <K, V> MutableLiveData<Map<K, V>>.set(lock: Lock, key: K, value: V) {
    lock.withLock {
        val map = (this.value ?: emptyMap()).toMutableMap()
        map[key] = value
        postValue(map)
    }
}

/** Removes the value stored at [key] using the [lock] */
fun <K, V> MutableLiveData<Map<K, V>>.remove(lock: Lock, key: K) {
    lock.withLock {
        val map = (this.value ?: emptyMap()).toMutableMap()
        map.remove(key)
        postValue(map)
    }
}

/** Fetches the current value of the LiveData using the [lock]. */
fun <T> LiveData<T>.get(lock: Lock) = lock.withLock { this.value }
