package com.aistudio.aiagent.pxtmre.domain.repository

import com.aistudio.aiagent.pxtmre.domain.model.ChatSession
import com.aistudio.aiagent.pxtmre.domain.model.ProviderResult
import com.aistudio.aiagent.pxtmre.domain.model.ProviderStatus
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType
import com.aistudio.aiagent.pxtmre.domain.model.ToolAction
import com.aistudio.aiagent.pxtmre.domain.model.ToolResult
import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    val providerStatuses: Flow<List<ProviderStatus>>
    val sessions: Flow<List<ChatSession>>
    val activeSessionId: Flow<String?>
    val toolResults: Flow<List<ToolResult>>
    val toolActions: List<ToolAction>

    suspend fun createSession(title: String? = null): String
    suspend fun deleteSession(sessionId: String)
    suspend fun selectSession(sessionId: String)
    suspend fun sendMessage(text: String): ProviderResult

    suspend fun connectWithApiKey(providerType: ProviderType, apiKey: String, model: String): ProviderResult
    suspend fun connectWithOfficialAccount(providerType: ProviderType): ProviderResult
    suspend fun disconnectProvider(providerType: ProviderType): ProviderResult
    suspend fun activateProvider(providerType: ProviderType): ProviderResult

    suspend fun runTool(toolId: String): ProviderResult
}
