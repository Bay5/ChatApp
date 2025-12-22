package com.bay.chatapp.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bay.chatapp.view.ContactsFragment
import com.bay.chatapp.view.ChatsFragment
import com.bay.chatapp.view.SettingsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ContactsFragment()
            1 -> ChatsFragment()
            2 -> SettingsFragment()
            else -> ChatsFragment()
        }
    }
}
