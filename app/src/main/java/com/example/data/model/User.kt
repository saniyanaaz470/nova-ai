package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val password: String,
    val isPremium: Boolean = false,
    val messageCount: Int = 0,
    val lastSavedDate: String = ""
)
