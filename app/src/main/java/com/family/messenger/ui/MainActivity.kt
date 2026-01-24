package com.family.messenger.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.family.messenger.R
import com.family.messenger.adapters.ChatsAdapter
import com.family.messenger.adapters.UsersAdapter
import com.family.messenger.data.FirebaseRepository
import com.family.messenger.databinding.ActivityMainBinding
import com.family.messenger.databinding.DialogNewChatBinding
import com.family.messenger.models.Chat
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatsAdapter: ChatsAdapter
    private val repository = FirebaseRepository()
    private var chatsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        if (repository.getCurrentUserId() == null) {
            navigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupListeners()
        loadChats()

        // Update online status
        lifecycleScope.launch {
            repository.updateUserOnlineStatus(true)
        }
    }

    private fun setupRecyclerView() {
        val currentUserId = repository.getCurrentUserId() ?: return

        chatsAdapter = ChatsAdapter(currentUserId) { chat ->
            openChat(chat)
        }

        binding.chatsRecyclerView.apply {
            adapter = chatsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupListeners() {
        binding.newChatFab.setOnClickListener {
            showNewChatDialog()
        }
    }

    private fun loadChats() {
        binding.progressBar.visibility = View.VISIBLE

        chatsListener = repository.listenToChats { chats ->
            binding.progressBar.visibility = View.GONE

            if (chats.isEmpty()) {
                binding.emptyTextView.visibility = View.VISIBLE
                binding.chatsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyTextView.visibility = View.GONE
                binding.chatsRecyclerView.visibility = View.VISIBLE
                chatsAdapter.submitList(chats)
            }
        }
    }

    private fun showNewChatDialog() {
        val dialogBinding = DialogNewChatBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        val usersAdapter = UsersAdapter { user ->
            dialog.dismiss()
            lifecycleScope.launch {
                val chatId = repository.createOrGetChat(user.uid)
                if (chatId != null) {
                    // Find or create chat object
                    val chat = Chat(
                        id = chatId,
                        participants = listOf(repository.getCurrentUserId()!!, user.uid),
                        participantNames = mapOf(
                            repository.getCurrentUserId()!! to (repository.getCurrentUser()?.displayName ?: ""),
                            user.uid to user.displayName
                        )
                    )
                    openChat(chat)
                }
            }
        }

        dialogBinding.usersRecyclerView.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        dialogBinding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val users = repository.getAllUsers()
            dialogBinding.progressBar.visibility = View.GONE
            usersAdapter.submitList(users)
        }

        dialog.show()
    }

    private fun openChat(chat: Chat) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chat.id)
            putExtra("chatName", chat.getOtherParticipantName(repository.getCurrentUserId()!!))
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                repository.signOut()
                navigateToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatsListener?.remove()

        lifecycleScope.launch {
            repository.updateUserOnlineStatus(false)
        }
    }
}
