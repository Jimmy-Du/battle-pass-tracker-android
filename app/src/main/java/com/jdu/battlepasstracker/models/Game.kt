package com.jdu.battlepasstracker.models

import java.util.*

data class Game(
    val id: Int,
    val title: String,
    val season_title: String,
    val season_start_date: Date,
    val season_end_date: Date
)
