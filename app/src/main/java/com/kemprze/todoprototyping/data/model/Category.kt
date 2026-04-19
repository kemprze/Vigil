package com.kemprze.todoprototyping.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.kemprze.todoprototyping.R

enum class Category(@StringRes val categoryNameRes: Int,
        @StringRes val categoryImageRes: Int,
    val color: Color) {
    NONE(categoryNameRes = R.string.category_none, categoryImageRes = R.string.icon_none, color = Color.Transparent),
    WORK(categoryNameRes = R.string.category_work, categoryImageRes = R.string.icon_work, color = Color(0xFF5B8DEF)),
    PERSONAL(categoryNameRes = R.string.category_personal, categoryImageRes = R.string.icon_personal, color = Color(0xFFE07B5B)),
    SHOPPING(categoryNameRes = R.string.category_shopping, categoryImageRes = R.string.icon_shopping, color = Color(0xFFE0C67E)),
    HEALTH(categoryNameRes = R.string.category_health, categoryImageRes = R.string.icon_health, color = Color(0xFFE05B5B)),
    HOME(categoryNameRes = R.string.category_home, categoryImageRes = R.string.icon_home, color = Color(0xFFB07BE0)),
    EDUCATION(categoryNameRes = R.string.category_education, categoryImageRes = R.string.icon_education, color = Color(0xFF5BC5E0)),
    FINANCE(categoryNameRes = R.string.category_finance, categoryImageRes = R.string.icon_finance, color = Color(0xFFE0C45B)),
    OTHER(categoryNameRes = R.string.category_other, categoryImageRes = R.string.icon_other, color = Color(0xFF9E9E9E))
}