package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import android.widget.Toast
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.ui.theme.ChatGptGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.PremiumGoldLight
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.Persona
import com.example.ui.viewmodel.Personas
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current

    val allRooms by viewModel.allRooms.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val errorState by viewModel.error.collectAsState()
    val selectedPersona by viewModel.selectedPersona.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val userEmail by viewModel.userEmail.collectAsState()
    val customDomain by viewModel.customDomain.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { com.example.data.pref.PreferencesManager(context) }
    
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showWebPortal by remember { mutableStateOf(false) }
    
    // Download states
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadStatusText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (prefs.isTwoMonthsCompleted()) {
            showUpdateDialog = true
        }
    }

    var inputText by remember { mutableStateOf("") }
    var showPaywall by remember { mutableStateOf(false) }
    var showPersonaSelector by remember { mutableStateOf(false) }
    var showPersonaDropdown by remember { mutableStateOf(false) }
    
    // Attachments and image mode state
    var showAttachmentsMenu by remember { mutableStateOf(false) }
    var isImageModeActive by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.sendAttachmentMock(
                attachmentType = "Camera Captured Photo",
                name = "captured_image.png"
            )
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.sendAttachmentMock(
                attachmentType = "Photo Library Image",
                name = "gallery_photo.jpg"
            )
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.sendAttachmentMock(
                attachmentType = "Document File",
                name = "selected_document.pdf"
            )
        }
    }

    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Intercept Persona Locked Error to trigger paywall
    LaunchedEffect(errorState) {
        if (errorState == "PremiumOnly" || errorState == "LimitReached") {
            showPaywall = true
            viewModel.clearError()
        }
    }

    val suggestedPrompts = remember {
        listOf(
            SuggestedPrompt(
                "Draft a Code",
                "Write a simple Kotlin Coroutine task with delays",
                Icons.Default.Create,
                Color(0xFF10B981)
            ),
            SuggestedPrompt(
                "Creative Story",
                "Draft an essay brainstorming about a human landing on Mars",
                Icons.Default.Create,
                Color(0xFF8B5CF6)
            ),
            SuggestedPrompt(
                "Mindfulness Tip",
                "Give me 3 mindful breathing loops to stay focused",
                Icons.Default.Person,
                Color(0xFFF59E0B)
            ),
            SuggestedPrompt(
                "Quick Summary",
                "Summarize quantum cryptography protocols simply",
                Icons.Default.Info,
                Color(0xFF3B82F6)
            )
        )
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            containerColor = DarkSurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Update Available",
                    tint = ChatGptGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "New Update Available!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Nova AI has an exciting bi-monthly update ready for you. Upgrade now to get the latest models, improved image generator, and 10x faster response speeds!",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Package Size: 105.4 MB\nRelease Cycle: 2 Months completed",
                        color = ChatGptGreen,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpdateDialog = false
                        showWebPortal = true // Open the APK Web Portal to download!
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChatGptGreen)
                ) {
                    Text("Update Now (105 MB)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Remind Me Later", color = TextGray)
                }
            }
        )
    }

    if (showWebPortal) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Browser URL address bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showWebPortal = false }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            tint = ChatGptGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "https://nova-ai.com/download",
                            color = TextGray,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Page body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nova AI Glowing Emblem Logo
                    Image(
                        painter = painterResource(id = R.drawable.img_nova_logo),
                        contentDescription = "Nova Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Nova AI Web Portal",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Official Release Center • Package Portal",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PremiumGold,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Download Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nova AI Pro APK (Stable)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Version v2.4.0 • Android 8.0+ • Secure MD5 verified",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))

                            if (isDownloading) {
                                // Download progress display
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    androidx.compose.material3.LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = ChatGptGreen,
                                        trackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = downloadStatusText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format(java.util.Locale.US, "%.1f%% Completed", downloadProgress * 100),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ChatGptGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { isDownloading = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                                    ) {
                                        Text("Cancel Download", color = Color.White)
                                    }
                                }
                            } else {
                                // Download buttons
                                Button(
                                    onClick = {
                                        isDownloading = true
                                        downloadProgress = 0f
                                        downloadStatusText = "Initializing connection..."
                                        // Start real 100MB download
                                        val url = "https://speed.hetzner.de/100MB.bin"
                                        startApkDownload(
                                            context = context,
                                            urlStr = url,
                                            onProgress = { prog, status ->
                                                if (isDownloading) {
                                                    downloadProgress = prog
                                                    downloadStatusText = status
                                                }
                                            },
                                            onComplete = { file ->
                                                isDownloading = false
                                                Toast.makeText(context, "Nova Pro APK downloaded to Downloads!", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                isDownloading = false
                                                Toast.makeText(context, "Network Error: $err. Try Offline Mode!", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChatGptGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download APK over Network (105 MB)", fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Offline Generation button
                                OutlinedButton(
                                    onClick = {
                                        isDownloading = true
                                        downloadProgress = 0f
                                        downloadStatusText = "Preparing local package stream..."
                                        generateLocal100MbFile(
                                            context = context,
                                            onProgress = { prog, status ->
                                                if (isDownloading) {
                                                    downloadProgress = prog
                                                    downloadStatusText = status
                                                }
                                            },
                                            onComplete = { file ->
                                                isDownloading = false
                                                Toast.makeText(context, "Offline 105MB APK package created in app storage!", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumGold),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = PremiumGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Instant Mock Download (105 MB offline)", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Simulated 2-Month Completer testing card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.7f)),
                        border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🧪 TEST CONTROLLER • RELEASE popup",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PremiumGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Simulate that 2 months have completed on your phone. This triggers the automatic update popup on launching/reloading the chat page!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    // Set launch time to 61 days ago
                                    prefs.firstLaunchTime = System.currentTimeMillis() - (61L * 24L * 60L * 60L * 1000L)
                                    Toast.makeText(context, "Simulation Activated! Restart app or reload to see the 2-Month Update Popup.", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Simulate 2 Months Completed", color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Install Instructions Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "How to Install Nova AI APK:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Click either the Network or Offline download button above.\n" +
                                   "2. Once downloaded, tap the download complete notification.\n" +
                                   "3. If prompted, enable 'Allow installation from this source' in your device settings.\n" +
                                   "4. Press 'Install' to enjoy the update!",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextGray, lineHeight = 18.sp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showPaywall) {
        PremiumScreen(
            isPremium = isPremium,
            onUpgradeSuccess = {
                viewModel.upgradeToPremium()
                showPaywall = false
            },
            onClose = { showPaywall = false }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(310.dp),
                    drawerContainerColor = DarkSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header with Logo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_nova_logo),
                                contentDescription = "Nova AI Logo",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Nova AI Chat",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                )
                                if (customDomain.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = PremiumGold,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = customDomain,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PremiumGold,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // New Chat Button
                        Button(
                            onClick = {
                                viewModel.createNewChat()
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("new_chat_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ChatGptGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("New Chat", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search conversations input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_history_input"),
                            placeholder = { Text("Search history...", color = TextGray.copy(alpha = 0.5f)) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextGray)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ChatGptGreen.copy(alpha = 0.6f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedLabelColor = ChatGptGreen,
                                unfocusedTextColor = TextWhite,
                                focusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "HISTORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextGray.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chat History List
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (allRooms.isEmpty()) {
                                item {
                                    Text(
                                        text = if (searchQuery.isBlank()) "No conversation history." else "No chats matching search query.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextGray.copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp)
                                    )
                                }
                            } else {
                                items(allRooms, key = { it.id }) { room ->
                                    val isCurrent = currentRoom?.id == room.id
                                    val roomPersona = Personas.find { it.id == room.personaId } ?: Personas.first()

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCurrent) Color.White.copy(alpha = 0.08f) else Color.Transparent)
                                            .clickable {
                                                viewModel.selectRoom(room)
                                                scope.launch { drawerState.close() }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (roomPersona.iconName) {
                                                    "Code" -> Icons.Default.Create
                                                    "Edit" -> Icons.Default.Create
                                                    "Mood" -> Icons.Default.Person
                                                    else -> Icons.Default.Info
                                                },
                                                contentDescription = null,
                                                tint = if (isCurrent) ChatGptGreen else TextGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = room.title,
                                                color = if (isCurrent) Color.White else TextGray,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteRoom(room.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete conversation",
                                                tint = TextGray.copy(alpha = 0.4f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

                        // APK Download Portal Button
                        Card(
                            onClick = { 
                                showWebPortal = true 
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .testTag("apk_website_button"),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                            border = BorderStroke(1.dp, ChatGptGreen.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(ChatGptGreen.copy(alpha = 0.15f), RoundedCornerShape(50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = ChatGptGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Nova AI APK Website",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "Official Web Portal (100MB+ APK)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                                    )
                                }
                            }
                        }

                        // Sidebar Bottom: Subscription and Account controls
                        if (!isPremium) {
                            Card(
                                onClick = { showPaywall = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upgrade_pro_card"),
                                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                                border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(PremiumGold.copy(alpha = 0.15f), RoundedCornerShape(50)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = PremiumGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Upgrade to Pro",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "No limits, smart pro features",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // User profile section with logout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(ChatGptGreen.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ChatGptGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (userEmail.isNotBlank()) userEmail.substringBefore("@") else "User",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 140.dp)
                                    )
                                    Text(
                                        text = if (isPremium) "Premium Plan" else "Free Account",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isPremium) PremiumGold else TextGray,
                                            fontWeight = if (isPremium) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Log Out",
                                    tint = Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            // Active Persona click dropdown
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showPersonaDropdown = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("persona_dropdown_trigger"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (selectedPersona.iconName) {
                                            "Code" -> Icons.Default.Create
                                            "Edit" -> Icons.Default.Create
                                            "Mood" -> Icons.Default.Person
                                            else -> Icons.Default.Star
                                        },
                                        contentDescription = null,
                                        tint = ChatGptGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selectedPersona.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    )
                                    Text(
                                        text = " ▼",
                                        color = TextGray,
                                        fontSize = 10.sp
                                    )
                                }

                                DropdownMenu(
                                    expanded = showPersonaDropdown,
                                    onDismissRequest = { showPersonaDropdown = false },
                                    modifier = Modifier
                                        .background(DarkSurface)
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .width(240.dp)
                                ) {
                                    Personas.forEach { persona ->
                                        val isSelected = selectedPersona.id == persona.id
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = persona.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) ChatGptGreen else TextWhite
                                                        )
                                                    )
                                                    Text(
                                                        text = persona.description,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = TextGray,
                                                            fontSize = 11.sp
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectPersona(persona)
                                                showPersonaDropdown = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = when (persona.iconName) {
                                                        "Code" -> Icons.Default.Create
                                                        "Edit" -> Icons.Default.Create
                                                        "Mood" -> Icons.Default.Person
                                                        else -> Icons.Default.Star
                                                    },
                                                    contentDescription = null,
                                                    tint = if (isSelected) ChatGptGreen else TextGray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            trailingIcon = {
                                                if (persona.isPremiumOnly && !isPremium) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(PremiumGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "PRO",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 8.sp,
                                                                color = PremiumGold,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier.testTag("persona_dropdown_item_${persona.id}")
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open drawer", tint = TextWhite)
                            }
                        },
                        actions = {
                            if (!isPremium) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(listOf(PremiumGold, PremiumGoldLight)),
                                            shape = RoundedCornerShape(20)
                                        )
                                        .clickable { showPaywall = true }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = DarkBackground,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Upgrade",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DarkBackground
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding()
                ) {
                    if (messages.isEmpty() && !isGenerating) {
                        // GOOD HOME PAGE
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Pulsing glowing center circle
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(ChatGptGreen.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                    .border(1.dp, ChatGptGreen.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_nova_logo),
                                    contentDescription = "Nova AI logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "What can I write for you today?",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Text(
                                text = "Ask anything. Select expert personas above for professional code, creative copy, or mindfulness coaches.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Grid of ChatGPT style workspace prompt shortcuts
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                userScrollEnabled = false
                            ) {
                                items(suggestedPrompts, key = { it.title }) { prompt ->
                                    Card(
                                        onClick = {
                                            inputText = prompt.fullPrompt
                                            viewModel.sendMessage(prompt.fullPrompt)
                                            inputText = ""
                                        },
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .testTag("prompt_suggestion_${prompt.title.replace(" ", "_")}"),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(prompt.iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = prompt.icon,
                                                    contentDescription = null,
                                                    tint = prompt.iconColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = prompt.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextWhite
                                                    )
                                                )
                                                Text(
                                                    text = prompt.subtitle,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Active Chat Conversation
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("messages_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                val isUser = msg.role == "user"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    if (!isUser) {
                                        // Model Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(ChatGptGreen, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Column(
                                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        // Bubble background
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (isUser) ChatGptGreen else DarkSurface,
                                                    shape = RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                                    )
                                                )
                                                .border(
                                                    width = if (isUser) 0.dp else 1.dp,
                                                    color = if (isUser) Color.Transparent else Color.White.copy(alpha = 0.05f),
                                                    shape = RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                                    )
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = msg.text,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = Color.White,
                                                        lineHeight = 20.sp
                                                    )
                                                )
                                                if (msg.imageUrl != null) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .heightIn(min = 150.dp, max = 280.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .border(1.dp, PremiumGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                            .background(Color.Black.copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        AsyncImage(
                                                            model = msg.imageUrl,
                                                            contentDescription = "AI Generated Image",
                                                            modifier = Modifier.fillMaxWidth(),
                                                            contentScale = ContentScale.Fit
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (!isUser) {
                                            // Copy action for model messages
                                            Row(
                                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Create,
                                                    contentDescription = "Copy text",
                                                    tint = TextGray.copy(alpha = 0.5f),
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            clipboardManager.setText(AnnotatedString(msg.text))
                                                        }
                                                )
                                            }
                                        }
                                    }

                                    if (isUser) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // User Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (isGenerating) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(ChatGptGreen, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "Nova is reasoning...",
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom chat input panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {


                        if (isImageModeActive) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .background(PremiumGold.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .border(1.dp, PremiumGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { isImageModeActive = false }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = PremiumGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🎨 Image Generation Mode Active (Tap to cancel)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = PremiumGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Cancel image mode",
                                    tint = PremiumGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, RoundedCornerShape(24.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showAttachmentsMenu = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("plus_attachments_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Show attachment options",
                                    tint = ChatGptGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            TextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_text"),
                                placeholder = { Text(if (isImageModeActive) "Describe the image to generate..." else "Message Nova...", color = TextGray.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (inputText.isNotBlank() && !isGenerating) {
                                            if (isImageModeActive) {
                                                viewModel.generateAndSendAIImage(inputText)
                                                isImageModeActive = false
                                            } else {
                                                viewModel.sendMessage(inputText)
                                            }
                                            inputText = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )

                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank() && !isGenerating) {
                                        if (isImageModeActive) {
                                            viewModel.generateAndSendAIImage(inputText)
                                            isImageModeActive = false
                                        } else {
                                            viewModel.sendMessage(inputText)
                                        }
                                        inputText = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = inputText.isNotBlank() && !isGenerating,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (inputText.isNotBlank()) {
                                            if (isImageModeActive) PremiumGold else ChatGptGreen
                                        } else {
                                            Color.White.copy(alpha = 0.05f)
                                        },
                                        RoundedCornerShape(50.dp)
                                    )
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = if (inputText.isNotBlank()) Color.White else TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Tiny ChatGPT style warning (keeps screen super clean)
                        Text(
                            text = "Nova AI may produce inaccurate information about people, places, or facts. Enterprise proxy active.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = TextGray.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Custom Persona selector sheet/dialog
    if (showPersonaSelector) {
        Dialog(onDismissRequest = { showPersonaSelector = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Change AI Persona",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Personas.forEach { persona ->
                        val isSelected = selectedPersona.id == persona.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ChatGptGreen.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    viewModel.selectPersona(persona)
                                    showPersonaSelector = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isSelected) ChatGptGreen.copy(alpha = 0.15f) else Color.White.copy(
                                                alpha = 0.05f
                                            ),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (persona.iconName) {
                                            "Code" -> Icons.Default.Create
                                            "Edit" -> Icons.Default.Create
                                            "Mood" -> Icons.Default.Person
                                            else -> Icons.Default.Star
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) ChatGptGreen else TextWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = persona.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    )
                                    Text(
                                        text = persona.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                                    )
                                }
                            }

                            if (persona.isPremiumOnly && !isPremium) {
                                Box(
                                    modifier = Modifier
                                        .background(PremiumGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = PremiumGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    // Attachments dialog option menu
    if (showAttachmentsMenu) {
        Dialog(onDismissRequest = { showAttachmentsMenu = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add Attachment",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Camera
                        AttachmentOptionItem(
                            title = "Camera",
                            icon = Icons.Default.Create,
                            iconColor = Color(0xFF3B82F6),
                            onClick = {
                                showAttachmentsMenu = false
                                cameraLauncher.launch(null)
                            }
                        )

                        // Photos
                        AttachmentOptionItem(
                            title = "Photos",
                            icon = Icons.Default.Person,
                            iconColor = Color(0xFF10B981),
                            onClick = {
                                showAttachmentsMenu = false
                                photoLauncher.launch("image/*")
                            }
                        )

                        // Files
                        AttachmentOptionItem(
                            title = "Files",
                            icon = Icons.Default.Menu,
                            iconColor = Color(0xFFF59E0B),
                            onClick = {
                                showAttachmentsMenu = false
                                fileLauncher.launch("*/*")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Plugins Section
                    Text(
                        text = "AI PLUGINS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PremiumGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Plugin Item: Image Generator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .clickable {
                                showAttachmentsMenu = false
                                if (isPremium) {
                                    isImageModeActive = true
                                } else {
                                    showPaywall = true
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PremiumGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = PremiumGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI Image Generator",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (!isPremium) {
                                    Box(
                                        modifier = Modifier
                                            .background(PremiumGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                color = PremiumGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Generate premium, high-resolution AI art on-demand.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showAttachmentsMenu = false }) {
                        Text("Cancel", color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentOptionItem(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(25.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextWhite,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

data class SuggestedPrompt(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val fullPrompt: String = subtitle
)

// Adaptive Border Stroke helper
@Composable
fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = remember(width, color) {
    androidx.compose.foundation.BorderStroke(width, color)
}

fun startApkDownload(
    context: android.content.Context,
    urlStr: String,
    onProgress: (Float, String) -> Unit,
    onComplete: (java.io.File) -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = java.net.URL(urlStr)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                onError("Server returned HTTP ${connection.responseCode}")
                return@launch
            }

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val targetFile = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "nova_ai_v2.4_pro.apk")
            
            val output = java.io.FileOutputStream(targetFile)
            val data = ByteArray(1024 * 64) // 64kb buffer
            var total: Long = 0
            var count: Int
            var lastUpdate = System.currentTimeMillis()

            while (input.read(data).also { count = it } != -1) {
                total += count
                output.write(data, 0, count)

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdate > 300) { // Update progress UI every 300ms
                    val progress = if (fileLength > 0) total.toFloat() / fileLength else 0f
                    val downloadedMb = total.toFloat() / (1024 * 1024)
                    val totalMb = fileLength.toFloat() / (1024 * 1024)
                    val statusText = String.format(java.util.Locale.US, "Downloading: %.1f MB / %.1f MB", downloadedMb, totalMb)
                    
                    withContext(Dispatchers.Main) {
                        onProgress(progress, statusText)
                    }
                    lastUpdate = currentTime
                }
            }

            output.flush()
            output.close()
            input.close()

            withContext(Dispatchers.Main) {
                onProgress(1f, "Download Complete! Saved to app downloads: ${targetFile.name}")
                onComplete(targetFile)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e.localizedMessage ?: "Unknown download error")
            }
        }
    }
}

fun generateLocal100MbFile(
    context: android.content.Context,
    onProgress: (Float, String) -> Unit,
    onComplete: (java.io.File) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val targetFile = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "nova_ai_v2.4_pro_simulated.apk")
            val output = java.io.FileOutputStream(targetFile)
            val buffer = ByteArray(1024 * 1024) // 1MB buffer
            // Fill with some pattern
            for (i in 0 until 1024 * 1024) {
                buffer[i] = (i % 256).toByte()
            }
            val totalSize = 105 * 1024 * 1024L // 105 MB
            var written = 0L
            while (written < totalSize) {
                output.write(buffer)
                written += buffer.size
                val progress = written.toFloat() / totalSize
                val currentMb = written.toFloat() / (1024 * 1024)
                val statusText = String.format(java.util.Locale.US, "Simulating offline package generation: %.1f MB / 105.0 MB", currentMb)
                
                withContext(Dispatchers.Main) {
                    onProgress(progress, statusText)
                }
                delay(30) // Smooth progress display
            }
            output.flush()
            output.close()
            withContext(Dispatchers.Main) {
                onComplete(targetFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
