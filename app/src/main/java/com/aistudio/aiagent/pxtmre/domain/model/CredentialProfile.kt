package com.aistudio.aiagent.pxtmre.domain.model

data class CredentialProfile(
    val providerType: ProviderType,
    val authType: AuthType,
    val model: String,
    val encryptedSecret: String?,
    val accountLabel: String?,
)
