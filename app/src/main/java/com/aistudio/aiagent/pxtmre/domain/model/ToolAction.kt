package com.aistudio.aiagent.pxtmre.domain.model

data class ToolAction(
    val id: String,
    val title: String,
    val description: String,
    val commandPreview: String,
)

data class ToolResult(
    val id: String,
    val title: String,
    val output: String,
    val timestamp: Long,
)
