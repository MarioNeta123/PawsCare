package com.example.pawscare2

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PawsViewModelValidationTest {

    private fun validatePetData(species: String, name: String, breed: String, age: Int, weight: Double): Boolean {
        val cleanName = name.trim()
        val cleanBreed = breed.trim()
        val cleanSpecies = species.uppercase().trim().ifBlank { "PERRO" }

        if (cleanName.length < 2 || cleanBreed.isEmpty()) return false

        return when (cleanSpecies) {
            "GATO" -> age in 0..25 && weight in 0.2..15.0
            "AVE", "PAJARO" -> age in 0..50 && weight in 0.01..5.0
            "CONEJO", "HAMSTER" -> age in 0..12 && weight in 0.01..8.0
            else -> age in 0..30 && weight in 0.2..120.0
        }
    }

    private fun validatePhone(phone: String): Boolean {
        val clean = phone.trim()
        return clean.isEmpty() || clean.matches(Regex("^[0-9]{7,15}$"))
    }

    private fun validateAddress(address: String): Boolean {
        val clean = address.trim()
        return clean.isEmpty() || clean.length >= 4
    }

    @Test
    fun testValidDogData() {
        assertTrue(validatePetData("PERRO", "Monchito", "Golden Retriever", 3, 28.5))
        assertTrue(validatePetData("PERRO", "Rocky", "Bulldog", 1, 11.0))
    }

    @Test
    fun testInvalidDogData() {
        assertFalse(validatePetData("PERRO", "M", "Golden Retriever", 3, 28.5)) // Nombre corto
        assertFalse(validatePetData("PERRO", "Monchito", "", 3, 28.5)) // Raza vacia
        assertFalse(validatePetData("PERRO", "Monchito", "Golden Retriever", 35, 28.5)) // Edad excesiva
        assertFalse(validatePetData("PERRO", "Monchito", "Golden Retriever", 3, 150.0)) // Peso excesivo
    }

    @Test
    fun testCatWeightValidation() {
        assertTrue(validatePetData("GATO", "Luna", "Siamés", 2, 4.2))
        assertFalse(validatePetData("GATO", "Luna", "Siamés", 2, 20.0)) // Gato no puede pesar 20kg
    }

    @Test
    fun testBirdWeightValidation() {
        assertTrue(validatePetData("AVE", "Paco", "Loro", 5, 0.4))
        assertFalse(validatePetData("AVE", "Paco", "Loro", 5, 10.0)) // Ave no puede pesar 10kg
    }

    @Test
    fun testPhoneValidation() {
        assertTrue(validatePhone("5512345678"))
        assertTrue(validatePhone(""))
        assertFalse(validatePhone("123")) // Demasiado corto
        assertFalse(validatePhone("ABC55123456")) // Contiene letras
    }

    @Test
    fun testAddressValidation() {
        assertTrue(validateAddress("Av. Insurgentes 123"))
        assertTrue(validateAddress(""))
        assertFalse(validateAddress("Av.")) // Demasiado corto
    }
}
