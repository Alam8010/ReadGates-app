package com.example.readgates

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppBlockerService : AccessibilityService() {

    private val blockedApps = listOf("com.google.android.youtube")

    companion object {
        @Volatile var overlayActive = false
        @Volatile var unlockUntil = 0L
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (blockedApps.contains(packageName)) {
                if (overlayActive) return
                if (System.currentTimeMillis() < unlockUntil) return
                overlayActive = true
                val intent = Intent(this, OverlayActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}