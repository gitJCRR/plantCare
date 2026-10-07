package com.tareaandroid.plantcare.model

import com.tareaandroid.plantcare.ui.light.LightFit
import com.tareaandroid.plantcare.ui.light.LightMeterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Clasificación de lecturas del sensor y comparación con la luz que necesita cada planta. */
class LightLevelTest {

    @Test
    fun luxReadings_areClassifiedByRange() {
        assertNull(LightLevel.fromLux(10f))
        assertEquals(LightLevel.LOW, LightLevel.fromLux(300f))
        assertEquals(LightLevel.MEDIUM, LightLevel.fromLux(1_000f))
        assertEquals(LightLevel.MEDIUM, LightLevel.fromLux(9_999f))
        assertEquals(LightLevel.HIGH, LightLevel.fromLux(25_000f))
    }

    @Test
    fun plantFit_comparesNeededAndMeasuredLight() {
        assertEquals(LightFit.GOOD, LightMeterViewModel.fitFor(LightLevel.MEDIUM, LightLevel.MEDIUM))
        assertEquals(LightFit.NEEDS_MORE, LightMeterViewModel.fitFor(LightLevel.HIGH, LightLevel.MEDIUM))
        assertEquals(LightFit.NEEDS_LESS, LightMeterViewModel.fitFor(LightLevel.LOW, LightLevel.HIGH))
        assertEquals(LightFit.NEEDS_MORE, LightMeterViewModel.fitFor(LightLevel.LOW, null))
    }
}
