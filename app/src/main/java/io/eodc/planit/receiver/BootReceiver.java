package io.eodc.planit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.eodc.planit.helper.NotificationHelper;

/**
 * A broadcast receiver that catches an intent that fires after the phone is booted. It catches
 * this intent, and schedules the notification. This is needed because alarms, what is used to
 * schedule notifications, are automatically all canceled when the phone shuts off.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            NotificationHelper helper = new NotificationHelper(context);
            helper.scheduleNotification();
        }
    }
}
