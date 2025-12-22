package com.bay.chatapp

import android.app.Application

class ChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    companion object {
        lateinit var instance: ChatApp
            private set
    }
}
