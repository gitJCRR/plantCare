package com.tareaandroid.plantcare.data.local

import com.tareaandroid.plantcare.data.local.entity.CareEventEntity
import com.tareaandroid.plantcare.data.local.entity.PlantEntity
import com.tareaandroid.plantcare.model.CareEvent
import com.tareaandroid.plantcare.model.Plant

/** Conversiones entre las entidades de Room y los modelos de dominio. */

fun PlantEntity.toModel() = Plant(
    id = id,
    name = name,
    species = species,
    location = location,
    photoUri = photoUri,
    waterEveryDays = waterEveryDays,
    lastWatered = lastWatered,
    fertilizeEveryDays = fertilizeEveryDays,
    lastFertilized = lastFertilized,
    lightLevel = lightLevel,
    notes = notes,
)

fun Plant.toEntity(userId: String) = PlantEntity(
    id = id,
    userId = userId,
    name = name,
    species = species,
    location = location,
    photoUri = photoUri,
    waterEveryDays = waterEveryDays,
    lastWatered = lastWatered,
    fertilizeEveryDays = fertilizeEveryDays,
    lastFertilized = lastFertilized,
    lightLevel = lightLevel,
    notes = notes,
)

fun CareEventEntity.toModel() = CareEvent(
    id = id,
    plantId = plantId,
    type = type,
    date = date,
    note = note,
)
