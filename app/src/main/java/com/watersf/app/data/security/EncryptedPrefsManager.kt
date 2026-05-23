package com.watersf.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.watersf.app.domain.model.User
import com.watersf.app.domain.model.UserType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPrefsManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_water_sf_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_JWT_TOKEN = "key_jwt_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_TYPE = "key_user_type"
        private const val KEY_USER_NIS = "key_user_nis"
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_JWT_TOKEN, null)
    }

    fun saveUser(user: User) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_TYPE, user.type.name)
            putString(KEY_USER_NIS, user.nis)
            apply()
        }
    }

    fun getUser(): User? {
        val id = sharedPreferences.getString(KEY_USER_ID, null) ?: return null
        val name = sharedPreferences.getString(KEY_USER_NAME, null) ?: return null
        val email = sharedPreferences.getString(KEY_USER_EMAIL, null) ?: return null
        val typeStr = sharedPreferences.getString(KEY_USER_TYPE, null) ?: return null
        val nis = sharedPreferences.getString(KEY_USER_NIS, null)

        return User(
            id = id,
            name = name,
            email = email,
            type = UserType.fromString(typeStr),
            nis = nis
        )
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}