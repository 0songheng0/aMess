package com.family.messenger.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val fcmToken: String = "",
    val online: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "fcmToken" to fcmToken,
            "online" to online,
            "lastSeen" to lastSeen
        )
    }
}
