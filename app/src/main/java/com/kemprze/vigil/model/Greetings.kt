package com.kemprze.vigil.model

import java.time.LocalTime

object Greetings {
    private val morning = listOf(
        "Morning, %s",
        "Rise and shine, %s",
        "Fresh start, %s",
        "Early bird, %s",
        "Good morning, %s",
        "New day, %s",
        "Coffee time, %s?",
        "Let's go, %s",
        "Morning energy, %s",
        "Starting strong, %s"
    )

    private val afternoon = listOf(
        "Afternoon, %s",
        "Still going, %s",
        "How's the day, %s?",
        "Midday check-in, %s",
        "Keep it up, %s",
        "You're doing great, %s",
        "Halfway there, %s",
        "Afternoon boost, %s",
        "Don't stop now, %s",
        "Good afternoon, %s"
    )

    private val evening = listOf(
        "Evening, %s",
        "Winding down, %s?",
        "Almost there, %s",
        "End of day, %s",
        "Good evening, %s",
        "What's left, %s?",
        "Final stretch, %s",
        "Evening check-in, %s",
        "Nearly done, %s",
        "Closing time, %s"
    )

    private val night = listOf(
        "Late night, %s",
        "Burning the midnight oil, %s",
        "Night owl mode, %s",
        "Couldn't sleep, %s?",
        "Still at it, %s",
        "The night is yours, %s",
        "Quiet hours, %s",
        "Late shift, %s",
        "Just you and the dark, %s",
        "Night mode, %s"
    )

    fun forHour(name: String, hour: Int = LocalTime.now().hour): String {
        val template = when (hour) {
            in 5..11 -> morning
            in 12..17 -> afternoon
            in 18..21 -> evening
            else -> night
        }.random()
        return template.format(name)
    }
}