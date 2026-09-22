package com.example.pawscare2

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Gestor de Autenticación para manejar el inicio de sesión con Firebase.
 */
class PawsAuthManager {
    private val auth = FirebaseAuth.getInstance()

    /**
     * Obtiene el estado del usuario actual (si está logueado o no).
     */
    fun getCurrentUserFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Obtiene el ID del usuario actual.
     */
    fun getUserId(): String? = auth.currentUser?.uid

    /**
     * Inicia sesión con correo y contraseña.
     */
    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    /**
     * Registra un nuevo usuario con correo y contraseña.
     */
    fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    /**
     * Cierra la sesión del usuario.
     */
    fun signOut() {
        auth.signOut()
    }
}
