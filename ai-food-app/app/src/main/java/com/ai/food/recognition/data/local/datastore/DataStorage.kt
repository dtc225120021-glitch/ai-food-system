package com.ai.food.recognition.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ai.food.recognition.domain.model.local.LocalStorage
import com.ai.food.recognition.ext.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = Constants.APP_NAME_DATASTORE)

class DataStoreManager(
    private val context: Context
) : LocalStorage {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("key_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("key_refresh_token")
        private val KEY_IS_LOGIN = booleanPreferencesKey("key_is_login")
    }

    override val token: Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_TOKEN]
        }

    override val refreshToken: Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_REFRESH_TOKEN]
        }

    override val isLogin: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_IS_LOGIN] ?: false
        }

    override suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    override suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REFRESH_TOKEN] = token
        }
    }

    override suspend fun setLogin(isLogin: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGIN] = isLogin
        }
    }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}