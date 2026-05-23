package com.aistudio.aiagent.pxtmre.domain.service

import com.aistudio.aiagent.pxtmre.domain.model.CredentialProfile
import com.aistudio.aiagent.pxtmre.domain.model.ProviderMeta
import com.aistudio.aiagent.pxtmre.domain.model.ProviderResult
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType

interface ProviderGateway {
    val meta: ProviderMeta
    fun supports(providerType: ProviderType): Boolean = meta.type == providerType
    suspend fun verifyApiKey(profile: CredentialProfile): ProviderResult
    suspend fun startOfficialLogin(): ProviderResult
}
