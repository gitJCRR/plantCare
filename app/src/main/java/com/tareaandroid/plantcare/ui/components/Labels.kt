package com.tareaandroid.plantcare.ui.components

import androidx.annotation.StringRes
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.model.CareType
import com.tareaandroid.plantcare.model.LightLevel

/** Textos visibles de los enums del modelo (el modelo no depende de los recursos de Android). */

@get:StringRes
val LightLevel.labelRes: Int
    get() = when (this) {
        LightLevel.LOW -> R.string.light_low
        LightLevel.MEDIUM -> R.string.light_medium
        LightLevel.HIGH -> R.string.light_high
    }

@get:StringRes
val CareType.labelRes: Int
    get() = when (this) {
        CareType.WATER -> R.string.care_water
        CareType.FERTILIZE -> R.string.care_fertilize
        CareType.PRUNE -> R.string.care_prune
        CareType.REPOT -> R.string.care_repot
    }
