package com.aistudio.aiagent.pxtmre.data.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aistudio.aiagent.pxtmre.domain.model.AuthType
import com.aistudio.aiagent.pxtmre.domain.model.CredentialProfile
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EncryptedCredentialStore(context: Context) : SecureCredentialStore {
    private val gson = Gson()
    private val preferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun listProfiles(): List<CredentialProfile> {
        val raw = preferences.getString(KEY_PROFILES, null) ?: return emptyList()
        val type = object : TypeToken<List<StoredCredentialProfile>>() {}.type
        return gson.fromJson<List<StoredCredentialProfile>>(raw, type).map { it.toDomain() }
    }

    override fun saveProfile(profile: CredentialProfile) {
        val updated = listProfiles()
            .filterNot { it.providerType == profile.providerType }
            .plus(profile)
            .map { StoredCredentialProfile.fromDomain(it) }
        preferences.edit().putString(KEY_PROFILES, gson.toJson(updated)).apply()
    }

    override fun removeProfile(providerType: ProviderType) {
        val updated = listProfiles()
            .filterNot { it.providerType == providerType }
            .map { StoredCredentialProfile.fromDomain(it) }
        preferences.edit().putString(KEY_PROFILES, gson.toJson(updated)).apply()
        if (getActiveProvider() == providerType) {
            setActiveProvider(null)
        }
    }

    override fun getActiveProvider(): ProviderType? {
        val value = preferences.getString(KEY_ACTIVE_PROVIDER, null) ?: return null
        return ProviderType.entries.firstOrNull { it.name == value }
    }

    override fun setActiveProvider(providerType: ProviderType?) {
        preferences.edit().putString(KEY_ACTIVE_PROVIDER, providerType?.name).apply()
    }

    private data class StoredCredentialProfile(
        val providerType: String,
        val authType: String,
        val model: String,
        val encryptedSecret: String?,
        val accountLabel: String?,
    ) {
        fun toDomain(): CredentialProfile = CredentialProfile(
            providerType = ProviderType.valueOf(providerType),
            authType = AuthType.valueOf(authType),
            model = model,
            encryptedSecret = encryptedSecret,
            accountLabel = accountLabel,
        )

        companion object {
            fun fromDomain(profile: CredentialProfile): StoredCredentialProfile = StoredCredentialProfile(
                providerType = profile.providerType.name,
                authType = profile.authType.name,
                model = profile.model,
                encryptedSecret = profile.encryptedSecret,
                accountLabel = profile.accountLabel,
            )
        }
    }

    private companion object {
        const val FILE_NAME = "agent_host_secure"
        const val KEY_PROFILES = "profiles"
        const val KEY_ACTIVE_PROVIDER = "active_provider"
    }
}
