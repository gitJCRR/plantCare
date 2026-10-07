package com.tareaandroid.plantcare.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/** Room no sabe guardar [LocalDate]: se almacena como número de días desde 1970-01-01. */
class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()
}
