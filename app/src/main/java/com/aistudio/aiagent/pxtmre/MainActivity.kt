package com.aistudio.aiagent.pxtmre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aistudio.aiagent.pxtmre.data.DefaultAgentRepository
import com.aistudio.aiagent.pxtmre.data.local.LocalBridgeStore
import com.aistudio.aiagent.pxtmre.data.local.LocalSessionStore
import com.aistudio.aiagent.pxtmre.data.secure.EncryptedCredentialStore
import com.aistudio.aiagent.pxtmre.data.service.GeminiGatewayStub
import com.aistudio.aiagent.pxtmre.data.service.OpenAiGatewayStub
import com.aistudio.aiagent.pxtmre.presentation.AgentViewModel
import com.aistudio.aiagent.pxtmre.presentation.AgentViewModelFactory
import com.aistudio.aiagent.pxtmre.presentation.ui.AgentHostApp
import com.aistudio.aiagent.pxtmre.presentation.ui.theme.AgentHostTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = DefaultAgentRepository(
            credentialStore = EncryptedCredentialStore(applicationContext),
            sessionStore = LocalSessionStore(applicationContext),
            bridgeStore = LocalBridgeStore(),
            gateways = listOf(
                OpenAiGatewayStub(),
                GeminiGatewayStub(),
            ),
        )

        setContent {
            AgentHostTheme {
                val viewModel: AgentViewModel = viewModel(
                    factory = AgentViewModelFactory(repository),
                )
                val state by viewModel.uiState.collectAsState()
                AgentHostApp(
                    state = state,
                    onSelectSession = viewModel::selectSession,
                    onCreateSession = viewModel::createSession,
                    onDeleteSession = viewModel::deleteSession,
                    onSendMessage = viewModel::sendMessage,
                    onConnectApiKey = viewModel::connectWithApiKey,
                    onConnectOfficial = viewModel::connectWithOfficialAccount,
                    onDisconnectProvider = viewModel::disconnectProvider,
                    onActivateProvider = viewModel::activateProvider,
                    onRunTool = viewModel::runTool,
                    onDismissMessage = viewModel::consumeMessage,
                )
            }
        }
    }
}
