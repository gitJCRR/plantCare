package com.tareaandroid.plantcare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Punto de entrada de la aplicación: inicializa el grafo de dependencias de Hilt. */
@HiltAndroidApp
class PlantCareApp : Application()
