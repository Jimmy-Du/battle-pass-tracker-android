package com.jdu.battlepasstracker.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.adapters.SelectGameAdapter
import com.jdu.battlepasstracker.api.RetrofitInstance
import com.jdu.battlepasstracker.models.Game
import com.jdu.battlepasstracker.utils.BPTPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectGameFragment : Fragment(R.layout.fragment_select_game) {
    var selectedGames: ArrayList<Int> = arrayListOf()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectGameRecycler: RecyclerView = view.findViewById(R.id.selectGamesRecycler)
        val setSelectGamesBtn: Button = view.findViewById(R.id.setSelectedGamesBtn)
        val selectGamesProgressBar: ProgressBar = view.findViewById(R.id.selectGamesProgressBar)

        setSelectGamesBtn.setOnClickListener {
            BPTPreferences.saveSelectedGames(requireContext().applicationContext, selectedGames)
        }

        selectedGames = BPTPreferences.loadSelectedGames(requireContext().applicationContext)

        val response = RetrofitInstance.api.getGameSelection()
        response.enqueue(object: Callback<List<Game>?> {
            override fun onResponse(call: Call<List<Game>?>, response: Response<List<Game>?>) {
                when (response.code()) {
                    // if the return code of the response is 500, a server error message is displayed
                    500 -> {
                        Toast.makeText(activity?.applicationContext, "Server Error. Please try again later.", Toast.LENGTH_LONG).show()
                    }
                    // if the return code of the response is 200, the retrieved games will be displayed
                    200 -> {
                        setSelectGamesBtn.visibility = View.VISIBLE
                        selectGameRecycler.visibility = View.VISIBLE
                        selectGamesProgressBar.visibility = View.GONE

                        val games: List<Game> = response.body()!!.sortedBy { it.title }
                        val layoutManager = LinearLayoutManager(activity)

                        selectGameRecycler.layoutManager = layoutManager
                        selectGameRecycler.adapter = SelectGameAdapter(games, selectedGames)
                    }
                }
            }

            override fun onFailure(call: Call<List<Game>?>, t: Throwable) {
                Toast.makeText(activity?.applicationContext, "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show()
            }
        })
    }
}