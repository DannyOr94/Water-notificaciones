package com.watersf.app.data.security

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
    // Use Provider to avoid circular dependency since PrefsManager is injected in AuthRepositoryImpl which is injected in network modules.
    private val prefsManagerProvider: Provider<EncryptedPrefsManager>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val token = prefsManagerProvider.get().getToken()
        if (!token.isNullOrBlank()) {
            builder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(builder.build())

        // Handle 401 Unauthorized globally if token is expired
        if (response.code == 401) {
            prefsManagerProvider.get().clearSession()
            // Optionally, trigger an event or redirect to Login screen
        }

        return response
    }
}
