package com.aistudio.aiagent.pxtmre.data.local

import com.aistudio.aiagent.pxtmre.domain.model.ToolResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LocalBridgeStore {
    private val _toolResults = MutableStateFlow<List<ToolResult>>(emptyList())
    val toolResults: StateFlow<List<ToolResult>> = _toolResults.asStateFlow()

    fun runTool(title: String, commandPreview: String): ToolResult {
        val timestamp = System.currentTimeMillis()
        val timeLabel = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(timestamp))
        val result = ToolResult(
            id = UUID.randomUUID().toString(),
            title = title,
            output = buildString {
                appendLine("Local bridge stub dijalankan.")
                appendLine("Waktu: $timeLabel")
                appendLine("Command preview: $commandPreview")
                append("Catatan: Phase 1 belum mengklaim eksekusi provider/tool resmi. Ini masih host runtime lokal yang aman.")
            },
            timestamp = timestamp,
        )
        _toolResults.update { listOf(result) + it }
        return result
    }
}
