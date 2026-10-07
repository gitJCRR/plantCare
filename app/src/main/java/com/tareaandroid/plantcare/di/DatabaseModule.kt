package com.tareaandroid.plantcare.di

import android.content.Context
import androidx.room.Room
import com.tareaandroid.plantcare.data.local.PlantCareDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Indica a Hilt cómo crear la base de datos: una única instancia para toda la app. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlantCareDatabase =
        Room.databaseBuilder(context, PlantCareDatabase::class.java, PlantCareDatabase.NAME)
            .build()
}
