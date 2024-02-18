package ru.sitronics.velobike.data.managers

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import im.threads.UserInfoBuilder
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.ui.activities.ChatActivity
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import ru.sitronics.velobike.domain.chat.ChatManager
import ru.sitronics.velobike.tools.Logg
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatManagerImp @Inject constructor(
    @ApplicationContext val appContext: Context
) : ChatManager, UnreadMessagesCountListener {
    private var unreadMessagesCountListener: ((count: Int) -> Unit)? = null
    private var userId: String = ""
    private var isInitialized = false

    override fun initialize() {
        if (!isInitialized) {
            Logg.d("Chat service is initializing")
            val configBuilder = ConfigBuilder(appContext)
                .unreadMessagesCountListener(this)
                .surveyCompletionDelay(2000)
                .historyLoadingCount(50)
                .isDebugLoggingEnabled(true)
                .datastoreUrl("https://mosmetro-ds.threads.im/")
            ThreadsLib.init(configBuilder)
            isInitialized = true
        } else {
            Logg.d("Chat service is already initialized")
        }
    }

    override fun addUnreadMessagesCountListener(listener: (count: Int) -> Unit) {
        unreadMessagesCountListener = listener
    }

    override fun onUnreadMessagesCountChanged(count: Int) {
        Logg.d("!!!!! onUnreadMessagesCountChanged: $count")
        unreadMessagesCountListener?.invoke(count)
    }

    override fun login(user: String) {
        if (isInitialized) {
            this.userId = "vb_$user"
            ThreadsLib.getInstance().initUser(
                UserInfoBuilder(userId).setAppMarker(APP_MARKER)
            )
        } else {
            Logg.d("Chat service is NOT initialized")
        }
    }

    override fun logout() {
        if (isInitialized) {
            ThreadsLib.getInstance().logoutClient(userId)
            unreadMessagesCountListener = null
        } else {
            Logg.d("Chat service is NOT initialized")
        }
    }

    override fun showChat(context: Context) {
        context.startActivity(Intent(context, ChatActivity::class.java))
    }

    companion object {
        const val APP_MARKER = "velobike_android"
    }
}