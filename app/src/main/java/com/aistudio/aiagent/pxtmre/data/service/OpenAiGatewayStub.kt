package com.aistudio.aiagent.pxtmre.data.service

import com.aistudio.aiagent.pxtmre.domain.model.CredentialProfile
import com.aistudio.aiagent.pxtmre.domain.model.ProviderMeta
import com.aistudio.aiagent.pxtmre.domain.model.ProviderResult
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType
import com.aistudio.aiagent.pxtmre.domain.service.ProviderGateway
import kotlinx.coroutines.delay

class OpenAiGatewayStub : ProviderGateway {
    override val meta: ProviderMeta = ProviderMeta(
        type = ProviderType.OPENAI,
        defaultModel = "gpt-4.1-mini",
        officialLoginAvailable = false,
        officialLoginNote = "OpenAI public API umumnya dipakai dengan API key. Phase 1 tidak mengklaim ada login akun resmi untuk third-party native app.",
    )

    override suspend fun verifyApiKey(profile: CredentialProfile): ProviderResult {
        delay(200)
        return if (!profile.encryptedSecret.isNullOrBlank()) {
            ProviderResult.Success("Profile OpenAI tersimpan aman. Verifikasi real belum diaktifkan di Phase 1.")
        } else {
            ProviderResult.Failure("API key OpenAI kosong.")
        }
    }

    override suspend fun startOfficialLogin(): ProviderResult {
        return ProviderResult.Failure(meta.officialLoginNote)
    }
}
