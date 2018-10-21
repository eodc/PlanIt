package io.eodc.planit.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import io.eodc.planit.helper.NotificationHelper

/**
 * A broadcast receiver that is triggered by the alarm scheduled to fire notifications. After
 * receiving, it fires a notification.
 */
class NotificationPublishReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.fireNotification()
    }
}
