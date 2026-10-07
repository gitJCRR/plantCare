package com.tareaandroid.plantcare.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tareaandroid.plantcare.data.local.PlantCareDatabase
import com.tareaandroid.plantcare.data.local.toEntity
import com.tareaandroid.plantcare.data.repository.PlantRepositoryImpl
import com.tareaandroid.plantcare.model.CareType
import com.tareaandroid.plantcare.model.Plant
import com.tareaandroid.plantcare.model.User
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

/** Pruebas del repositorio sobre una base de datos en memoria. */
@RunWith(AndroidJUnit4::class)
class PlantRepositoryTest {

    private val ana = User(uid = "uid-ana", email = "ana@ejemplo.com")
    private val juan = User(uid = "uid-juan", email = "juan@ejemplo.com")

    private lateinit var db: PlantCareDatabase
    private lateinit var auth: FakeAuthRepository
    private lateinit var repository: PlantRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PlantCareDatabase::class.java,
        ).build()
        auth = FakeAuthRepository(ana)
        repository = PlantRepositoryImpl(db, auth)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun eachUser_onlySeesTheirOwnPlants() = runBlocking {
        val anaPlantId = repository.savePlant(Plant(name = "Monstera", waterEveryDays = 7))

        auth.switchTo(juan)
        repository.savePlant(Plant(name = "Cactus", waterEveryDays = 14))

        assertEquals(listOf("Cactus"), repository.observePlants().first().map { it.name })
        assertNull(repository.getPlant(anaPlantId))

        auth.switchTo(ana)
        assertEquals(listOf("Monstera"), repository.observePlants().first().map { it.name })
    }

    @Test
    fun plantsCreatedBeforeLogin_areClaimedByFirstUser() = runBlocking {
        db.plantDao().insert(Plant(name = "Ficus", waterEveryDays = 5).toEntity(userId = "local"))

        val plants = repository.observePlants().first()

        assertEquals(listOf("Ficus"), plants.map { it.name })
    }

    @Test
    fun withoutSession_noPlantsAreVisible() = runBlocking {
        repository.savePlant(Plant(name = "Pothos", waterEveryDays = 3))
        auth.signOut()

        assertTrue(repository.observePlants().first().isEmpty())
    }

    @Test
    fun savePlant_insertsThenUpdates() = runBlocking {
        val id = repository.savePlant(Plant(name = "Monstera", waterEveryDays = 7))
        repository.savePlant(repository.getPlant(id)!!.copy(name = "Monstera deliciosa"))

        val plants = repository.observePlants().first()
        assertEquals(1, plants.size)
        assertEquals("Monstera deliciosa", plants.single().name)
    }

    @Test
    fun registerWatering_updatesPlantAndHistory() = runBlocking {
        val day = LocalDate.of(2026, 10, 7)
        val id = repository.savePlant(Plant(name = "Pothos", waterEveryDays = 3))

        repository.registerCare(id, CareType.WATER, day)

        assertEquals(day, repository.getPlant(id)!!.lastWatered)
        val history = repository.observeCareEvents(id).first()
        assertEquals(listOf(CareType.WATER), history.map { it.type })
    }

    @Test
    fun registerPruning_onlyAddsToHistory() = runBlocking {
        val id = repository.savePlant(Plant(name = "Ficus", waterEveryDays = 5))

        repository.registerCare(id, CareType.PRUNE, LocalDate.of(2026, 10, 7))

        assertEquals(null, repository.getPlant(id)!!.lastWatered)
        assertEquals(1, repository.observeCareEvents(id).first().size)
    }
}
