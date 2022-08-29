package com.jdu.battlepasstracker.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.models.Game
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GameAdapter(val games: List<Game>): RecyclerView.Adapter<GameAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.game_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: GameAdapter.ViewHolder, position: Int) {
        val today = Calendar.getInstance()
        val formattedSeasonStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(games[position].season_start_date)
        val formattedSeasonEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(games[position].season_end_date)
        var seasonTimeLeft = TimeUnit.DAYS.convert(games[position].season_end_date.time - today.timeInMillis, TimeUnit.MILLISECONDS)
        val seasonDateDiff = TimeUnit.DAYS.convert(games[position].season_end_date.time - games[position].season_start_date.time, TimeUnit.MILLISECONDS)

        holder.gameTitle.text = games[position].title
        holder.seasonTitle.text = games[position].season_title
        holder.seasonDates.text = String.format("%1s - %2s", formattedSeasonStartDate, formattedSeasonEndDate)

        // if the amount of remaining days is less than half of the season, the background of the days left
        // will be set to green
        if (seasonTimeLeft > seasonDateDiff / 2) {
            holder.daysLeftBackground.setBackgroundColor(Color.parseColor("#22c55e"))
        }
        // if the amount of remaining days is less than a quarter of the season, the background of the days left
        // will be set to red
        else if (seasonTimeLeft <= seasonDateDiff / 4) {
            holder.daysLeftBackground.setBackgroundColor(Color.parseColor("#ef4444"))
        }
        else {
            holder.daysLeftBackground.setBackgroundColor(Color.parseColor("#eab308"))
        }

        // if there are 0 full days left for the season, the amount of hours left will be displayed instead
        if (seasonTimeLeft == 0.toLong()) {
            seasonTimeLeft = TimeUnit.HOURS.convert(games[position].season_end_date.time - today.timeInMillis, TimeUnit.MILLISECONDS)
            holder.daysLeft.text = seasonTimeLeft.toString()
            holder.daysLeftLabel.text = "Hours Left"
        }
        // else, the amount of full days remaining is displayed
        else {
            holder.daysLeft.text = seasonTimeLeft.toString()
        }
    }

    override fun getItemCount(): Int {
        return games.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var gameTitle: TextView
        var seasonTitle: TextView
        var seasonDates: TextView
        var daysLeft: TextView
        var daysLeftLabel: TextView
        var daysLeftBackground: View

        init {
            gameTitle = itemView.findViewById(R.id.gameTextView)
            seasonTitle = itemView.findViewById(R.id.seasonTextView)
            seasonDates = itemView.findViewById(R.id.datesTextView)
            daysLeft = itemView.findViewById(R.id.daysLeftTextView)
            daysLeftLabel = itemView.findViewById(R.id.daysLeftLabelTextView)
            daysLeftBackground = itemView.findViewById(R.id.daysLeftBackground)
        }
    }
}