package com.aistudio.aiagent.pxtmre.domain.model

data class ProviderStatus(
    val providerType: ProviderType,
    val connected: Boolean,
    val active: Boolean,
    val authType: AuthType?,
    val model: String,
    val accountLabel: String?,
    val officialLoginAvailable: Boolean,
    val officialLoginNote: String,
)
