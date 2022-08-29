package com.jdu.battlepasstracker.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.adapters.GameAdapter
import com.jdu.battlepasstracker.api.RetrofitInstance
import com.jdu.battlepasstracker.models.Game
import com.jdu.battlepasstracker.utils.BPTPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(R.layout.fragment_home) {
    lateinit var games: List<Game>
    lateinit var sortAutoCompleteTextView: AutoCompleteTextView
    lateinit var gamesRecycler: RecyclerView
    lateinit var homeProgressBar: ProgressBar



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup variables to contain the game recycler view and sort auto complete view
        gamesRecycler = view.findViewById(R.id.gamesRecycler)
        sortAutoCompleteTextView = view.findViewById(R.id.sortAutoCompleteTextView)
        homeProgressBar = view.findViewById(R.id.homeProgressBar)

        // setup an item click listener to allow the user to sort games
        sortAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                sortGames(position)
            }

        // make api request to server to get games to display
        val response = RetrofitInstance.api.getGames(BPTPreferences.getSelectedGamesInJson(requireContext().applicationContext))
        response.enqueue(object: Callback<List<Game>?> {
            override fun onResponse(call: Call<List<Game>?>, response: Response<List<Game>?>) {
                when (response.code()) {
                    // if the return code of the response is 500, a server error message is displayed
                    500 -> {
                        Toast.makeText(activity?.applicationContext, "Server Error. Please try again later.", Toast.LENGTH_LONG).show()
                    }
                    // if the return code of the response is 200, the retrieved games will be displayed
                    200 -> {
                        homeProgressBar.visibility = View.GONE
                        gamesRecycler.visibility = View.VISIBLE
                        sortAutoCompleteTextView.visibility = View.VISIBLE

                        games = response.body()!!.sortedBy { it.season_end_date }
                        val layoutManager = LinearLayoutManager(activity)

                        gamesRecycler.layoutManager = layoutManager
                        gamesRecycler.adapter = GameAdapter(games)
                    }
                }
            }

            override fun onFailure(call: Call<List<Game>?>, t: Throwable) {
                Toast.makeText(activity?.applicationContext, "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show()
            }
        })
    }



    override fun onResume() {
        super.onResume()

        // sets the exposed dropdown menu options to the sort options
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, sortOptions)
        sortAutoCompleteTextView.setAdapter(arrayAdapter)
    }



    // Function:    sortGames()
    // Description: sorts the list of games depending on the sort option passed in
    // Parameters:  sortOption: an Int representing the sort option that games will be sorted by.
    //              The number passed in correlates with the array index of the sort_options string array
    // Return:      N/A
    private fun sortGames(sortOption: Int) {
        when (sortOption) {
            0 -> gamesRecycler.adapter = GameAdapter(games.sortedBy { it.season_end_date })
            1 -> gamesRecycler.adapter = GameAdapter(games.sortedBy { it.season_end_date }.reversed())
            2 -> gamesRecycler.adapter = GameAdapter(games.sortedBy { it.title })
            3 -> gamesRecycler.adapter = GameAdapter(games.sortedBy { it.title }.reversed())
            4 -> gamesRecycler.adapter = GameAdapter(games.sortedBy { it.season_title })
            5 -> gamesRecycler.adapter = GameAdapter(games.sortedBy { it.season_title }.reversed())
            6 -> gamesRecycler.adapter = GameAdapter(games.sortedBy { it.season_start_date })
            7 -> gamesRecycler.adapter = GameAdapter(games.sortedBy { it.season_start_date }.reversed())
        }
    }
}