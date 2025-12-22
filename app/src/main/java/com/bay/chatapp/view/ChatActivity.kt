package com.bay.chatapp.view

import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.view.adapter.MessageAdapter
import com.bay.chatapp.viewmodel.ChatViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import kotlin.math.max
import androidx.constraintlayout.widget.ConstraintLayout

class ChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: ImageButton
    private lateinit var toolbar: Toolbar

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter

    private var otherUid: String = ""
    private var otherUsername: String = ""
    private var otherPhotoUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val root: View = findViewById(R.id.rootChat)
        val inputBar: View = findViewById(R.id.chatInputContainer)
        val messagesList: RecyclerView = findViewById(R.id.rvMessages)

        // Ensure content stays above status/nav bars and keyboard
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(inputBar) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val bottom = max(ime, sb)
            v.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = bottom
            }
            // also give the list padding so last message stays visible
            messagesList.setPadding(
                messagesList.paddingLeft,
                messagesList.paddingTop,
                messagesList.paddingRight,
                bottom
            )
            insets
        }

        otherUid = intent.getStringExtra("otherUid") ?: ""
        otherUsername = intent.getStringExtra("otherUsername") ?: ""
        otherPhotoUrl = intent.getStringExtra("otherPhotoUrl") ?: ""

        if (otherUid.isBlank()) {
            Toast.makeText(this, "Missing other user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbarChat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            androidx.core.app.NotificationManagerCompat.from(this)
                .cancel(listOf(currentUid, otherUid).sorted().joinToString("_").hashCode())
            val intent = android.content.Intent(this, MainActivity::class.java).apply {
                putExtra("openTab", "chats")
                addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            finish()
        }

        // Inflate custom toolbar content with avatar + name
        val custom = layoutInflater.inflate(R.layout.toolbar_chat, toolbar, false)
        val lp = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.START }
        toolbar.addView(custom, lp)

        val tvTitle: TextView = custom.findViewById(R.id.tvTitle)
        val ivAvatar: ImageView = custom.findViewById(R.id.ivAvatar)
        tvTitle.text = if (otherUsername.isNotBlank()) otherUsername else "Chat"
        Glide.with(this)
            .load(otherPhotoUrl)
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
            .circleCrop()
            .into(ivAvatar)

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        androidx.core.app.NotificationManagerCompat.from(this)
            .cancel(listOf(currentUid, otherUid).sorted().joinToString("_").hashCode())
        adapter = MessageAdapter(emptyList(), currentUid)

        val lm = LinearLayoutManager(this)
        lm.stackFromEnd = true
        rvMessages.layoutManager = lm
        rvMessages.adapter = adapter

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        viewModel.messages.observe(this) { msgs ->
            adapter.submitList(msgs)
            rvMessages.scrollToPosition(msgs.size - 1)
            viewModel.markAllRead()
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
