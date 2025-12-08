package com.bay.chatapp.view

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.adapter.MessageAdapter
import com.bay.chatapp.viewmodel.ChatViewModel
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText

class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var imgAvatar: ImageView
    private lateinit var tvTitle: TextView

    private var otherUid: String? = null
    private var otherUsername: String? = null
    private var otherPhotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val root: View = findViewById(R.id.rootChat)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        otherUid = intent.getStringExtra("otherUid")
        otherUsername = intent.getStringExtra("otherUsername")
        otherPhotoUrl = intent.getStringExtra("otherPhotoUrl")

        if (otherUid.isNullOrBlank()) {
            Toast.makeText(this, "Missing user info", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        progressBar = ProgressBar(this).apply { visibility = View.GONE } // optional
        btnBack = findViewById(R.id.btnBack)
        imgAvatar = findViewById(R.id.imgAvatarChat)
        tvTitle = findViewById(R.id.tvChatTitle)

        tvTitle.text = otherUsername ?: "Chat"

        otherPhotoUrl?.let { url ->
            if (url.isNotBlank()) {
                Glide.with(this)
                    .load(url)
                    .centerCrop()
                    .into(imgAvatar)
            }
        }

        adapter = MessageAdapter(emptyList())
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = adapter

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        viewModel.messages.observe(this) { list ->
            adapter.submitList(list)
            rvMessages.scrollToPosition(list.size - 1)
        }

        viewModel.error.observe(this) { err ->
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loading.observe(this) { loading ->
            // you can show a small loading indicator if you want
        }

        btnSend.setOnClickListener {
            val text = etMessage.text?.toString().orEmpty()
            viewModel.sendMessage(text)
            etMessage.setText("")
        }

        btnBack.setOnClickListener {
            finish()
        }

        // start listening for this conversation
        viewModel.startChat(otherUid!!)
    }
}
