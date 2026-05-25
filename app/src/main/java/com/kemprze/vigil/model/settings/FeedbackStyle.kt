package com.kemprze.vigil.model.settings

import androidx.annotation.StringRes
import com.kemprze.vigil.R

enum class FeedbackStyle(@StringRes val emojiRes: Int, @StringRes val labelRes: Int) {
    ENCOURAGING(R.string.feedback_encouraging_emoji, R.string.feedback_encouraging_label),
    DIRECT(R.string.feedback_direct_emoji, R.string.feedback_direct_label),
    TOUGH(R.string.feedback_tough_emoji, R.string.feedback_tough_label)
}