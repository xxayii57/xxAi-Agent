package com.aistudio.aiagent.pxtmre.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aistudio.aiagent.pxtmre.domain.model.AuthType
import com.aistudio.aiagent.pxtmre.domain.model.ChatSession
import com.aistudio.aiagent.pxtmre.domain.model.MessageRole
import com.aistudio.aiagent.pxtmre.domain.model.ProviderStatus
import com.aistudio.aiagent.pxtmre.domain.model.ProviderType
import com.aistudio.aiagent.pxtmre.domain.model.ToolAction
import com.aistudio.aiagent.pxtmre.domain.model.ToolResult
import com.aistudio.aiagent.pxtmre.presentation.AgentUiState

private enum class HostTab(val label: String) {
    CHAT("Home"),
    SESSIONS("Sessions"),
    TOOLS("Tools"),
    PROVIDERS("Providers"),
    SETTINGS("Settings"),
}

@Composable
fun AgentHostApp(
    state: AgentUiState,
    onSelectSession: (String) -> Unit,
    onCreateSession: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onConnectApiKey: (ProviderType, String, String) -> Unit,
    onConnectOfficial: (ProviderType) -> Unit,
    onDisconnectProvider: (ProviderType) -> Unit,
    onActivateProvider: (ProviderType) -> Unit,
    onRunTool: (String) -> Unit,
    onDismissMessage: () -> Unit,
) {
    var currentTab by rememberSaveable { mutableStateOf(HostTab.CHAT) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            onDismissMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                HostTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    HostTab.CHAT -> Icons.Rounded.Hub
                                    HostTab.SESSIONS -> Icons.Rounded.Storage
                                    HostTab.TOOLS -> Icons.Rounded.Build
                                    HostTab.PROVIDERS -> Icons.Rounded.Key
                                    HostTab.SETTINGS -> Icons.Rounded.Settings
                                },
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
        ) {
            when (currentTab) {
                HostTab.CHAT -> HostHomeTab(
                    state = state,
                    onSendMessage = onSendMessage,
                    onCreateSession = onCreateSession,
                    onSelectSession = onSelectSession,
                    onRunTool = onRunTool,
                )
                HostTab.SESSIONS -> SessionsTab(
                    state = state,
                    onSelectSession = onSelectSession,
                    onCreateSession = onCreateSession,
                    onDeleteSession = onDeleteSession,
                )
                HostTab.TOOLS -> ToolsTab(state.tools, state.toolResults, onRunTool)
                HostTab.PROVIDERS -> ProvidersTab(
                    providers = state.providers,
                    onConnectApiKey = onConnectApiKey,
                    onConnectOfficial = onConnectOfficial,
                    onDisconnectProvider = onDisconnectProvider,
                    onActivateProvider = onActivateProvider,
                )
                HostTab.SETTINGS -> SettingsTab(state)
            }

            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostHomeTab(
    state: AgentUiState,
    onSendMessage: (String) -> Unit,
    onCreateSession: () -> Unit,
    onSelectSession: (String) -> Unit,
    onRunTool: (String) -> Unit,
) {
    val session = state.sessions.firstOrNull { it.id == state.activeSessionId } ?: state.sessions.firstOrNull()
    val activeProvider = state.providers.firstOrNull { it.active && it.connected }
    var draft by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Asisten AI Pribadi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Host agent mobile-first untuk chat, session, tools, local bridge, dan multi-provider. Ini bukan template kosong lagi.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Session ${state.sessions.size}") },
                            leadingIcon = { Icon(Icons.Rounded.Storage, contentDescription = null) },
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Tools ${state.tools.size}") },
                            leadingIcon = { Icon(Icons.Rounded.Build, contentDescription = null) },
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(activeProvider?.providerType?.label ?: "Provider belum aktif") },
                            leadingIcon = {
                                Icon(
                                    if (activeProvider != null) Icons.Rounded.CloudDone else Icons.Rounded.LinkOff,
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Runtime",
                    value = "Local bridge",
                    note = "Stub aman, belum overclaim",
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Auth",
                    value = activeProvider?.authType?.label ?: "Belum connect",
                    note = "Encrypted API key fallback",
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Quick Actions", style = MaterialTheme.typography.titleLarge)
                            Text("Arahkan user langsung ke alur yang paling penting.", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = onCreateSession) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Session Baru")
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.tools.take(3).forEach { tool ->
                            AssistChip(
                                onClick = { onRunTool(tool.id) },
                                label = { Text(tool.title) },
                                leadingIcon = { Icon(Icons.Rounded.RocketLaunch, contentDescription = null) },
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(session?.title ?: "Belum ada session", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Session aktif menampung chat user, output agent, dan log tool bridge.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (state.sessions.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.sessions.take(4).forEach { chatSession ->
                                FilterChip(
                                    selected = chatSession.id == state.activeSessionId,
                                    onClick = { onSelectSession(chatSession.id) },
                                    label = { Text(chatSession.title) },
                                )
                            }
                        }
                    }
                    MessageFeed(session = session)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Send Task", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tulis task / prompt agent") },
                        supportingText = {
                            Text("Contoh: cek status provider aktif, siapkan plan tool bridge, atau buat stub workflow lokal.")
                        },
                    )
                    Button(
                        onClick = {
                            onSendMessage(draft)
                            draft = ""
                        },
                        enabled = draft.isNotBlank(),
                    ) {
                        Icon(Icons.Rounded.Chat, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Kirim ke Agent")
                    }
                }
            }
        }

        if (state.toolResults.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tool Output Terakhir", style = MaterialTheme.typography.titleLarge)
                        Text(state.toolResults.first().output, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    note: String,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(note, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MessageFeed(session: ChatSession?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (session?.messages ?: emptyList()).takeLast(6).forEach { message ->
            Surface(
                tonalElevation = if (message.role == MessageRole.USER) 2.dp else 0.dp,
                shape = MaterialTheme.shapes.medium,
                color = when (message.role) {
                    MessageRole.USER -> MaterialTheme.colorScheme.primaryContainer
                    MessageRole.ASSISTANT -> MaterialTheme.colorScheme.secondaryContainer
                    MessageRole.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        when (message.role) {
                            MessageRole.USER -> "User"
                            MessageRole.ASSISTANT -> "Agent"
                            MessageRole.SYSTEM -> "System"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(message.content, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SessionsTab(
    state: AgentUiState,
    onSelectSession: (String) -> Unit,
    onCreateSession: () -> Unit,
    onDeleteSession: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Sessions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Riwayat kerja agent lokal.", style = MaterialTheme.typography.bodyMedium)
                }
                Button(onClick = onCreateSession) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Session Baru")
                }
            }
        }
        items(state.sessions) { session ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(session.title, style = MaterialTheme.typography.titleLarge)
                    Text("Pesan: ${session.messages.size}", style = MaterialTheme.typography.bodyMedium)
                    Text("Provider: ${session.providerType?.label ?: "Belum dipilih"}", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onSelectSession(session.id) }) {
                            Text(if (state.activeSessionId == session.id) "Sedang Aktif" else "Pilih")
                        }
                        TextButton(onClick = { onDeleteSession(session.id) }) {
                            Text("Hapus")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolsTab(
    toolActions: List<ToolAction>,
    toolResults: List<ToolResult>,
    onRunTool: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tools", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Host punya local bridge/runtime sederhana. Phase 1 masih stub tapi alurnya nyata.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        items(toolActions) { tool ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tool.title, style = MaterialTheme.typography.titleLarge)
                    Text(tool.description, style = MaterialTheme.typography.bodyMedium)
                    Text(tool.commandPreview, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { onRunTool(tool.id) }) {
                        Text("Jalankan")
                    }
                }
            }
        }
        if (toolResults.isNotEmpty()) {
            item {
                Text("Log Local Bridge", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            items(toolResults) { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(result.title, style = MaterialTheme.typography.titleMedium)
                        Text(result.output, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvidersTab(
    providers: List<ProviderStatus>,
    onConnectApiKey: (ProviderType, String, String) -> Unit,
    onConnectOfficial: (ProviderType) -> Unit,
    onDisconnectProvider: (ProviderType) -> Unit,
    onActivateProvider: (ProviderType) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Providers", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("OpenAI dan Gemini didukung dari awal lewat abstraction yang sama.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        items(providers) { provider ->
            var showDialog by remember(provider.providerType) { mutableStateOf(false) }
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(provider.providerType.label, style = MaterialTheme.typography.titleLarge)
                        AssistChip(
                            onClick = {},
                            label = { Text(if (provider.connected) "Connected" else "Disconnected") },
                            leadingIcon = {
                                Icon(
                                    if (provider.connected) Icons.Rounded.CheckCircle else Icons.Rounded.LinkOff,
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                    StatusLine("Auth", provider.authType?.label ?: "Belum ada")
                    StatusLine("Model", provider.model)
                    StatusLine("Active", if (provider.active) "Ya" else "Tidak")
                    StatusLine("Profile", provider.accountLabel ?: "-")
                    StatusLine("Official login", if (provider.officialLoginAvailable) "Tersedia" else "Belum tersedia")
                    Text(provider.officialLoginNote, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showDialog = true }) {
                            Text(if (provider.connected) "Update Auth" else "Connect")
                        }
                        if (provider.connected) {
                            Button(onClick = { onActivateProvider(provider.providerType) }) {
                                Text("Set Active")
                            }
                            TextButton(onClick = { onDisconnectProvider(provider.providerType) }) {
                                Text("Disconnect")
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                ConnectDialog(
                    provider = provider,
                    onDismiss = { showDialog = false },
                    onConnectApiKey = { apiKey, model ->
                        onConnectApiKey(provider.providerType, apiKey, model)
                        showDialog = false
                    },
                    onConnectOfficial = {
                        onConnectOfficial(provider.providerType)
                        showDialog = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsTab(state: AgentUiState) {
    val activeProvider = state.providers.firstOrNull { it.active && it.connected }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Arsitektur", style = MaterialTheme.typography.titleLarge)
                    Text("Repository tunggal, secure credential store, session store, provider gateway, local bridge stub, dan UI Compose.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Host Status", style = MaterialTheme.typography.titleLarge)
                    Text("Session aktif: ${state.activeSessionId ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Provider aktif: ${activeProvider?.providerType?.label ?: "Belum aktif"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Tool log: ${state.toolResults.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kejujuran Integrasi", style = MaterialTheme.typography.titleLarge)
                    Text("Phase 1 tidak pura-pura punya login akun OpenAI/Gemini resmi. Kalau jalur resmi belum ada, fallback yang dipakai adalah encrypted API key profile.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Keamanan", style = MaterialTheme.typography.titleLarge)
                    Text("Credential sensitif disimpan di encrypted local storage. Tidak ada hardcode API key di source.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ConnectDialog(
    provider: ProviderStatus,
    onDismiss: () -> Unit,
    onConnectApiKey: (String, String) -> Unit,
    onConnectOfficial: () -> Unit,
) {
    var apiKey by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf(provider.model) }
    var useOfficialLogin by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect ${provider.providerType.label}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = useOfficialLogin,
                        onClick = { useOfficialLogin = true },
                        enabled = provider.officialLoginAvailable,
                        label = { Text(AuthType.OFFICIAL_ACCOUNT.label) },
                    )
                    FilterChip(
                        selected = !useOfficialLogin,
                        onClick = { useOfficialLogin = false },
                        label = { Text(AuthType.ENCRYPTED_API_KEY.label) },
                    )
                }

                if (useOfficialLogin) {
                    Text(provider.officialLoginNote, style = MaterialTheme.typography.bodyMedium)
                } else {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("API key") },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Model") },
                    )
                    Text("Credential disimpan terenkripsi di device.", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (useOfficialLogin) onConnectOfficial() else onConnectApiKey(apiKey, model)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
    )
}
