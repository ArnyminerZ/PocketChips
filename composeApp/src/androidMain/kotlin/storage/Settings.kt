package storage

import com.arnyminerz.pocketchips.applicationContext
import com.arnyminerz.pocketchips.dataStore
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toBlockingObservableSettings
import com.russhwolf.settings.datastore.DataStoreSettings

@ExperimentalSettingsApi
@OptIn(ExperimentalSettingsImplementation::class)
actual val settings: ObservableSettings by lazy {
    DataStoreSettings(applicationContext.dataStore).toBlockingObservableSettings()
}
