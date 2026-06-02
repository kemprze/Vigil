package com.kemprze.vigil.data.model

import androidx.annotation.StringRes
import com.kemprze.vigil.R

enum class Duration(val label: String, val maxMinutes: Int, @StringRes val emojiRes: Int) {
    QUICK("Quick", 5, R.string.duration_quick),
    SHORT("Short", 15, R.string.duration_short),
    MEDIUM("Medium", 45, R.string.duration_medium),
    LONG("Long", 120, R.string.duration_long),
    DEEP("Deep work", Int.MAX_VALUE, R.string.duration_deep_work);

    companion object {
        fun fromMinutes(minutes: Int): Duration {
            return entries.firstOrNull { minutes <= it.maxMinutes } ?: Duration.DEEP
        }
    }
}
