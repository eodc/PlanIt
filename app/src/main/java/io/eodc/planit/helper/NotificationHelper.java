package io.eodc.planit.helper;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.PreferenceManager;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.eodc.planit.R;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.receiver.NotificationPublishReceiver;

/**
 * Helper to fire, schedule, and cancel notifications
 *
 * @author 2n
 */
public class NotificationHelper extends ContextWrapper {
    private static final int SUMMARY_NOTIF_ID = 99999; // High number in case some person for some reason is taking 99998 classes.....

    private static final String CLASSES_CHANNEL_ID = "classes";
    private static final String REMINDER_CHANNEL_ID = "reminder";
    private static final String GROUP_ID = "assignments";

    private NotificationManager manager;

    /**
     * Constructs a new NotificationHelper
     *
     * @param base The context to use
     */
    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel classes = new NotificationChannel(CLASSES_CHANNEL_ID,
                    getString(R.string.noti_channel_classes),
                    NotificationManager.IMPORTANCE_DEFAULT);
            classes.setSound(null, null);

            NotificationChannel reminder = new NotificationChannel(REMINDER_CHANNEL_ID,
                    getString(R.string.noti_channel_reminder),
                    NotificationManager.IMPORTANCE_DEFAULT);
            reminder.setLightColor(Color.BLUE);
            reminder.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(classes);
            getManager().createNotificationChannel(reminder);
        }
    }

    /**
     * Utility method to convert a string set to a list of integers
     *
     * @param set The string set to convert
     * @return A sorted lsit of
     */
    private static List<Integer> stringSetToIntegerList(Set<String> set) {
        if (set != null) {
            List<Integer> list = new ArrayList<>();
            for (String s : set) {
                list.add(Integer.valueOf(s));
            }
            Collections.sort(list);
            return list;
        }
        return null;
    }

    private NotificationManager getManager() {
        if (manager == null) manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        return manager;
    }

    /**
     * Fires a notification
     */
    public void fireNotification() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showNotification = sharedPreferences.getBoolean(getString(R.string.pref_show_notif_key), true);
        if (showNotification) {
            String whatDayValue = sharedPreferences.getString(getString(R.string.pref_what_assign_show_key), "");
            DateTime dtTimeToShow = new DateTime();
            if (whatDayValue.equals(getString(R.string.pref_what_assign_show_curr_day_value))) {
                dtTimeToShow = dtTimeToShow.withTimeAtStartOfDay();
            } else if (whatDayValue.equals(getString(R.string.pref_what_assign_show_next_day_value))) {
                dtTimeToShow = dtTimeToShow.plusDays(1).withTimeAtStartOfDay();
            }

            String selection = PlannerContract.AssignmentColumns.DUE_DATE + "= date(" + dtTimeToShow.getMillis() / 1000 + ", 'unixepoch', 'localtime') and " +
                    PlannerContract.AssignmentColumns.COMPLETED + "=0";
            Cursor dueAssign = getContentResolver().query(PlannerContract.AssignmentColumns.CONTENT_URI,
                    null, selection, null, PlannerContract.AssignmentColumns.CLASS_ID + " asc");
            Cursor classes = getContentResolver().query(PlannerContract.ClassColumns.CONTENT_URI,
                    null, null, null, PlannerContract.AssignmentColumns._ID + " asc");

            if (dueAssign != null && classes != null) {
                NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_book_black_24dp)
                        .setGroup(GROUP_ID)
                        .setGroupSummary(true);

                NotificationCompat.InboxStyle summaryStyle = new NotificationCompat.InboxStyle();

                int summaryLineCount = 0;
                int overflowClasses = 0;
                int classesWithAssignmentsDue = 0;

                while (classes.moveToNext()) {
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CLASSES_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_book_black_24dp)
                            .setContentTitle(classes.getString(classes.getColumnIndex(PlannerContract.ClassColumns.NAME)))
                            .setAutoCancel(true)
                            .setGroup(GROUP_ID);

                    NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
                    StringBuilder sb = new StringBuilder();
                    String className = classes.getString(classes.getColumnIndex(PlannerContract.ClassColumns.NAME));
                    int classId = classes.getInt(classes.getColumnIndex(PlannerContract.ClassColumns._ID));
                    int assignmentsDue = 0;
                    while (dueAssign.moveToNext() &&
                            dueAssign.getInt(dueAssign.getColumnIndex(PlannerContract.AssignmentColumns.CLASS_ID)) == classId) {
                        sb.append(dueAssign.getString(dueAssign.getColumnIndex(PlannerContract.AssignmentColumns.TITLE)))
                                .append("\n");
                        assignmentsDue++;
                    }
                    if (dueAssign.getPosition() != dueAssign.getCount()) dueAssign.moveToPrevious();
                    if (assignmentsDue > 0) {
                        summaryStyle.addLine(className + " " + (assignmentsDue == 1 ? sb.toString() : assignmentsDue + " assignments due"));
                        summaryLineCount++;
                        if (summaryLineCount > 6) overflowClasses++;

                        notificationStyle.bigText(sb.toString().trim());
                        Notification notif = notificationBuilder.setContentText(assignmentsDue == 1 ? sb.toString() : assignmentsDue + " assignments due")
                                .setStyle(notificationStyle).build();
                        classesWithAssignmentsDue++;
                        if (manager != null) manager.notify(classId, notif);
                        else {
                            NotificationManagerCompat notifManagerCompat = NotificationManagerCompat.from(this);
                            notifManagerCompat.notify(classId, notif);
                        }
                    }
                }
                summaryStyle
                        .setBigContentTitle(classesWithAssignmentsDue + (classesWithAssignmentsDue == 1 ? " class " : " classes ") + "with assignments due");

                if (overflowClasses > 0) {
                    summaryStyle.setSummaryText("+" + overflowClasses + " other " + (overflowClasses == 1 ? "class" : "classes"));
                }

                summaryBuilder.setStyle(summaryStyle);
                Notification summaryNotif = summaryBuilder.build();
                if (manager != null) manager.notify(SUMMARY_NOTIF_ID, summaryNotif);
                else {
                    NotificationManagerCompat notifManagerCompat = NotificationManagerCompat.from(this);
                    notifManagerCompat.notify(SUMMARY_NOTIF_ID, summaryNotif);
                }

                scheduleNotification();

                dueAssign.close();
                classes.close();
            }
        }
    }

    /**
     * Schedules a notification, based on the user's preference
     */
    public void scheduleNotification() {
        Intent intent = new Intent(this, NotificationPublishReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        DateTime dtNow = new DateTime();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        List<Integer> daysToNotify = stringSetToIntegerList(sharedPreferences.getStringSet(getString(R.string.pref_show_notif_days_key), null));

        for (int i = 0; i < daysToNotify.size(); i++) {
            int dayOfWeek = daysToNotify.get(i);
            DateTime dayToNotify = getNotificationTime(dayOfWeek);
            if (dayOfWeek >= dtNow.getDayOfWeek() && dtNow.isBefore(dayToNotify)) {
                setAlarm(dayToNotify, pendingIntent);
                return;
            }
        }

        DateTime dayToNotify = getNotificationTime(daysToNotify.get(0))
                .plusWeeks(1);

        setAlarm(dayToNotify, pendingIntent);
    }

    /**
     * Sets the alarm to be fired at the specified time
     *
     * @param time          The time to fire the notification at
     * @param pendingIntent The pending intent containing a {@link NotificationPublishReceiver}
     */
    private void setAlarm(DateTime time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) alarmManager.set(AlarmManager.RTC_WAKEUP, time.getMillis(), pendingIntent);
    }

    /**
     * Cancels any scheduled notifications
     */
    public void cancelNotification() {
        Intent intent = new Intent(this, NotificationPublishReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) alarmManager.cancel(pendingIntent);
    }

    /**
     * Gets the notification time based on the day of week and user preference
     *
     * @param dayToNotify The weekday to notify on
     * @return A {@link DateTime} containing information on the date and time to fire the notification
     * at
     */
    private DateTime getNotificationTime(int dayToNotify) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        DateTime dtNow = new DateTime();
        String timeToNotify = sharedPreferences.getString(getString(R.string.pref_show_notif_time_key), "");

        DateTime dtNotifyOn = dtNow.withDayOfWeek(dayToNotify);

        String[] timeParts = timeToNotify.split(":");
        dtNotifyOn = dtNotifyOn.withHourOfDay(Integer.valueOf(timeParts[0]));
        dtNotifyOn = dtNotifyOn.withMinuteOfHour(Integer.valueOf(timeParts[1]));
        dtNotifyOn = dtNotifyOn.withSecondOfMinute(0);

        return dtNotifyOn;
    }
}
