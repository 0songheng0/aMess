package com.family.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.family.messenger.databinding.ItemChatBinding
import com.family.messenger.models.Chat
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(
    private val currentUserId: String,
    private val onChatClick: (Chat) -> Unit
) : ListAdapter<Chat, ChatsAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(
        private val binding: ItemChatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            val otherParticipantName = chat.getOtherParticipantName(currentUserId)

            binding.nameTextView.text = otherParticipantName
            binding.lastMessageTextView.text = chat.lastMessage
            binding.timeTextView.text = formatTime(chat.lastMessageTime)

            // Set avatar initial
            binding.avatarTextView.text = otherParticipantName.firstOrNull()?.toString()?.uppercase() ?: "?"

            // Show unread badge if there are unread messages
            if (chat.unreadCount > 0 && chat.lastMessageSenderId != currentUserId) {
                binding.unreadBadge.visibility = View.VISIBLE
                binding.unreadBadge.text = chat.unreadCount.toString()
            } else {
                binding.unreadBadge.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onChatClick(chat)
            }
        }

        private fun formatTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
                diff < 604800000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
                else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}
