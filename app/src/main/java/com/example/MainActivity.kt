package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.pref.PreferencesManager
import com.example.data.repository.ChatRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ChatViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Services & Databases locally
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ChatRepository(database.chatDao(), database.userDao())
        val prefs = PreferencesManager(applicationContext)

        // Instantiate ViewModels
        val viewModelFactory = ChatViewModelFactory(repository, prefs)
        val chatViewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val isLoggedIn by chatViewModel.isLoggedIn.collectAsState()

                    if (isLoggedIn) {
                        ChatScreen(
                            viewModel = chatViewModel,
                            onSignOut = {
                                chatViewModel.logout()
                            }
                        )
                    } else {
                        AuthScreen(
                            viewModel = chatViewModel
                        )
                    }
                }
            }
        }
    }
}
