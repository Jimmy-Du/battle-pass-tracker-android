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
import androidx.work.*
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.MainActivity
import com.jdu.battlepasstracker.api.RetrofitInstance
import com.jdu.battlepasstracker.models.Game
import com.jdu.battlepasstracker.utils.BPTPreferences
import com.jdu.battlepasstracker.utils.EnqueueDailyWorker
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val responseCall = RetrofitInstance.api.getGames(BPTPreferences.getSelectedGamesInJson(applicationContext))
        val response = responseCall.execute()

        // if the status code of the response starts with a 2, indicating a successful response,
        // will then check to see if any notifications should be sent, and will queue another
        // worker to check again in 24hrs
        return if (response.code().toString().startsWith('2')) {
            processGameDates(response.body()!!.sortedBy { it.title })
            EnqueueDailyWorker.setupNotificationWorker(applicationContext)

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



    // Function:    processGameDates()
    // Description: checks all the dates of the games passed in, if a game's battle pass has
    //              just reached a quarter of the duration or a week is left or it is, the last day
    //              a notification is sent
    // Parameters:  games: a list of games that will be evaluated to see if a notification
    //              should be sent indicating how many days are left for a battle pass
    // Return:      N/A
    private fun processGameDates(games: List<Game>) {
        // for loop that goes through the list of games and will send a notification if
        // a certain criteria is met
        for (game in games) {
            val notificationPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            var seasonTimeLeft = TimeUnit.DAYS.convert(game.season_end_date.time - today.timeInMillis, TimeUnit.MILLISECONDS)
            val seasonDateDiff = TimeUnit.DAYS.convert(game.season_end_date.time - game.season_start_date.time, TimeUnit.MILLISECONDS)
            var notificationString = ""
            var notificationTitle = "${game.title} Battle Pass"

            // if the remaining days and the total duration of the season are equal and the user has enabled
            // notifications for when new seasons start, a notification is created and sent
            if (DateUtils.isToday(game.season_start_date.time) && notificationPreferences.getBoolean("BPStartNotifications", true)) {
                notificationTitle = "$notificationTitle - New Season Begins Today"
                notificationString = "The ${game.season_title} battle pass begins today in ${game.title}!"
            }
            // if the remaining days is half the total duration of the season and the user has enabled
            // season halfway notifications, a notification is created and sent
            else if (seasonTimeLeft == seasonDateDiff / 2 && notificationPreferences.getBoolean("BPHalfWayNotifications", true)) {
                notificationTitle = "$notificationTitle - Halfway Over"
                notificationString = "It is the midpoint of the ${game.season_title} battle pass for ${game.title} with $seasonTimeLeft days remaining!"
            }
            // if the remaining days is a quarter the total duration of the season and the user has enabled
            // season quarter notifications, a notification is created and sent
            else if (seasonTimeLeft == seasonDateDiff / 4 && notificationPreferences.getBoolean("BPQuarterNotifications", true)) {
                notificationTitle = "$notificationTitle - A Quarter Left"
                notificationString = "There is only $seasonTimeLeft days left in the ${game.season_title} battle pass for ${game.title}"
            }
            // if the remaining days is 7 days/1 week and the user has enabled
            // season final week notifications, a notification is created and sent
            else if (seasonTimeLeft == 7.toLong() && notificationPreferences.getBoolean("BPFinalWeekNotifications", true)) {
                notificationTitle = "$notificationTitle - 1 Week Left"
                notificationString = "1 week remaining for the ${game.title} ${game.season_title} battle pass!"
            }
            // if the remaining days is equal to 1 and the user has enabled
            // season final day notifications, a notification is created and sent
            else if (seasonTimeLeft == 1.toLong() && notificationPreferences.getBoolean("BPFinalDayNotifications", true)) {
                notificationTitle = "$notificationTitle - Last Day"
                notificationString = "It is the final day to complete the ${game.title} ${game.season_title} battle pass!"
            }
            // if the remaining days is equal to 0 and the user has enabled season final day notifications,
            // the remaining hours is calculated, and a notification is created and sent
            else if (seasonTimeLeft == 0.toLong() && notificationPreferences.getBoolean("BPFinalDayNotifications", true)) {
                seasonTimeLeft = TimeUnit.HOURS.convert(game.season_end_date.time - today.timeInMillis, TimeUnit.MILLISECONDS)

                notificationTitle = "$notificationTitle - Hours Remaining"
                notificationString = "There are only $seasonTimeLeft hours left in the ${game.title} ${game.season_title} battle pass!"
            }
            // if the user has weekly notifications enabled, and it is the day that they have set weekly notifications
            // to appear, a notification is created and sent
            else if (today.get(Calendar.DAY_OF_WEEK) == notificationPreferences.getString("BPWeeklyNotificationsDay", "1")!!.toInt() &&
                     notificationPreferences.getBoolean("BPWeeklyNotifications", false)) {
                notificationTitle = "$notificationTitle - Weekly Reminder"
                notificationString = "$seasonTimeLeft days remain for the ${game.title} ${game.season_title} battle pass."
            }

            // if the notification string has been altered, indicating that a notification should
            // be sent, a notification will be created and displayed
            if (notificationString != "") {
                createAndSendNotification(
                    game.id,
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
    private fun createAndSendNotification(id: Int, title: String, message: String) {
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
            applicationContext.getString(R.string.battle_pass_channel_id)
        )
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_baseline_videogame_asset_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(id, notification)
    }
}