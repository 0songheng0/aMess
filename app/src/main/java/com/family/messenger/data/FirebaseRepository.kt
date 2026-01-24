package com.family.messenger.data

import android.util.Log
import com.family.messenger.models.Chat
import com.family.messenger.models.Message
import com.family.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val chatsCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")

    private val TAG = "FirebaseRepository"

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUser(): User? {
        val user = auth.currentUser ?: return null
        return User(
            uid = user.uid,
            email = user.email ?: "",
            displayName = user.displayName ?: ""
        )
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            val userDoc = usersCollection.document(user.uid).get().await()
            val userData = userDoc.toObject(User::class.java) ?: User(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: ""
            )
            updateUserOnlineStatus(true)
            Result.success(userData)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("User is null"))

            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName
            )

            usersCollection.document(firebaseUser.uid).set(user.toMap()).await()
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        getCurrentUserId()?.let { uid ->
            usersCollection.document(uid).update(
                mapOf(
                    "online" to false,
                    "lastSeen" to System.currentTimeMillis()
                )
            )
        }
        auth.signOut()
    }

    suspend fun updateUserOnlineStatus(online: Boolean) {
        getCurrentUserId()?.let { uid ->
            try {
                usersCollection.document(uid).update(
                    mapOf(
                        "online" to online,
                        "lastSeen" to System.currentTimeMillis()
                    )
                ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update online status", e)
            }
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                .filter { it.uid != getCurrentUserId() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get users", e)
            emptyList()
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user", e)
            null
        }
    }

    suspend fun createOrGetChat(otherUserId: String): String? {
        val currentUserId = getCurrentUserId() ?: return null

        return try {
            // Check if chat already exists
            val existingChat = chatsCollection
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Chat::class.java) }
                .firstOrNull { it.participants.contains(otherUserId) }

            if (existingChat != null) {
                return existingChat.id
            }

            // Create new chat
            val chatId = UUID.randomUUID().toString()
            val currentUser = getCurrentUser()
            val otherUser = getUserById(otherUserId)

            val chat = Chat(
                id = chatId,
                participants = listOf(currentUserId, otherUserId),
                participantNames = mapOf(
                    currentUserId to (currentUser?.displayName ?: ""),
                    otherUserId to (otherUser?.displayName ?: "")
                ),
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis()
            )

            chatsCollection.document(chatId).set(chat.toMap()).await()
            chatId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create chat", e)
            null
        }
    }

    fun listenToChats(onChatsChanged: (List<Chat>) -> Unit): ListenerRegistration {
        val currentUserId = getCurrentUserId() ?: return object : ListenerRegistration {
            override fun remove() {}
        }

        return chatsCollection
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen to chats failed", error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull {
                    it.toObject(Chat::class.java)
                } ?: emptyList()

                onChatsChanged(chats)
            }
    }

    fun listenToMessages(chatId: String, onMessagesChanged: (List<Message>) -> Unit): ListenerRegistration {
        return messagesCollection
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen to messages failed", error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(Message::class.java)
                } ?: emptyList()

                onMessagesChanged(messages)
            }
    }

    suspend fun sendMessage(chatId: String, text: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        val currentUser = getCurrentUser() ?: return false

        return try {
            val messageId = UUID.randomUUID().toString()
            val message = Message(
                id = messageId,
                chatId = chatId,
                senderId = currentUserId,
                senderName = currentUser.displayName,
                text = text,
                timestamp = System.currentTimeMillis()
            )

            messagesCollection.document(messageId).set(message.toMap()).await()

            // Update chat's last message
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to message.timestamp,
                    "lastMessageSenderId" to currentUserId
                )
            ).await()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            false
        }
    }
}
