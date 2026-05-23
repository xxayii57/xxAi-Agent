package com.aistudio.aiagent.pxtmre.domain.model

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: Long,
    val providerType: ProviderType?,
    val messages: List<AgentMessage>,
)
