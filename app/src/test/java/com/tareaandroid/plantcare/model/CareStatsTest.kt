package com.tareaandroid.plantcare.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/** Agrupación del historial de cuidados por mes para el gráfico. */
class CareStatsTest {

    private val today = LocalDate.of(2026, 10, 7)

    private fun event(date: LocalDate, type: CareType) = CareEvent(plantId = 1, type = type, date = date)

    @Test
    fun groupsByMonth_andSeparatesWaterings() {
        val history = listOf(
            event(LocalDate.of(2026, 10, 1), CareType.WATER),
            event(LocalDate.of(2026, 10, 5), CareType.WATER),
            event(LocalDate.of(2026, 10, 6), CareType.PRUNE),
            event(LocalDate.of(2026, 8, 20), CareType.FERTILIZE),
        )

        val stats = monthlyCareStats(history, today, months = 3)

        assertEquals(listOf(YearMonth.of(2026, 8), YearMonth.of(2026, 9), YearMonth.of(2026, 10)), stats.map { it.month })
        assertEquals(MonthlyCare(YearMonth.of(2026, 8), waterings = 0, otherCares = 1), stats[0])
        assertEquals(MonthlyCare(YearMonth.of(2026, 9), waterings = 0, otherCares = 0), stats[1])
        assertEquals(MonthlyCare(YearMonth.of(2026, 10), waterings = 2, otherCares = 1), stats[2])
    }

    @Test
    fun ignoresCaresOlderThanTheRange_andWorksAcrossYears() {
        val history = listOf(
            event(LocalDate.of(2025, 12, 31), CareType.WATER),
            event(LocalDate.of(2025, 1, 1), CareType.WATER),
        )

        val stats = monthlyCareStats(history, LocalDate.of(2026, 1, 15), months = 2)

        assertEquals(listOf(1, 0), stats.map { it.waterings })
    }
}
