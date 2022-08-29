package com.jdu.battlepasstracker.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.models.Event
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EventAdapter(val events: List<Event>) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.event_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val today = Calendar.getInstance()
        val formattedEventStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(events[position].event_start_date)
        val formattedEventEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(events[position].event_end_date)
        var eventTimeLeft = TimeUnit.DAYS.convert(events[position].event_end_date.time - today.timeInMillis, TimeUnit.MILLISECONDS)
        val eventDateDiff = TimeUnit.DAYS.convert(events[position].event_end_date.time - events[position].event_start_date.time, TimeUnit.MILLISECONDS)

        holder.eventTitle.text = events[position].event_title
        holder.gameTitle.text = events[position].title
        holder.eventDates.text = String.format("%1s - %2s", formattedEventStartDate, formattedEventEndDate)

        // if the amount of remaining days is less than half of the event duration, the background of the days left
        // will be set to green
        if (eventTimeLeft > eventDateDiff / 2) {
            holder.daysLeftBackground.setBackgroundColor(Color.parseColor("#22c55e"))
        }
        // if the amount of remaining days is less than a quarter of the event, the background of the days left
        // will be set to red
        else if (eventTimeLeft <= eventDateDiff / 4) {
            holder.daysLeftBackground.setBackgroundColor(Color.parseColor("#ef4444"))
        }
        else {
            holder.daysLeftBackground.setBackgroundColor(Color.parseColor("#eab308"))
        }

        // if there are 0 full days left for the event, the amount of hours left will be displayed instead
        if (eventTimeLeft == 0.toLong()) {
            eventTimeLeft = TimeUnit.HOURS.convert(events[position].event_end_date.time - today.timeInMillis, TimeUnit.MILLISECONDS)
            holder.daysLeft.text = eventTimeLeft.toString()
            holder.daysLeftLabel.text = "Hours Left"
        }
        // else, the amount of full days remaining is displayed
        else {
            holder.daysLeft.text = eventTimeLeft.toString()
        }
    }

    override fun getItemCount(): Int {
        return events.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var eventTitle: TextView
        var gameTitle: TextView
        var eventDates: TextView
        var daysLeft: TextView
        var daysLeftLabel: TextView
        var daysLeftBackground: View

        init {
            eventTitle = itemView.findViewById(R.id.eventTextView)
            gameTitle = itemView.findViewById(R.id.gameTextView)
            eventDates = itemView.findViewById(R.id.datesTextView)
            daysLeft = itemView.findViewById(R.id.daysLeftTextView)
            daysLeftLabel = itemView.findViewById(R.id.daysLeftLabelTextView)
            daysLeftBackground = itemView.findViewById(R.id.daysLeftBackground)
        }
    }
}