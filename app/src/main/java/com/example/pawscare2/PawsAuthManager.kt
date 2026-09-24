package com.example.pawscare2

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Gestor de Autenticación para controlar inicio de sesión y registro en Firebase Auth
class PawsAuthManager {
    private val auth = FirebaseAuth.getInstance()

    // Obtiene el flujo del usuario actual en tiempo real
    fun getCurrentUserFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Obtiene el ID del usuario actual
    fun getUserId(): String? = auth.currentUser?.uid

    // Inicia sesión con correo y contraseña
    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val ex = task.exception
                    val userFriendlyError = when (ex) {
                        is FirebaseAuthInvalidUserException ->
                            "No existe ninguna cuenta registrada con este correo electrónico."
                        is FirebaseAuthInvalidCredentialsException ->
                            "Contraseña o correo electrónico incorrectos."
                        else -> ex?.localizedMessage ?: "Error al iniciar sesión."
                    }
                    onResult(false, userFriendlyError)
                }
            }
    }

    // Registra un nuevo usuario con verificación de duplicados
    fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val ex = task.exception
                    val userFriendlyError = when (ex) {
                        is FirebaseAuthUserCollisionException ->
                            "Este correo electrónico ya está registrado en PawsCare. Inicia sesión o usa otro correo."
                        is FirebaseAuthWeakPasswordException ->
                            "La contraseña debe tener al menos 6 caracteres y ser más segura."
                        is FirebaseAuthInvalidCredentialsException ->
                            "El correo electrónico ingresado no es válido o ha sido rechazado por el servidor."
                        else -> ex?.localizedMessage ?: "Error al registrar la cuenta."
                    }
                    onResult(false, userFriendlyError)
                }
            }
    }

    // Cierra la sesión activa
    fun signOut() {
        auth.signOut()
    }
}
