package com.tareaandroid.plantcare.di

import com.tareaandroid.plantcare.data.sensor.AndroidLightSensor
import com.tareaandroid.plantcare.data.sensor.LightSensor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorModule {

    @Binds
    abstract fun bindLightSensor(impl: AndroidLightSensor): LightSensor
}
