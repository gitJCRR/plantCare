package com.tareaandroid.plantcare.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/** Cálculo del retraso inicial del recordatorio diario. */
class ReminderSchedulerTest {

    private val nineAm = LocalTime.of(9, 0)

    @Test
    fun beforeReminderTime_waitsUntilToday() {
        val now = LocalDateTime.of(2026, 10, 7, 7, 30)
        assertEquals(Duration.ofMinutes(90), ReminderScheduler.delayUntil(nineAm, now))
    }

    @Test
    fun afterReminderTime_waitsUntilTomorrow() {
        val now = LocalDateTime.of(2026, 10, 7, 18, 0)
        assertEquals(Duration.ofHours(15), ReminderScheduler.delayUntil(nineAm, now))
    }

    @Test
    fun exactlyAtReminderTime_schedulesNextDay() {
        val now = LocalDateTime.of(2026, 10, 7, 9, 0)
        assertEquals(Duration.ofDays(1), ReminderScheduler.delayUntil(nineAm, now))
    }
}
