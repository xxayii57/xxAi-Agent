package com.aistudio.aiagent.pxtmre.data

import com.aistudio.aiagent.pxtmre.data.local.LocalBridgeStore
import com.aistudio.aiagent.pxtmre.data.local.LocalSessionStore
import com.aistudio.aiagent.pxtmre.data.secure.SecureCredentialStore
import com.aistudio.aiagent.pxtmre.domain.model.AgentMessage
import com.aistudio.aiagent.pxtmre.domain.model.AuthType
import com.aistudio.aiagent.pxtmre.domain.model.ChatSession
import com.aistudio.aiagent.pxtmre.domain.model.CredentialProfile
import com.aistudio.aiagent.pxtmre.domain.model.MessageRole
import com.aistudio.aiagent.pxtmre.domain.model.ProviderResult
import com.aistudio.aiagent.pxtmre.domain.model.ProviderStatus
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType
import com.aistudio.aiagent.pxtmre.domain.model.ToolAction
import com.aistudio.aiagent.pxtmre.domain.repository.AgentRepository
import com.aistudio.aiagent.pxtmre.domain.service.ProviderGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class DefaultAgentRepository(
    private val credentialStore: SecureCredentialStore,
    private val sessionStore: LocalSessionStore,
    private val bridgeStore: LocalBridgeStore,
    private val gateways: List<ProviderGateway>,
) : AgentRepository {
    private val _sessions = MutableStateFlow(loadInitialSessions())
    override val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow(_sessions.value.firstOrNull()?.id)
    override val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val _providerStatuses = MutableStateFlow(buildProviderStatuses())
    override val providerStatuses: StateFlow<List<ProviderStatus>> = _providerStatuses.asStateFlow()

    override val toolResults = bridgeStore.toolResults

    override val toolActions: List<ToolAction> = listOf(
        ToolAction(
            id = "bridge_health",
            title = "Bridge Health Check",
            description = "Cek local runtime / bridge host di device.",
            commandPreview = "agent_host health-check",
        ),
        ToolAction(
            id = "sandbox_files",
            title = "List Sandbox Files",
            description = "Simulasi lihat storage sandbox app lokal.",
            commandPreview = "agent_host list-sandbox",
        ),
        ToolAction(
            id = "mock_sync",
            title = "Mock Provider Sync",
            description = "Simulasi sync status provider ke dashboard host.",
            commandPreview = "agent_host sync-providers",
        ),
    )

    override suspend fun createSession(title: String?): String {
        val provider = currentActiveProvider()
        val session = ChatSession(
            id = UUID.randomUUID().toString(),
            title = title ?: "Sesi ${_sessions.value.size + 1}",
            createdAt = System.currentTimeMillis(),
            providerType = provider,
            messages = listOf(
                AgentMessage(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.SYSTEM,
                    content = "Session dibuat. Host agent berjalan lokal di HP. Provider real masih bisa dipasang bertahap.",
                    timestamp = System.currentTimeMillis(),
                ),
            ),
        )
        _sessions.update { listOf(session) + it }
        _activeSessionId.value = session.id
        persistSessions()
        return session.id
    }

    override suspend fun deleteSession(sessionId: String) {
        _sessions.update { list -> list.filterNot { it.id == sessionId } }
        if (_activeSessionId.value == sessionId) {
            _activeSessionId.value = _sessions.value.firstOrNull()?.id
        }
        if (_sessions.value.isEmpty()) {
            createSession("Sesi Baru")
        } else {
            persistSessions()
        }
    }

    override suspend fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
    }

    override suspend fun sendMessage(text: String): ProviderResult {
        if (text.isBlank()) return ProviderResult.Failure("Pesan kosong.")
        val sessionId = _activeSessionId.value ?: createSession()
        appendMessage(sessionId, MessageRole.USER, text)

        val activeProvider = _providerStatuses.value.firstOrNull { it.active && it.connected }
        val assistantReply = buildString {
            appendLine("Host agent menerima tugas.")
            appendLine("Mode: local mobile-first runtime")
            appendLine("Provider aktif: ${activeProvider?.providerType?.label ?: "belum terkoneksi"}")
            appendLine("Model aktif: ${activeProvider?.model ?: "stub-local"}")
            appendLine()
            append("Phase 1 masih pakai stub respons. Langkah berikutnya adalah sambungkan chat ini ke gateway provider real atau bridge lokal sesuai kebutuhan.")
        }
        appendMessage(sessionId, MessageRole.ASSISTANT, assistantReply)
        return ProviderResult.Success("Pesan diproses.")
    }

    override suspend fun connectWithApiKey(providerType: ProviderType, apiKey: String, model: String): ProviderResult {
        if (apiKey.isBlank()) return ProviderResult.Failure("API key wajib diisi.")
        val gateway = gateways.first { it.supports(providerType) }
        val profile = CredentialProfile(
            providerType = providerType,
            authType = AuthType.ENCRYPTED_API_KEY,
            model = model.ifBlank { gateway.meta.defaultModel },
            encryptedSecret = apiKey,
            accountLabel = "API key profile",
        )
        val result = gateway.verifyApiKey(profile)
        if (result is ProviderResult.Success) {
            credentialStore.saveProfile(profile)
            credentialStore.setActiveProvider(providerType)
            refreshProviderStatuses()
        }
        return result
    }

    override suspend fun connectWithOfficialAccount(providerType: ProviderType): ProviderResult {
        val result = gateways.first { it.supports(providerType) }.startOfficialLogin()
        refreshProviderStatuses()
        return result
    }

    override suspend fun disconnectProvider(providerType: ProviderType): ProviderResult {
        credentialStore.removeProfile(providerType)
        refreshProviderStatuses()
        return ProviderResult.Success("${providerType.label} diputus.")
    }

    override suspend fun activateProvider(providerType: ProviderType): ProviderResult {
        val connected = credentialStore.listProfiles().firstOrNull { it.providerType == providerType }
            ?: return ProviderResult.Failure("${providerType.label} belum terkoneksi.")
        credentialStore.setActiveProvider(connected.providerType)
        refreshProviderStatuses()
        return ProviderResult.Success("${providerType.label} jadi provider aktif.")
    }

    override suspend fun runTool(toolId: String): ProviderResult {
        val tool = toolActions.firstOrNull { it.id == toolId }
            ?: return ProviderResult.Failure("Tool tidak ditemukan.")
        val result = bridgeStore.runTool(tool.title, tool.commandPreview)
        val sessionId = _activeSessionId.value ?: createSession()
        appendMessage(
            sessionId = sessionId,
            role = MessageRole.SYSTEM,
            content = "${result.title}\n\n${result.output}",
        )
        return ProviderResult.Success("${tool.title} dijalankan.")
    }

    private fun loadInitialSessions(): List<ChatSession> {
        val existing = sessionStore.listSessions()
        return if (existing.isNotEmpty()) existing else listOf(
            ChatSession(
                id = UUID.randomUUID().toString(),
                title = "Sesi 1",
                createdAt = System.currentTimeMillis(),
                providerType = null,
                messages = listOf(
                    AgentMessage(
                        id = UUID.randomUUID().toString(),
                        role = MessageRole.SYSTEM,
                        content = "Dashboard host agent siap. Phase 1 fokus ke local runtime, session, tools, dan provider auth yang jujur.",
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            ),
        )
    }

    private fun appendMessage(sessionId: String, role: MessageRole, content: String) {
        _sessions.update { list ->
            list.map { session ->
                if (session.id != sessionId) {
                    session
                } else {
                    session.copy(
                        messages = session.messages + AgentMessage(
                            id = UUID.randomUUID().toString(),
                            role = role,
                            content = content,
                            timestamp = System.currentTimeMillis(),
                        ),
                    )
                }
            }
        }
        persistSessions()
    }

    private fun persistSessions() {
        sessionStore.saveSessions(_sessions.value)
    }

    private fun refreshProviderStatuses() {
        _providerStatuses.value = buildProviderStatuses()
        val active = currentActiveProvider()
        _sessions.update { list ->
            list.map { session ->
                if (session.id == _activeSessionId.value) {
                    session.copy(providerType = active)
                } else {
                    session
                }
            }
        }
        persistSessions()
    }

    private fun buildProviderStatuses(): List<ProviderStatus> {
        val profiles = credentialStore.listProfiles()
        val activeProvider = credentialStore.getActiveProvider()
        return gateways.map { gateway ->
            val profile = profiles.firstOrNull { it.providerType == gateway.meta.type }
            ProviderStatus(
                providerType = gateway.meta.type,
                connected = profile != null,
                active = activeProvider == gateway.meta.type,
                authType = profile?.authType,
                model = profile?.model ?: gateway.meta.defaultModel,
                accountLabel = profile?.accountLabel,
                officialLoginAvailable = gateway.meta.officialLoginAvailable,
                officialLoginNote = gateway.meta.officialLoginNote,
            )
        }
    }

    private fun currentActiveProvider(): ProviderType? {
        return _providerStatuses.value.firstOrNull { it.active && it.connected }?.providerType
    }
}
