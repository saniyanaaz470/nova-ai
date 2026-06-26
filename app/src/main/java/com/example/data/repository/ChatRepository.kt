package com.example.data.repository

import com.example.data.db.ChatDao
import com.example.data.db.UserDao
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
    private val userDao: UserDao
) {
    val allRooms: Flow<List<ChatRoom>> = chatDao.getAllRooms()

    // User Operations
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun updatePassword(email: String, newPassword: String): Boolean {
        val rowsAffected = userDao.updatePassword(email, newPassword)
        return rowsAffected > 0
    }

    suspend fun getRoomById(roomId: String): ChatRoom? = chatDao.getRoomById(roomId)

    suspend fun createRoom(room: ChatRoom) = chatDao.insertRoom(room)

    suspend fun updateRoomTitle(roomId: String, newTitle: String) = chatDao.updateRoomTitle(roomId, newTitle)

    suspend fun deleteRoom(roomId: String) {
        chatDao.deleteRoomById(roomId)
        chatDao.deleteMessagesForRoom(roomId)
    }

    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessage>> = chatDao.getMessagesForRoom(roomId)

    suspend fun getMessagesForRoomSync(roomId: String): List<ChatMessage> = chatDao.getMessagesForRoomSync(roomId)

    suspend fun insertMessage(message: ChatMessage) = chatDao.insertMessage(message)
}
