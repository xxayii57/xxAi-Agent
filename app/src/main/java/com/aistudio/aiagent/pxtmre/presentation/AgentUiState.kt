package com.aistudio.aiagent.pxtmre.presentation

import com.aistudio.aiagent.pxtmre.domain.model.ChatSession
import com.aistudio.aiagent.pxtmre.domain.model.ProviderStatus
import com.aistudio.aiagent.pxtmre.domain.model.ToolAction
import com.aistudio.aiagent.pxtmre.domain.model.ToolResult

data class AgentUiState(
    val providers: List<ProviderStatus> = emptyList(),
    val sessions: List<ChatSession> = emptyList(),
    val activeSessionId: String? = null,
    val tools: List<ToolAction> = emptyList(),
    val toolResults: List<ToolResult> = emptyList(),
    val message: String? = null,
    val loading: Boolean = false,
)
