package com.tareaandroid.plantcare.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pruebas de la lógica de riego del modelo [Plant]. */
class PlantTest {

    private val today = LocalDate.of(2026, 10, 10)

    @Test
    fun wateredRecently_doesNotNeedWater() {
        val plant = Plant(name = "Monstera", waterEveryDays = 7, lastWatered = today.minusDays(2))
        assertEquals(5, plant.daysUntilWatering(today))
        assertFalse(plant.needsWater(today))
    }

    @Test
    fun wateringDueToday_needsWater() {
        val plant = Plant(name = "Pothos", waterEveryDays = 3, lastWatered = today.minusDays(3))
        assertEquals(0, plant.daysUntilWatering(today))
        assertTrue(plant.needsWater(today))
    }

    @Test
    fun overdueWatering_isNegative() {
        val plant = Plant(name = "Ficus", waterEveryDays = 2, lastWatered = today.minusDays(5))
        assertEquals(-3, plant.daysUntilWatering(today))
        assertTrue(plant.needsWater(today))
    }

    @Test
    fun neverWatered_needsWater() {
        val plant = Plant(name = "Cactus", waterEveryDays = 14)
        assertNull(plant.nextWatering)
        assertTrue(plant.needsWater(today))
    }

    @Test
    fun nextFertilizing_onlyWhenPlantIsFertilized() {
        val noFertilizer = Plant(name = "Aloe", waterEveryDays = 10, lastFertilized = today)
        assertNull(noFertilizer.nextFertilizing)

        val fertilized = noFertilizer.copy(fertilizeEveryDays = 30)
        assertEquals(today.plusDays(30), fertilized.nextFertilizing)
    }
}
