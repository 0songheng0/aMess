package com.family.messenger.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.family.messenger.adapters.MessagesAdapter
import com.family.messenger.data.FirebaseRepository
import com.family.messenger.databinding.ActivityChatBinding
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private val repository = FirebaseRepository()
    private var messagesListener: ListenerRegistration? = null
    private lateinit var chatId: String
    private lateinit var chatName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent.getStringExtra("chatId") ?: run {
            finish()
            return
        }

        chatName = intent.getStringExtra("chatName") ?: "Chat"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = chatName
        }

        setupRecyclerView()
        setupListeners()
        loadMessages()
    }

    private fun setupRecyclerView() {
        val currentUserId = repository.getCurrentUserId() ?: return

        messagesAdapter = MessagesAdapter(currentUserId)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.messagesRecyclerView.apply {
            adapter = messagesAdapter
            this.layoutManager = layoutManager
        }
    }

    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadMessages() {
        binding.progressBar.visibility = View.VISIBLE

        messagesListener = repository.listenToMessages(chatId) { messages ->
            binding.progressBar.visibility = View.GONE
            messagesAdapter.submitList(messages) {
                // Scroll to bottom when new message is added
                if (messages.isNotEmpty()) {
                    binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageEditText.text.toString().trim()

        if (messageText.isEmpty()) {
            return
        }

        binding.sendButton.isEnabled = false

        lifecycleScope.launch {
            val success = repository.sendMessage(chatId, messageText)

            binding.sendButton.isEnabled = true

            if (success) {
                binding.messageEditText.text?.clear()
            } else {
                // Show error
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.remove()
    }
}
