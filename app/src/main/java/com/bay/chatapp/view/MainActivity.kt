package com.bay.chatapp.view

import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bay.chatapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.bay.chatapp.notification.MessageNotificationManager
import com.bay.chatapp.notification.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        NotificationHelper.createChannel(this)
        MessageNotificationManager.start(this)

        val root = findViewById<android.view.View>(R.id.mainContainer)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            val tab = intent?.getStringExtra("openTab")
            if (tab == "chats") {
                openChats()
                bottomNav.selectedItemId = R.id.nav_chats
            } else {
                openContacts()
                bottomNav.selectedItemId = R.id.nav_contacts
            }
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tab = intent.getStringExtra("openTab")
        if (tab == "chats") {
            openChats()
            bottomNav.selectedItemId = R.id.nav_chats
        } else if (tab == "contacts") {
            openContacts()
            bottomNav.selectedItemId = R.id.nav_contacts
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
