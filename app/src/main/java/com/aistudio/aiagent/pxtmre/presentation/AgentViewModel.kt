package com.aistudio.aiagent.pxtmre.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.aiagent.pxtmre.domain.model.ProviderResult
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType
import com.aistudio.aiagent.pxtmre.domain.repository.AgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AgentViewModel(
    private val repository: AgentRepository,
) : ViewModel() {
    private val transientState = MutableStateFlow(AgentUiState())

    val uiState: StateFlow<AgentUiState> = combine(
        repository.providerStatuses,
        repository.sessions,
        repository.activeSessionId,
        repository.toolResults,
        transientState,
    ) { providers, sessions, activeSessionId, toolResults, transient ->
        transient.copy(
            providers = providers,
            sessions = sessions,
            activeSessionId = activeSessionId,
            tools = repository.toolActions,
            toolResults = toolResults,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AgentUiState(tools = repository.toolActions),
    )

    fun createSession() {
        runAction { repository.createSession(); ProviderResult.Success("Session baru dibuat.") }
    }

    fun deleteSession(sessionId: String) {
        runAction { repository.deleteSession(sessionId); ProviderResult.Success("Session dihapus.") }
    }

    fun selectSession(sessionId: String) {
        viewModelScope.launch { repository.selectSession(sessionId) }
    }

    fun sendMessage(text: String) {
        runAction { repository.sendMessage(text) }
    }

    fun connectWithApiKey(providerType: ProviderType, apiKey: String, model: String) {
        runAction { repository.connectWithApiKey(providerType, apiKey, model) }
    }

    fun connectWithOfficialAccount(providerType: ProviderType) {
        runAction { repository.connectWithOfficialAccount(providerType) }
    }

    fun disconnectProvider(providerType: ProviderType) {
        runAction { repository.disconnectProvider(providerType) }
    }

    fun activateProvider(providerType: ProviderType) {
        runAction { repository.activateProvider(providerType) }
    }

    fun runTool(toolId: String) {
        runAction { repository.runTool(toolId) }
    }

    fun consumeMessage() {
        transientState.update { it.copy(message = null) }
    }

    private fun runAction(block: suspend () -> ProviderResult) {
        viewModelScope.launch {
            transientState.update { it.copy(loading = true, message = null) }
            val result = block()
            transientState.update {
                it.copy(
                    loading = false,
                    message = when (result) {
                        is ProviderResult.Success -> result.message
                        is ProviderResult.Failure -> result.message
                    },
                )
            }
        }
    }
}
