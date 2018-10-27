package io.eodc.planit.helper

import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import io.eodc.planit.R
import io.eodc.planit.db.PlannerDatabase
import io.eodc.planit.receiver.NotificationPublishReceiver
import org.joda.time.DateTime

/**
 * Helper to fire, schedule, and cancel notifications
 *
 * @author 2n
 */
class NotificationHelper
/**
 * Constructs a new NotificationHelper
 *
 * @param base The context to use
 */
(base: Context) : ContextWrapper(base) {

    private var mManager: NotificationManager? = null

    private val manager: NotificationManager
        get() {
            if (mManager == null) mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return mManager!!
        }

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val classes = NotificationChannel(SUBJECTS_CHANNEL_ID,
                    getString(R.string.notif_channel_classes),
                    NotificationManager.IMPORTANCE_DEFAULT)
            classes.setSound(null, null)

            val reminder = NotificationChannel(REMINDER_CHANNEL_ID,
                    getString(R.string.notif_channel_reminder),
                    NotificationManager.IMPORTANCE_DEFAULT)
            reminder.lightColor = Color.BLUE
            reminder.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            manager.createNotificationChannel(classes)
            manager.createNotificationChannel(reminder)
        }
    }

    /**
     * Fires a notification
     */
    fun fireNotification() {
        Thread {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val showNotification = sharedPreferences.getBoolean(getString(R.string.pref_show_notif_key), true)
            if (showNotification) {
                val whatDayValue = sharedPreferences.getString(getString(R.string.pref_what_assign_show_key), "")
                var dtToShow = DateTime()
                if (whatDayValue == getString(R.string.pref_what_assign_show_curr_day_value)) {
                    dtToShow = dtToShow.withTimeAtStartOfDay()
                } else if (whatDayValue == getString(R.string.pref_what_assign_show_next_day_value)) {
                    dtToShow = dtToShow.plusDays(1).withTimeAtStartOfDay()
                }

                val dueAssignments = PlannerDatabase.getInstance(this)!!.assignmentDao()
                        .getStaticAssignmentsDueBetweenDates(dtToShow, dtToShow.plusDays(1))
                val subjects = PlannerDatabase.getInstance(this)!!.classDao().allSubjects

                val summaryBuilder = NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_book_black_24dp)
                        .setGroup(GROUP_ID)
                        .setGroupSummary(true)

                val summaryStyle = NotificationCompat.InboxStyle()

                var summaryLineCount = 0
                var overflowClasses = 0
                var classesWithAssignmentsDue = 0

                for (currentSubject in subjects) {
                    val notificationBuilder = NotificationCompat.Builder(this, SUBJECTS_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_book_black_24dp)
                            .setContentTitle(currentSubject.name)
                            .setAutoCancel(true)
                            .setGroup(GROUP_ID)

                    val notificationStyle = NotificationCompat.BigTextStyle()
                    val sb = StringBuilder()
                    val className = currentSubject.name
                    val classId = currentSubject.id
                    var assignmentsDue = 0
                    for (assign in dueAssignments) {
                        if (assign.classId == currentSubject.id) {
                            sb.append(assign.title)
                                    .append("\n")
                            assignmentsDue++
                        }
                    }
                    if (assignmentsDue > 0) {
                        summaryStyle.addLine(className + " " + if (assignmentsDue == 1) sb.toString() else assignmentsDue.toString() + " assignments due")
                        summaryLineCount++
                        if (summaryLineCount > 6) overflowClasses++

                        notificationStyle.bigText(sb.toString().trim { it <= ' ' })
                        val notif = notificationBuilder.setContentText(if (assignmentsDue == 1) sb.toString() else assignmentsDue.toString() + " assignments due")
                                .setStyle(notificationStyle).build()
                        classesWithAssignmentsDue++
                        if (mManager != null)
                            mManager!!.notify(classId, notif)
                        else {
                            val notifManagerCompat = NotificationManagerCompat.from(this)
                            notifManagerCompat.notify(classId, notif)
                        }
                    }
                }
                summaryStyle
                        .setBigContentTitle(classesWithAssignmentsDue.toString() + (if (classesWithAssignmentsDue == 1) " class " else " subjects ") + "with assignments due")

                if (overflowClasses > 0) {
                    summaryStyle.setSummaryText("+" + overflowClasses + " other " + if (overflowClasses == 1) "class" else "subjects")
                }

                summaryBuilder.setStyle(summaryStyle)
                val summaryNotif = summaryBuilder.build()
                if (mManager != null)
                    mManager!!.notify(SUMMARY_NOTIF_ID, summaryNotif)
                else {
                    val notifManagerCompat = NotificationManagerCompat.from(this)
                    notifManagerCompat.notify(SUMMARY_NOTIF_ID, summaryNotif)
                }

                scheduleNotification()
            }
        }.start()
    }

    /**
     * Schedules a notification, based on the user's preference
     */
    fun scheduleNotification() {
        val intent = Intent(this, NotificationPublishReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val dtNow = DateTime()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val daysToNotify = sharedPreferences.getStringSet(getString(R.string.pref_show_notif_days_key), null)

        if (daysToNotify != null) {
            if (dtNow.dayOfWeek + 1 < 8) {
                for (i in dtNow.dayOfWeek + 1..7) {
                    if (daysToNotify.contains(Integer.toString(i))) {
                        setAlarm(getNotificationTime(i), pendingIntent)
                        return
                    }
                }
            }

            val dayToNotify = getNotificationTime(daysToNotify.iterator().next().toInt())
                    .plusWeeks(1)

            setAlarm(dayToNotify, pendingIntent)
        }
    }

    /**
     * Sets the alarm to be fired at the specified time
     *
     * @param time          The time to fire the notification at
     * @param pendingIntent The pending intent containing a [NotificationPublishReceiver]
     */
    private fun setAlarm(time: DateTime, pendingIntent: PendingIntent) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, time.millis, pendingIntent)
    }

    /**
     * Cancels any scheduled notifications
     */
    fun cancelNotification() {
        val intent = Intent(this, NotificationPublishReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Gets the notification time based on the day of week and user preference
     *
     * @param dayToNotify The weekday to notify on
     * @return A [DateTime] containing information on the date and time to fire the notification
     * at
     */
    private fun getNotificationTime(dayToNotify: Int): DateTime {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val timeToNotify = sharedPreferences.getString(getString(R.string.pref_show_notif_time_key), "")

        val timeParts = timeToNotify!!.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        return DateTime().withDayOfWeek(dayToNotify)
                .withHourOfDay(Integer.valueOf(timeParts[0]))
                .withMinuteOfHour(Integer.valueOf(timeParts[1]))
                .withSecondOfMinute(0)
                .withMillisOfSecond(0)
    }

    companion object {
        private const val SUMMARY_NOTIF_ID = 99999 // High number in case some person for some reason is taking 99998 subjects.....

        private const val SUBJECTS_CHANNEL_ID = "subjects"
        private const val REMINDER_CHANNEL_ID = "reminder"
        private const val GROUP_ID = "assignments"

    }
}
