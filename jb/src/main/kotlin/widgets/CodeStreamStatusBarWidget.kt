package com.codestream.widgets

import com.codestream.codeStream
import com.codestream.sessionService
import com.codestream.settingsService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.MouseEvent

class CodeStreamStatusBarWidget(val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {

    var myStatusBar: StatusBar? = null

    init {
        project.sessionService?.let {
            it.onUserLoggedInChanged { refresh() }
            it.onMentionsChanged { refresh() }
        }
    }

    fun refresh() {
        myStatusBar?.updateWidget(ID())
    }

    override fun ID() = "CodeStream.StatusBar"

    override fun getPresentation(type: StatusBarWidget.PlatformType) = this

    override fun install(statusBar: StatusBar) {
        myStatusBar = statusBar
    }

    override fun dispose() = Unit

    override fun getTooltipText() = "Click to open CodeStream"

    override fun getMaxPossibleText() = tooltipText

    override fun getClickConsumer() = Consumer<MouseEvent> {
        project.codeStream?.toggleVisible()
    }

    override fun getText(): String {
        val settingsService = project.settingsService ?: return ""
        val sessionService = project.sessionService ?: return ""

        val prefix = settingsService.environmentDisplayPrefix

        val userLoggedIn = sessionService.userLoggedIn ?: return "$prefix Sign in..."
        val username = if (userLoggedIn.teamsCount == 1) {
            userLoggedIn.user.username
        } else {
            userLoggedIn.user.username + " - " + userLoggedIn.team.name
        }

        val suffix = when (sessionService.mentions) {
            0 -> ""
            in 1..19 -> "(${sessionService.mentions})"
            else -> "(20+)"
        }

        return "$prefix $username $suffix"
    }

    override fun getAlignment(): Float {
        return Component.RIGHT_ALIGNMENT
    }
}
