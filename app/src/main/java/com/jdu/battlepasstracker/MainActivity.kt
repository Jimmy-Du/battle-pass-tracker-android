package com.jdu.battlepasstracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.preference.PreferenceManager
import com.jdu.battlepasstracker.utils.EnqueueDailyWorker
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHost

        navController = navHostFragment.navController
        setupWithNavController(bottomNavigation, navController)

        createNotificationChannels()

        // if the user has set notifications to be enabled, a work manager task is enqueued
        // to send notifications when requested
        if (defaultSharedPrefs.getBoolean("BPNotifications", true)) {
            EnqueueDailyWorker.setupNotificationWorker(this)
        }

        // if the user has set notifications to be enabled, a work manager task is enqueued
        // to send event notifications when requested
        if (defaultSharedPrefs.getBoolean("EventNotifications", true)) {
            EnqueueDailyWorker.setupEventNotificationWorker(this)
        }
    }



    // Function:    createNotificationChannels()
    // Description: creates the notification channels used by the app
    // Parameters:  N/A
    // Return:      N/A
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val battlePassChannel = NotificationChannel(
                getString(R.string.battle_pass_channel_id),
                getString(R.string.battle_pass_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val eventChannel = NotificationChannel(
                getString(R.string.event_channel_id),
                getString(R.string.event_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(battlePassChannel)
            manager.createNotificationChannel(eventChannel)
        }
    }
}