package com.aistudio.aiagent.pxtmre.domain.model

data class ProviderMeta(
    val type: ProviderType,
    val defaultModel: String,
    val officialLoginAvailable: Boolean,
    val officialLoginNote: String,
)
