package com.jdu.battlepasstracker.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jdu.battlepasstracker.R
import com.jdu.battlepasstracker.adapters.EventAdapter
import com.jdu.battlepasstracker.api.RetrofitInstance
import com.jdu.battlepasstracker.models.Event
import com.jdu.battlepasstracker.utils.BPTPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsFragment :  Fragment(R.layout.fragment_events) {
    lateinit var events: List<Event>
    lateinit var eventsProgressBar: ProgressBar
    lateinit var eventsRecycler: RecyclerView
    lateinit var eventSortAutoCompleteTextView: AutoCompleteTextView



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventsProgressBar = view.findViewById(R.id.eventsProgressBar)
        eventsRecycler = view.findViewById(R.id.eventsRecycler)
        eventSortAutoCompleteTextView = view.findViewById(R.id.eventSortAutoCompleteTextView)
        eventSortAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                sortEvents(parent.getItemAtPosition(position) as String)
            }

        // makes request to get events for games
        val response = RetrofitInstance.api.getEvents(BPTPreferences.getSelectedGamesInJson(requireContext().applicationContext))
        response.enqueue(object: Callback<List<Event>?> {
            override fun onResponse(call: Call<List<Event>?>, response: Response<List<Event>?>) {
                when (response.code()) {
                    // if the return code of the response is 500, a server error message is displayed
                    500 -> {
                        Toast.makeText(activity?.applicationContext, "Server Error. Please try again later.", Toast.LENGTH_LONG).show()
                    }
                    // if the return code of the response is 200, the retrieved game events will be displayed
                    200 -> {
                        eventsProgressBar.visibility = View.GONE
                        eventSortAutoCompleteTextView.visibility = View.VISIBLE
                        eventsRecycler.visibility = View.VISIBLE

                        events = response.body()!!.sortedBy { it.event_end_date }
                        val layoutManager = LinearLayoutManager(activity)

                        eventSortAutoCompleteTextView.setAdapter(createEventSortAdapter())

                        eventsRecycler.layoutManager = layoutManager
                        eventsRecycler.adapter = EventAdapter(events)
                    }
                }
            }

            override fun onFailure(call: Call<List<Event>?>, t: Throwable) {
                Toast.makeText(activity?.applicationContext, "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show()
            }
        })
    }



    override fun onResume() {
        super.onResume()

        eventSortAutoCompleteTextView.setText(getString(R.string.event_sort_default_option), false)
    }



    // Function:    createEventSortAdapter()
    // Description: creates an array adapter containing the games that the user
    //              has selected that have events ongoing
    // Parameters:  N/A
    // Return:      An array adapter containing all the sort options for events
    private fun createEventSortAdapter(): ArrayAdapter<String> {
        val eventGames: MutableList<String> = mutableListOf("All Games")

        // for loop to go through each unique game that has events, and add it to
        // an array to be used as sort options
        for (event in events.distinctBy { it.title }) {
            eventGames.add(event.title)
        }

        return ArrayAdapter<String>(requireContext(), R.layout.dropdown_item, eventGames)
    }



    // Function:    sortEvents()
    // Description: sorts the events based off the game that is selected
    // Parameters:  N/A
    // Return:      N/A
    private fun sortEvents(gameToSortBy: String) {
        // if the user selects "All Games", all game events will be shown
        if (gameToSortBy == "All Games") {
            eventsRecycler.adapter = EventAdapter(events)
        }
        // else, only the selected game events are shown
        else {
            eventsRecycler.adapter = EventAdapter(events.filter { it.title == gameToSortBy })
        }
    }
}