package com.jdu.battlepasstracker.models

import java.util.*

data class Event(
    val id: Int,
    val game_id: Int,
    val title: String,
    val event_title: String,
    val event_start_date: Date,
    val event_end_date: Date
)
