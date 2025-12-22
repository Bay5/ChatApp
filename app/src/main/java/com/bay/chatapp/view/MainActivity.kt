package com.bay.chatapp.view

import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bay.chatapp.R
import com.bay.chatapp.notification.MessageNotificationManager
import com.bay.chatapp.notification.NotificationHelper
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: ChipNavigationBar

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
                bottomNav.setItemSelected(R.id.nav_chats, true)
            } else {
                openContacts()
                bottomNav.setItemSelected(R.id.nav_contacts, true)
            }
        }

        bottomNav.setOnItemSelectedListener { id ->
            when (id) {
                R.id.nav_contacts -> {
                    openContacts()
                }
                R.id.nav_chats -> {
                    openChats()
                }
                R.id.nav_settings -> {
                    openSettings()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tab = intent.getStringExtra("openTab")
        if (tab == "chats") {
            openChats()
            bottomNav.setItemSelected(R.id.nav_chats, true)
        } else if (tab == "contacts") {
            openContacts()
            bottomNav.setItemSelected(R.id.nav_contacts, true)
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
