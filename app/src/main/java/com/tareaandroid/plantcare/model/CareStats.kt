package com.tareaandroid.plantcare.model

import java.time.LocalDate
import java.time.YearMonth

/** Cuidados de un mes, separando los riegos del resto (abono, poda, trasplante). */
data class MonthlyCare(val month: YearMonth, val waterings: Int, val otherCares: Int) {
    val total: Int get() = waterings + otherCares
}

/**
 * Agrupa el historial en los últimos [months] meses (incluido el actual), del más antiguo
 * al más reciente. Los meses sin cuidados aparecen con 0 para que el gráfico no tenga huecos.
 */
fun monthlyCareStats(history: List<CareEvent>, today: LocalDate, months: Int = 6): List<MonthlyCare> {
    val current = YearMonth.from(today)
    val byMonth = history.groupBy { YearMonth.from(it.date) }
    return (months - 1 downTo 0).map { offset ->
        val month = current.minusMonths(offset.toLong())
        val events = byMonth[month].orEmpty()
        MonthlyCare(
            month = month,
            waterings = events.count { it.type == CareType.WATER },
            otherCares = events.count { it.type != CareType.WATER },
        )
    }
}
