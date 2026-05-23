package com.aistudio.aiagent.pxtmre.data.service

import com.aistudio.aiagent.pxtmre.domain.model.CredentialProfile
import com.aistudio.aiagent.pxtmre.domain.model.ProviderMeta
import com.aistudio.aiagent.pxtmre.domain.model.ProviderResult
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType
import com.aistudio.aiagent.pxtmre.domain.service.ProviderGateway
import kotlinx.coroutines.delay

class GeminiGatewayStub : ProviderGateway {
    override val meta: ProviderMeta = ProviderMeta(
        type = ProviderType.GEMINI,
        defaultModel = "gemini-1.5-flash",
        officialLoginAvailable = false,
        officialLoginNote = "Gemini API juga aman dimulai dari encrypted API key profile. Phase 1 tidak pura-pura punya login akun resmi.",
    )

    override suspend fun verifyApiKey(profile: CredentialProfile): ProviderResult {
        delay(200)
        return if (!profile.encryptedSecret.isNullOrBlank()) {
            ProviderResult.Success("Profile Gemini tersimpan aman. Verifikasi real belum diaktifkan di Phase 1.")
        } else {
            ProviderResult.Failure("API key Gemini kosong.")
        }
    }

    override suspend fun startOfficialLogin(): ProviderResult {
        return ProviderResult.Failure(meta.officialLoginNote)
    }
}
