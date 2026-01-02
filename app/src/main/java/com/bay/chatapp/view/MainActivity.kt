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
import androidx.viewpager2.widget.ViewPager2
import com.bay.chatapp.view.adapter.MainPagerAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: ChipNavigationBar
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPrefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        val mode = if (isDarkMode) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        NotificationHelper.createChannel(this)
        // Start background service instead of direct manager call
        val intent = android.content.Intent(this, com.bay.chatapp.service.MessageService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        val root = findViewById<android.view.View>(R.id.viewPagerMain)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bottomNav = findViewById(R.id.bottomNav)
        viewPager = findViewById(R.id.viewPagerMain)
        viewPager.adapter = MainPagerAdapter(this)
        viewPager.offscreenPageLimit = 2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomNav.setItemSelected(R.id.nav_contacts, true)
                    1 -> bottomNav.setItemSelected(R.id.nav_chats, true)
                    2 -> bottomNav.setItemSelected(R.id.nav_settings, true)
                }
            }
        })

        if (savedInstanceState == null) {
            val tab = intent?.getStringExtra("openTab")
            if (tab == "contacts") {
                openContacts()
            } else if (tab == "settings") {
                openSettings()
            } else {
                openChats()
            }
        }

        bottomNav.setOnItemSelectedListener { id ->
            when (id) {
                R.id.nav_contacts -> openContacts()
                R.id.nav_chats -> openChats()
                R.id.nav_settings -> openSettings()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tab = intent.getStringExtra("openTab")
        if (tab == "chats") {
            openChats()
        } else if (tab == "contacts") {
            openContacts()
        } else if (tab == "settings") {
            openSettings()
        }
    }

    private fun openContacts() {
        viewPager.currentItem = 0
        bottomNav.setItemSelected(R.id.nav_contacts, true)
    }

    private fun openChats() {
        viewPager.currentItem = 1
        bottomNav.setItemSelected(R.id.nav_chats, true)
    }

    private fun openSettings() {
        viewPager.currentItem = 2
        bottomNav.setItemSelected(R.id.nav_settings, true)
    }
}
