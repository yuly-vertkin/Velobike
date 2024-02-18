package ru.sitronics.velobike.domain.chat

import android.content.Context

interface ChatManager {
    fun initialize()
    fun login(user: String)
    fun logout()
    fun showChat(context: Context)
    fun addUnreadMessagesCountListener(listener: (count: Int) -> Unit)
}