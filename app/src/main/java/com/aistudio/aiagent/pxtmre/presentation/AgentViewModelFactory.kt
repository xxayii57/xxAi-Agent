package com.aistudio.aiagent.pxtmre.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aistudio.aiagent.pxtmre.domain.repository.AgentRepository

class AgentViewModelFactory(
    private val repository: AgentRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AgentViewModel::class.java))
        return AgentViewModel(repository) as T
    }
}
