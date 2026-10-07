package com.tareaandroid.plantcare.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tareaandroid.plantcare.data.local.PlantCareDatabase
import com.tareaandroid.plantcare.data.local.entity.CareEventEntity
import com.tareaandroid.plantcare.data.local.entity.PlantEntity
import com.tareaandroid.plantcare.model.CareType
import com.tareaandroid.plantcare.model.LightLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Pruebas de los DAO sobre una base de datos en memoria. */
@RunWith(AndroidJUnit4::class)
class PlantDaoTest {

    private lateinit var db: PlantCareDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PlantCareDatabase::class.java,
        ).build()
    }

    @After
    fun closeDb() = db.close()

    private fun plant(name: String, userId: String = "user") = PlantEntity(
        userId = userId,
        name = name,
        species = "",
        location = "",
        photoUri = null,
        waterEveryDays = 7,
        lastWatered = LocalDate.of(2026, 10, 1),
        fertilizeEveryDays = null,
        lastFertilized = null,
        lightLevel = LightLevel.MEDIUM,
        notes = "",
    )

    @Test
    fun insertAndReadPlant_keepsDatesAndEnums() = runBlocking {
        val id = db.plantDao().insert(plant("Monstera"))

        val stored = db.plantDao().getPlant(id)!!
        assertEquals("Monstera", stored.name)
        assertEquals(LocalDate.of(2026, 10, 1), stored.lastWatered)
        assertEquals(LightLevel.MEDIUM, stored.lightLevel)
    }

    @Test
    fun observePlants_filtersByUserAndSortsByName() = runBlocking {
        db.plantDao().insert(plant("cactus"))
        db.plantDao().insert(plant("Aloe"))
        db.plantDao().insert(plant("Ficus", userId = "other"))

        val names = db.plantDao().observePlants("user").first().map { it.name }
        assertEquals(listOf("Aloe", "cactus"), names)
    }

    @Test
    fun deletingPlant_cascadesToCareEvents() = runBlocking {
        val id = db.plantDao().insert(plant("Pothos"))
        db.careEventDao().insert(CareEventEntity(plantId = id, type = CareType.WATER, date = LocalDate.now()))
        assertEquals(1, db.careEventDao().observeEvents(id).first().size)

        db.plantDao().deleteById(id)

        assertNull(db.plantDao().getPlant(id))
        assertTrue(db.careEventDao().observeEvents(id).first().isEmpty())
    }
}
