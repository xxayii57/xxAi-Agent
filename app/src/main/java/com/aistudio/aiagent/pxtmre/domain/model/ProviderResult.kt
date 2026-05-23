package com.aistudio.aiagent.pxtmre.domain.model

sealed interface ProviderResult {
    data class Success(val message: String) : ProviderResult
    data class Failure(val message: String) : ProviderResult
}
