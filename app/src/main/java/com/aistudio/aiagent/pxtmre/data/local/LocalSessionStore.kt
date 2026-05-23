package com.aistudio.aiagent.pxtmre.data.local

import android.content.Context
import com.aistudio.aiagent.pxtmre.domain.model.AgentMessage
import com.aistudio.aiagent.pxtmre.domain.model.ChatSession
import com.aistudio.aiagent.pxtmre.domain.model.MessageRole
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun listSessions(): List<ChatSession> {
        val raw = preferences.getString(KEY_SESSIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<StoredSession>>() {}.type
        return gson.fromJson<List<StoredSession>>(raw, type).map { it.toDomain() }
    }

    fun saveSessions(sessions: List<ChatSession>) {
        val stored = sessions.map { StoredSession.fromDomain(it) }
        preferences.edit().putString(KEY_SESSIONS, gson.toJson(stored)).apply()
    }

    private data class StoredSession(
        val id: String,
        val title: String,
        val createdAt: Long,
        val providerType: String?,
        val messages: List<StoredMessage>,
    ) {
        fun toDomain(): ChatSession = ChatSession(
            id = id,
            title = title,
            createdAt = createdAt,
            providerType = providerType?.let { ProviderType.valueOf(it) },
            messages = messages.map { it.toDomain() },
        )

        companion object {
            fun fromDomain(session: ChatSession): StoredSession = StoredSession(
                id = session.id,
                title = session.title,
                createdAt = session.createdAt,
                providerType = session.providerType?.name,
                messages = session.messages.map { StoredMessage.fromDomain(it) },
            )
        }
    }

    private data class StoredMessage(
        val id: String,
        val role: String,
        val content: String,
        val timestamp: Long,
    ) {
        fun toDomain(): AgentMessage = AgentMessage(
            id = id,
            role = MessageRole.valueOf(role),
            content = content,
            timestamp = timestamp,
        )

        companion object {
            fun fromDomain(message: AgentMessage): StoredMessage = StoredMessage(
                id = message.id,
                role = message.role.name,
                content = message.content,
                timestamp = message.timestamp,
            )
        }
    }

    private companion object {
        const val FILE_NAME = "agent_host_sessions"
        const val KEY_SESSIONS = "sessions"
    }
}
