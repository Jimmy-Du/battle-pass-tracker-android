package com.jdu.battlepasstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.models.Game
import com.google.android.material.card.MaterialCardView

class SelectGameAdapter(val games: List<Game>, val selectedGames: ArrayList<Int>): RecyclerView.Adapter<SelectGameAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectGameAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.select_game_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.selectGame.text = games[position].title
        holder.selectGameCheckBox.isChecked = selectedGames.contains(games[position].id)
        holder.selectGameCard.setOnClickListener {
            holder.selectGameCheckBox.isChecked = !holder.selectGameCheckBox.isChecked
            selectGameClick(games[position].id)
        }
        holder.selectGameCheckBox.setOnClickListener {
            selectGameClick(games[position].id)
        }
    }

    override fun getItemCount(): Int {
        return games.size
    }

    // Function:    selectGameClick()
    // Description: called upon when a game on the select games fragment is clicked,
    //              will then add/remove the game from the selected games array
    // Parameters:  gameId: the id of the game that was clicked
    // Return:      N/A
    private fun selectGameClick(gameId: Int) {
        // if the selectedGames array contains the id of the game selected,
        // the clicked on game will be removed
        if (selectedGames.contains(gameId)) {
            selectedGames.remove(gameId)
        }
        // else, the id of the game clicked will be added to the selected games array
        else {
            selectedGames.add(gameId)
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var selectGameCard: MaterialCardView
        var selectGame: TextView
        var selectGameCheckBox: CheckBox

        init {
            selectGameCard = itemView.findViewById(R.id.selectGameCard)
            selectGame = itemView.findViewById(R.id.selectGameTextView)
            selectGameCheckBox = itemView.findViewById(R.id.selectGameCheckBox)
        }
    }
}