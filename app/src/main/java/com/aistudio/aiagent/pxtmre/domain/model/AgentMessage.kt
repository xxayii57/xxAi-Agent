package com.aistudio.aiagent.pxtmre.domain.model

data class AgentMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
}
