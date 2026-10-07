package com.tareaandroid.plantcare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tareaandroid.plantcare.data.local.dao.CareEventDao
import com.tareaandroid.plantcare.data.local.dao.PlantDao
import com.tareaandroid.plantcare.data.local.entity.CareEventEntity
import com.tareaandroid.plantcare.data.local.entity.PlantEntity

/** Base de datos local de la app. El esquema se exporta a `app/schemas/`. */
@Database(
    entities = [PlantEntity::class, CareEventEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PlantCareDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun careEventDao(): CareEventDao

    companion object {
        const val NAME = "plantcare.db"
    }
}
