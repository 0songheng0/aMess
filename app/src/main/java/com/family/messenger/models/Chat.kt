package com.family.messenger.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastMessageSenderId: String = "",
    val unreadCount: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "participants" to participants,
            "participantNames" to participantNames,
            "lastMessage" to lastMessage,
            "lastMessageTime" to lastMessageTime,
            "lastMessageSenderId" to lastMessageSenderId,
            "unreadCount" to unreadCount
        )
    }

    fun getOtherParticipantName(currentUserId: String): String {
        return participants.firstOrNull { it != currentUserId }?.let {
            participantNames[it] ?: "Unknown"
        } ?: "Unknown"
    }

    fun getOtherParticipantId(currentUserId: String): String {
        return participants.firstOrNull { it != currentUserId } ?: ""
    }
}
