package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GeminiRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.User
import com.example.data.pref.PreferencesManager
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.util.UUID

data class Persona(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val systemPrompt: String,
    val isPremiumOnly: Boolean
)

val Personas = listOf(
    Persona(
        id = "default",
        name = "Assistant",
        description = "Your versatile everyday AI assistant",
        iconName = "Default",
        systemPrompt = "You are Nova AI, a highly intelligent, helpful, and friendly conversational assistant like ChatGPT. Respond gracefully.",
        isPremiumOnly = false
    ),
    Persona(
        id = "coder",
        name = "Coder",
        description = "Expert in Kotlin, Java, Compose & clean code",
        iconName = "Code",
        systemPrompt = "You are an expert software developer. Provide complete, optimal, and documented code solutions with brief conceptual guides.",
        isPremiumOnly = true
    ),
    Persona(
        id = "writer",
        name = "Creative Writer",
        description = "A creative author, essayist and poet",
        iconName = "Edit",
        systemPrompt = "You are a creative author and professional copywriter. Help the user draft essays, stories, poems, or polished copy.",
        isPremiumOnly = true
    ),
    Persona(
        id = "coach",
        name = "Zen Life Coach",
        description = "Empathetic guidelines, habits, and targets",
        iconName = "Mood",
        systemPrompt = "You are an encouraging and empathetic personal life coach. Help users structure routines, manage goals, and find focus.",
        isPremiumOnly = true
    )
)

class ChatViewModel(
    private val repository: ChatRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allRooms: StateFlow<List<ChatRoom>> = repository.allRooms
        .combine(_searchQuery) { rooms, query ->
            if (query.isBlank()) {
                rooms
            } else {
                rooms.filter { it.title.contains(query, ignoreCase = true) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentRoom = MutableStateFlow<ChatRoom?>(null)
    val currentRoom: StateFlow<ChatRoom?> = _currentRoom.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isPremium = MutableStateFlow(prefs.isPremium)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _remainingMessages = MutableStateFlow(prefs.getRemainingMessages())
    val remainingMessages: StateFlow<Int> = _remainingMessages.asStateFlow()

    val isDailyBonusActive = MutableStateFlow(prefs.isDailyBonusActiveNow())
    val dailyBonusTimeLeft = MutableStateFlow("")

    fun activateDailyBonus() {
        prefs.dailyBonusActivationTime = System.currentTimeMillis()
        isDailyBonusActive.value = true
        val currentUserVal = _currentUser.value
        _remainingMessages.value = if (currentUserVal != null) {
            getRemainingMessagesForUser(currentUserVal)
        } else {
            prefs.getRemainingMessages()
        }
    }

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedPersona = MutableStateFlow(Personas.first())
    val selectedPersona: StateFlow<Persona> = _selectedPersona.asStateFlow()

    // Logged in user state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Login and custom domain variables
    private val _isLoggedIn = MutableStateFlow(prefs.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.userEmail)
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _customDomain = MutableStateFlow(prefs.customDomain)
    val customDomain: StateFlow<String> = _customDomain.asStateFlow()

    private fun getCurrentDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun getRemainingMessagesForUser(user: User): Int {
        if (user.isPremium || prefs.isDailyBonusActiveNow()) return 99999
        val currentDate = getCurrentDateString()
        if (user.lastSavedDate != currentDate) {
            return PreferencesManager.FREE_LIMIT
        }
        return (PreferencesManager.FREE_LIMIT - user.messageCount).coerceAtLeast(0)
    }

    init {
        // Daily bonus tick loop (optimized to prevent stutters and reduce CPU usage)
        viewModelScope.launch {
            while (true) {
                val active = prefs.isDailyBonusActiveNow()
                if (isDailyBonusActive.value != active) {
                    isDailyBonusActive.value = active
                }
                
                // Keep the remainingMessages reactive flow in sync only when value changes
                val currentUserVal = _currentUser.value
                val newRemaining = if (currentUserVal != null) {
                    getRemainingMessagesForUser(currentUserVal)
                } else {
                    prefs.getRemainingMessages()
                }
                if (_remainingMessages.value != newRemaining) {
                    _remainingMessages.value = newRemaining
                }
                
                delay(10000) // Highly efficient 10-second check interval instead of 1 second
            }
        }

        if (prefs.isLoggedIn) {
            viewModelScope.launch {
                val email = prefs.userEmail
                val user = repository.getUserByEmail(email)
                if (user != null) {
                    val currentDate = getCurrentDateString()
                    var updatedUser = user
                    if (user.lastSavedDate != currentDate) {
                        updatedUser = user.copy(lastSavedDate = currentDate, messageCount = 0)
                        repository.insertUser(updatedUser)
                    }
                    _currentUser.value = updatedUser
                    _isPremium.value = updatedUser.isPremium
                    _remainingMessages.value = getRemainingMessagesForUser(updatedUser)
                } else {
                    val newUser = User(
                        email = email,
                        password = prefs.userPassword,
                        isPremium = prefs.isPremium,
                        lastSavedDate = getCurrentDateString(),
                        messageCount = PreferencesManager.FREE_LIMIT - prefs.getRemainingMessages()
                    )
                    repository.insertUser(newUser)
                    _currentUser.value = newUser
                    _isPremium.value = newUser.isPremium
                    _remainingMessages.value = getRemainingMessagesForUser(newUser)
                }
            }
        }
    }

    // Observe messages dynamically for the current room
    val messages: StateFlow<List<ChatMessage>> = _currentRoom
        .flatMapLatest { room ->
            if (room != null) repository.getMessagesForRoom(room.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun signIn(email: String, psw: String, domain: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isBlank()) {
            onFailure("Email is required.")
            return
        }
        if (psw.isBlank()) {
            onFailure("Password is required.")
            return
        }
        viewModelScope.launch {
            var user = repository.getUserByEmail(email)
            if (user == null && email == "guest@nova-shared.ai") {
                user = User(
                    email = email,
                    password = psw,
                    isPremium = false,
                    messageCount = 0,
                    lastSavedDate = getCurrentDateString()
                )
                repository.insertUser(user)
            }
            
            if (user == null) {
                onFailure("No user found with this email.")
            } else if (user.password != psw) {
                onFailure("Incorrect password.")
            } else {
                val currentDate = getCurrentDateString()
                var updatedUser = user
                if (user.lastSavedDate != currentDate) {
                    updatedUser = user.copy(lastSavedDate = currentDate, messageCount = 0)
                    repository.insertUser(updatedUser)
                }
                _currentUser.value = updatedUser
                _isPremium.value = updatedUser.isPremium
                _remainingMessages.value = getRemainingMessagesForUser(updatedUser)

                prefs.isLoggedIn = true
                prefs.userEmail = email
                prefs.userPassword = psw
                prefs.customDomain = domain
                _isLoggedIn.value = true
                _userEmail.value = email
                _customDomain.value = domain

                onSuccess()
            }
        }
    }

    fun signUp(email: String, psw: String, domain: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isBlank()) {
            onFailure("Email is required.")
            return
        }
        if (psw.length < 6) {
            onFailure("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onFailure("An account with this email already exists.")
            } else {
                val newUser = User(
                    email = email,
                    password = psw,
                    isPremium = false,
                    messageCount = 0,
                    lastSavedDate = getCurrentDateString()
                )
                repository.insertUser(newUser)
                _currentUser.value = newUser
                _isPremium.value = false
                _remainingMessages.value = PreferencesManager.FREE_LIMIT

                prefs.isLoggedIn = true
                prefs.userEmail = email
                prefs.userPassword = psw
                prefs.customDomain = domain
                _isLoggedIn.value = true
                _userEmail.value = email
                _customDomain.value = domain

                onSuccess()
            }
        }
    }

    fun resetPassword(email: String, psw: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isBlank()) {
            onFailure("Email is required.")
            return
        }
        if (psw.length < 6) {
            onFailure("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                onFailure("No account found with this email.")
            } else {
                val success = repository.updatePassword(email, psw)
                if (success) {
                    onSuccess()
                } else {
                    onFailure("Failed to reset password. Please try again.")
                }
            }
        }
    }

    fun logout() {
        prefs.isLoggedIn = false
        prefs.userEmail = ""
        prefs.userPassword = ""
        prefs.customDomain = ""
        _isLoggedIn.value = false
        _userEmail.value = ""
        _customDomain.value = ""
        _currentUser.value = null
        _isPremium.value = false
        _remainingMessages.value = 0
        _currentRoom.value = null
    }

    fun selectRoom(room: ChatRoom?) {
        _currentRoom.value = room
        _error.value = null
        if (room != null) {
            val matchedPersona = Personas.find { it.id == room.personaId } ?: Personas.first()
            _selectedPersona.value = matchedPersona
        }
    }

    fun selectPersona(persona: Persona) {
        if (persona.isPremiumOnly && !_isPremium.value) {
            _error.value = "PremiumOnly"
            return
        }
        _selectedPersona.value = persona
        _currentRoom.value?.let { room ->
            viewModelScope.launch {
                val updatedRoom = room.copy(personaId = persona.id)
                repository.createRoom(updatedRoom)
                _currentRoom.value = updatedRoom
            }
        }
    }

    fun upgradeToPremium() {
        prefs.isPremium = true
        _isPremium.value = true
        _error.value = null
        
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(isPremium = true)
            _currentUser.value = updatedUser
            _remainingMessages.value = Int.MAX_VALUE
            viewModelScope.launch {
                repository.insertUser(updatedUser)
            }
        } ?: run {
            _remainingMessages.value = prefs.getRemainingMessages()
        }
    }

    fun downgradeToFree() {
        prefs.isPremium = false
        _isPremium.value = false
        
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(isPremium = false)
            _currentUser.value = updatedUser
            _remainingMessages.value = getRemainingMessagesForUser(updatedUser)
            viewModelScope.launch {
                repository.insertUser(updatedUser)
            }
        } ?: run {
            _remainingMessages.value = prefs.getRemainingMessages()
        }
    }

    fun createNewChat(title: String = "New Chat") {
        viewModelScope.launch {
            val newRoom = ChatRoom(
                id = UUID.randomUUID().toString(),
                title = title,
                personaId = _selectedPersona.value.id
            )
            repository.createRoom(newRoom)
            selectRoom(newRoom)
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            repository.deleteRoom(roomId)
            if (_currentRoom.value?.id == roomId) {
                _currentRoom.value = null
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            _error.value = "ApiKeyMissing"
            return
        }

        // Subscription check
        val currentUserVal = _currentUser.value
        val hasMessagesLeft = if (currentUserVal != null) {
            getRemainingMessagesForUser(currentUserVal) > 0
        } else {
            prefs.getRemainingMessages() > 0
        }

        if (!_isPremium.value && !hasMessagesLeft) {
            _error.value = "LimitReached"
            return
        }

        viewModelScope.launch {
            // Allocate a chat room if none is currently selected
            var room = _currentRoom.value
            if (room == null) {
                val autoTitle = if (text.length > 25) text.substring(0, 25) + "..." else text
                val newRoom = ChatRoom(
                    id = UUID.randomUUID().toString(),
                    title = autoTitle,
                    personaId = _selectedPersona.value.id
                )
                repository.createRoom(newRoom)
                selectRoom(newRoom)
                room = newRoom
            }

            // Consume a free message if standard user
            if (!_isPremium.value) {
                val user = _currentUser.value
                if (user != null) {
                    val remaining = getRemainingMessagesForUser(user)
                    if (remaining <= 0) {
                        _error.value = "LimitReached"
                        return@launch
                    }
                    val updatedUser = user.copy(
                        messageCount = user.messageCount + 1,
                        lastSavedDate = getCurrentDateString()
                    )
                    repository.insertUser(updatedUser)
                    _currentUser.value = updatedUser
                    _remainingMessages.value = getRemainingMessagesForUser(updatedUser)
                } else {
                    val success = prefs.incrementMessageCount()
                    if (!success) {
                        _error.value = "LimitReached"
                        return@launch
                    }
                    _remainingMessages.value = prefs.getRemainingMessages()
                }
            }

            // Save user message
            val userMsg = ChatMessage(roomId = room.id, role = "user", text = text)
            repository.insertMessage(userMsg)

            _isGenerating.value = true
            _error.value = null

            try {
                // Load messages context
                val history = repository.getMessagesForRoomSync(room.id)
                val modelToUse = if (_isPremium.value) "gemini-3.1-pro-preview" else "gemini-3.5-flash"

                // Map database messages to Gemini contents format
                val apiContents = history.map { msg ->
                    val apiRole = if (msg.role == "user") "user" else "model"
                    Content(
                        parts = listOf(Part(text = msg.text)),
                        role = apiRole
                    )
                }

                val systemInstruction = Content(
                    parts = listOf(Part(text = _selectedPersona.value.systemPrompt))
                )

                val request = GeminiRequest(
                    contents = apiContents,
                    systemInstruction = systemInstruction
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(
                        model = modelToUse,
                        apiKey = key,
                        request = request
                    )
                }

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (replyText != null) {
                    val aiMsg = ChatMessage(roomId = room.id, role = "model", text = replyText)
                    repository.insertMessage(aiMsg)
                } else {
                    _error.value = "No response candidates returned from AI model."
                }
            } catch (e: Exception) {
                _error.value = "API Error: ${e.localizedMessage ?: "Unknown failure"}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun sendAttachmentMock(attachmentType: String, name: String) {
        viewModelScope.launch {
            var room = _currentRoom.value
            if (room == null) {
                val newRoom = ChatRoom(
                    id = UUID.randomUUID().toString(),
                    title = "Attachment: $name",
                    personaId = _selectedPersona.value.id
                )
                repository.createRoom(newRoom)
                selectRoom(newRoom)
                room = newRoom
            }

            val userMsg = ChatMessage(
                roomId = room.id,
                role = "user",
                text = "📎 Attached $attachmentType: $name"
            )
            repository.insertMessage(userMsg)

            _isGenerating.value = true
            delay(1000)

            val reply = "📎 **Attachment Received**\n\n* File Name: `$name`\n* Type: `$attachmentType`\n\nAnalyzed successfully in the secure sandbox proxy environment! How can I assist you with this attachment?"
            val aiMsg = ChatMessage(
                roomId = room.id,
                role = "model",
                text = reply
            )
            repository.insertMessage(aiMsg)
            _isGenerating.value = false
        }
    }

    fun generateAndSendAIImage(prompt: String) {
        if (prompt.isBlank()) return
        
        viewModelScope.launch {
            var room = _currentRoom.value
            if (room == null) {
                val autoTitle = if (prompt.length > 20) prompt.substring(0, 20) + "..." else prompt
                val newRoom = ChatRoom(
                    id = UUID.randomUUID().toString(),
                    title = "🎨 Gen: $autoTitle",
                    personaId = _selectedPersona.value.id
                )
                repository.createRoom(newRoom)
                selectRoom(newRoom)
                room = newRoom
            }

            val userMsg = ChatMessage(
                roomId = room.id,
                role = "user",
                text = "🎨 Generate image: $prompt"
            )
            repository.insertMessage(userMsg)

            _isGenerating.value = true
            delay(1200)

            val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8")
            val imageUrl = "https://image.pollinations.ai/p/$encodedPrompt?width=1024&height=1024&nologo=true"

            val aiMsg = ChatMessage(
                roomId = room.id,
                role = "model",
                text = "Here is your generated image for prompt: *\"$prompt\"*",
                imageUrl = imageUrl
            )
            repository.insertMessage(aiMsg)
            _isGenerating.value = false
        }
    }
}

class ChatViewModelFactory(
    private val repository: ChatRepository,
    private val prefs: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
