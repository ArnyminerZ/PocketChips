package com.arnyminerz.pocketchips.utils

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val AndroidViewModel.context: Context get() = getApplication()

fun ViewModel.async(@WorkerThread block: suspend CoroutineScope.() -> Unit): Job =
    viewModelScope.launch {
        withContext(Dispatchers.IO, block = block)
    }
