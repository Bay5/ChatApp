package com.bay.chatapp.view

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.adapter.MessageAdapter
import com.bay.chatapp.viewmodel.ChatViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: ImageButton
    private lateinit var toolbar: Toolbar

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter

    private var otherUid: String = ""
    private var otherUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val root: View = findViewById(R.id.rootChat)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        otherUid = intent.getStringExtra("otherUid") ?: ""
        otherUsername = intent.getStringExtra("otherUsername") ?: ""

        if (otherUid.isBlank()) {
            Toast.makeText(this, "Missing other user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbarChat)
        toolbar.title = if (otherUsername.isNotBlank()) otherUsername else "Chat"
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        adapter = MessageAdapter(emptyList(), currentUid)

        val lm = LinearLayoutManager(this)
        lm.stackFromEnd = true
        rvMessages.layoutManager = lm
        rvMessages.adapter = adapter

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        viewModel.messages.observe(this) { msgs ->
            adapter.submitList(msgs)
            rvMessages.scrollToPosition(msgs.size - 1)
        }

        viewModel.error.observe(this) { err ->
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
            }
        }

        btnSend.setOnClickListener {
            val text = etMessage.text?.toString().orEmpty()
            if (text.isBlank()) return@setOnClickListener

            viewModel.sendMessage(text)
            etMessage.setText("")
        }

        // start listening after everything set
        viewModel.startChat(otherUid)
    }
}
