package com.tareaandroid.plantcare.model

import java.time.LocalDate

/** Un cuidado realizado a una planta (riego, abono, poda o trasplante). */
data class CareEvent(
    val id: Long = 0,
    val plantId: Long,
    val type: CareType,
    val date: LocalDate,
    val note: String = "",
)
