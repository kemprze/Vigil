package com.kemprze.vigil.data.model

enum class Duration(val label: String, val maxMinutes: Int) {
    QUICK("Quick", 5),
    SHORT("Short", 15),
    MEDIUM("Medium", 45),
    LONG("Long", 120),
    DEEP("Deep work", Int.MAX_VALUE);

    companion object {
        fun fromMinutes(minutes: Int): Duration {
            return entries.firstOrNull { minutes <= it.maxMinutes } ?: Duration.DEEP
        }
    }
}
