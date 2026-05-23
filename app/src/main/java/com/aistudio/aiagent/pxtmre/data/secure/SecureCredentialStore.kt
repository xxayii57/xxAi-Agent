package com.aistudio.aiagent.pxtmre.data.secure

import com.aistudio.aiagent.pxtmre.domain.model.CredentialProfile
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType

interface SecureCredentialStore {
    fun listProfiles(): List<CredentialProfile>
    fun saveProfile(profile: CredentialProfile)
    fun removeProfile(providerType: ProviderType)
    fun getActiveProvider(): ProviderType?
    fun setActiveProvider(providerType: ProviderType?)
}
