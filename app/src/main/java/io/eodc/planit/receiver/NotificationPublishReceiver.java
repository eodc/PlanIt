package io.eodc.planit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.eodc.planit.helper.NotificationHelper;

/**
 * A broadcast receiver that is triggered by the alarm scheduled to fire notifications. After
 * receiving, it fires a notification.
 */
public class NotificationPublishReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.fireNotification();
    }
}
