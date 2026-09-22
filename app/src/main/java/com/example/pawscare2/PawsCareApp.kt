package com.example.pawscare2

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Clase Application personalizada para inicializar Firebase al arrancar la app.
 */
class PawsCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializamos Firebase
        FirebaseApp.initializeApp(this)
    }
}
