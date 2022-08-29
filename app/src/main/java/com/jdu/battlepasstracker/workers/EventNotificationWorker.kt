package com.jdu.battlepasstracker.workers

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jdu.battlepasstracker.MainActivity
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.api.RetrofitInstance
import com.jdu.battlepasstracker.models.Event
import com.jdu.battlepasstracker.utils.BPTPreferences
import com.jdu.battlepasstracker.utils.EnqueueDailyWorker
import java.util.*
import java.util.concurrent.TimeUnit

class EventNotificationWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams)  {
    override fun doWork(): Result {
        val responseCall = RetrofitInstance.api.getEvents(BPTPreferences.getSelectedGamesInJson(applicationContext))
        val response = responseCall.execute()

        return if (response.code().toString().startsWith('2')) {
            processEventDates(response.body()!!)
            EnqueueDailyWorker.setupEventNotificationWorker(applicationContext)

            Result.success()
        }
        // if the status code of the response starts with a 5, indicating a server error,
        // an attempt to try again will be made
        else if (response.code().toString().startsWith('5')) {
            Result.retry()
        }
        // else, the worker will fail and not be attempted again
        else  {
            Result.failure()
        }
    }



    // Function:    processEventDates()
    // Description: checks all the dates of the events passed in, if an event has
    //              just reached a quarter of the duration or a week is left or it is, the last day
    //              a notification is sent
    // Parameters:  events: a list of events that will be evaluated to see if a notification
    //              should be sent indicating how many days are left for an event
    // Return:      N/A
    private fun processEventDates(events: List<Event>) {
        for (event in events) {
            val notificationPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            var eventTimeLeft = TimeUnit.DAYS.convert(event.event_end_date.time - today.timeInMillis, TimeUnit.MILLISECONDS)
            val eventDateDiff = TimeUnit.DAYS.convert(event.event_end_date.time - event.event_start_date.time, TimeUnit.MILLISECONDS)
            var notificationString = ""
            var notificationTitle = event.event_title

            // if the remaining days and the total duration of the event are equal and the user has enabled
            // notifications for when new events start, a notification is created and sent
            if (DateUtils.isToday(event.event_end_date.time) && notificationPreferences.getBoolean("EventStartNotifications", true)) {
                notificationTitle = "$notificationTitle - Begins Today!"
                notificationString = "The ${event.event_title} begins today in ${event.title}!"
            }
            // if the remaining days is half the total duration of the event and the user has enabled
            // event halfway notifications, a notification is created and sent
            else if (eventTimeLeft == eventDateDiff / 2 && notificationPreferences.getBoolean("EventHalfWayNotifications", true)) {
                notificationTitle = "$notificationTitle - Halfway Over"
                notificationString = "It is the midpoint of the ${event.event_title} event for ${event.title} with $eventTimeLeft days remaining!"
            }
            // if the remaining days is 1 and the user has enabled
            // event final day notifications, a notification is created and sent
            else if (eventTimeLeft == 1.toLong() && notificationPreferences.getBoolean("EventFinalDayNotifications", true)) {
                notificationTitle = "$notificationTitle - 1 Day Left"
                notificationString = "1 day remaining for the ${event.event_title} event in ${event.title}!"
            }
            // if the remaining days is 0 and the user has enable event final day notifications,
            // the amount of hours left in the event is calculated, and a notification is created and sent
            else if (eventTimeLeft == 0.toLong() && notificationPreferences.getBoolean("EventFinalDayNotifications", true)) {
                eventTimeLeft = TimeUnit.HOURS.convert(event.event_end_date.time - today.timeInMillis, TimeUnit.MILLISECONDS)

                notificationTitle = "$notificationTitle - Hours Left"
                notificationString = "$eventTimeLeft hours remaining for the ${event.event_title} event in ${event.title}!"
            }
            // if the remaining days is a quarter the total duration of the event and the user has enabled
            // event quarter notifications, a notification is created and sent
            else if (eventTimeLeft == eventDateDiff / 4 && notificationPreferences.getBoolean("EventQuarterNotifications", true)) {
                notificationTitle = "$notificationTitle - A Quarter Left"
                notificationString = "There is only $eventTimeLeft days left in the ${event.event_title} event for ${event.title}"
            }
            // if the remaining days is 7 days/1 week and the user has enabled
            // event final week notifications, a notification is created and sent
            else if (eventTimeLeft == 7.toLong() && notificationPreferences.getBoolean("EventFinalWeekNotifications", true)) {
                notificationTitle = "$notificationTitle - 1 Week Left"
                notificationString = "1 week remaining for the ${event.event_title} event in ${event.title}!"
            }

            if (notificationString != "") {
                createAndSendEventNotification(
                    event.id,
                    notificationTitle,
                    notificationString
                )
            }
        }
    }



    // Function:    createAndSendNotification()
    // Description: creates and displays a notification with the title and message passed in
    // Parameters:  id: the id of the notification
    //              title: the title of the notification
    //              message: the message of the notification
    // Return:      N/A
    private fun createAndSendEventNotification(id: Int, title: String, message: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.event_channel_id)
        )
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_baseline_event_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(id, notification)
    }
}