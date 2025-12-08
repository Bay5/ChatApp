package com.bay.chatapp.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bay.chatapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabNewChat: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val root = findViewById<android.view.View>(R.id.mainContainer)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bottomNav = findViewById(R.id.bottomNav)
        fabNewChat = findViewById(R.id.fabNewChat)

        // Default tab → Chats or Contacts (choose one)
        if (savedInstanceState == null) {
            openContacts() // or openChats()
            bottomNav.selectedItemId = R.id.nav_contacts
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_contacts -> {
                    openContacts()
                    true
                }
                R.id.nav_chats -> {
                    openChats()
                    true
                }
                R.id.nav_settings -> {
                    openSettings()
                    true
                }
                else -> false
            }
        }

        fabNewChat.setOnClickListener {
            // Start new chat by searching user
            startActivity(Intent(this, SearchUserActivity::class.java))
        }
    }

    private fun openContacts() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, ContactsFragment())
            .commit()
    }

    private fun openChats() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, ChatsFragment())
            .commit()
    }

    private fun openSettings() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, SettingsFragment())
            .commit()
    }
}
