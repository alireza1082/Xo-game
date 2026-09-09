package com.example.android.xo.ads

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val DATASTORE_NAME = "xo_ads"
private const val CONFIG_KEY = "ad_provider"
private val Context.adsDataStore by preferencesDataStore(DATASTORE_NAME)

interface AdConfigRepository {
    val provider: Flow<AdProvider>
    suspend fun currentProvider(): AdProvider
    suspend fun saveProvider(provider: AdProvider)
    suspend fun refreshFromRemote(): AdProvider?
}

/**
 * Stores only the provider selector locally. Credentials and placements remain build/server
 * configuration and are never written to logs or the UI.
 */
class DataStoreAdConfigRepository(
    context: Context,
    private val remoteConfigUrl: String,
    private val defaultProvider: AdProvider = AdProvider.MIXED
) : AdConfigRepository {
    private val dataStore = context.applicationContext.adsDataStore

    override val provider: Flow<AdProvider> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences -> AdProvider.parse(preferences[Keys.provider] ?: defaultProvider.wireValue) }

    override suspend fun currentProvider(): AdProvider = provider.first()

    override suspend fun saveProvider(provider: AdProvider) {
        dataStore.edit { it[Keys.provider] = provider.wireValue }
    }

    override suspend fun refreshFromRemote(): AdProvider? {
        if (remoteConfigUrl.isBlank()) return null
        val value = withContext(Dispatchers.IO) {
            val connection = (URL(remoteConfigUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = REMOTE_TIMEOUT_MS
                readTimeout = REMOTE_TIMEOUT_MS
                instanceFollowRedirects = true
            }
            try {
                if (connection.responseCode !in 200..299) return@withContext null
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    .trim()
                    .takeIf { it.isNotEmpty() }
            } finally {
                connection.disconnect()
            }
        }
        val parsed = value?.let { raw ->
            AdProvider.entries.firstOrNull { it.wireValue == raw.lowercase() }
        } ?: return null
        saveProvider(parsed)
        return parsed
    }

    private object Keys {
        val provider: Preferences.Key<String> = stringPreferencesKey(CONFIG_KEY)
    }

    private companion object {
        const val REMOTE_TIMEOUT_MS = 4_000
    }
}
