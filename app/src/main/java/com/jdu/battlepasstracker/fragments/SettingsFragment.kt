package com.jdu.battlepasstracker.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.WorkManager
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.utils.EnqueueDailyWorker

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val bpNotificationsPreference: SwitchPreferenceCompat? = findPreference("BPNotifications")
        val eventNotificationsPreference: SwitchPreferenceCompat? = findPreference("EventNotifications")

        bpNotificationsPreference?.setOnPreferenceChangeListener { _, newValue ->
            // if the battle pass notifications are switched to true, a work manager task is
            // enqueued to send notifications when requested
            if (newValue.toString().toBoolean()) {
                EnqueueDailyWorker.setupNotificationWorker(requireContext())
            }
            // if the battle pass notifications are switched to false, the notifications work manager is
            // cancelled to prevent notifications from being sent
            else {
                WorkManager.getInstance(requireContext()).cancelUniqueWork(requireContext().getString(R.string.notification_worker_name))
            }

            true
        }


        eventNotificationsPreference?.setOnPreferenceChangeListener { _, newValue ->
            // if the event notifications are switched to true, a work manager task is
            // enqueued to send notifications when requested
            if (newValue.toString().toBoolean()) {
                EnqueueDailyWorker.setupEventNotificationWorker(requireContext())
            }
            // if the event notifications are switched to false, the notifications work manager is
            // cancelled to prevent notifications from being sent
            else {
                WorkManager.getInstance(requireContext()).cancelUniqueWork(requireContext().getString(R.string.event_notification_worker_name))
            }

            true
        }
    }
}