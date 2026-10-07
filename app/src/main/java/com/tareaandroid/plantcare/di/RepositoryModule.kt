package com.tareaandroid.plantcare.di

import com.tareaandroid.plantcare.data.auth.AuthRepository
import com.tareaandroid.plantcare.data.auth.FirebaseAuthRepository
import com.tareaandroid.plantcare.data.repository.PlantRepository
import com.tareaandroid.plantcare.data.repository.PlantRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Asocia cada interfaz de repositorio con su implementación. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindPlantRepository(impl: PlantRepositoryImpl): PlantRepository

    @Binds
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository
}
