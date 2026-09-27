package com.example.pawscare2.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetTest {

    @Test
    fun testPetDefaultValues() {
        val pet = Pet()
        assertEquals("", pet.ownerId)
        assertEquals(0L, pet.id)
        assertEquals("", pet.name)
        assertEquals("PERRO", pet.species)
        assertEquals("", pet.breed)
        assertEquals(0, pet.age)
        assertEquals(0.0, pet.weight, 0.01)
        assertEquals("Macho", pet.gender)
        assertEquals("", pet.microchip)
        assertTrue(pet.isVerified)
    }

    @Test
    fun testGetIconEmojiPerro() {
        val pet = Pet(species = "PERRO")
        assertEquals("🐶", pet.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiGato() {
        val petGato = Pet(species = "GATO")
        val petCat = Pet(species = "CAT")
        assertEquals("🐱", petGato.getIconEmoji())
        assertEquals("🐱", petCat.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiAve() {
        val petAve = Pet(species = "AVE")
        val petPajaro = Pet(species = "PÁJARO")
        val petBird = Pet(species = "BIRD")
        assertEquals("🦜", petAve.getIconEmoji())
        assertEquals("🦜", petPajaro.getIconEmoji())
        assertEquals("🦜", petBird.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiConejo() {
        val petConejo = Pet(species = "CONEJO")
        val petRabbit = Pet(species = "RABBIT")
        assertEquals("🐰", petConejo.getIconEmoji())
        assertEquals("🐰", petRabbit.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiHamster() {
        val petHamster = Pet(species = "HAMSTER")
        assertEquals("🐹", petHamster.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiPez() {
        val petPez = Pet(species = "PEZ")
        val petFish = Pet(species = "FISH")
        assertEquals("🐠", petPez.getIconEmoji())
        assertEquals("🐠", petFish.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiReptil() {
        val petReptil = Pet(species = "REPTIL")
        val petReptile = Pet(species = "REPTILE")
        assertEquals("🦎", petReptil.getIconEmoji())
        assertEquals("🦎", petReptile.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiFallback() {
        val petDesconocido = Pet(species = "DESCONOCIDO")
        assertEquals("🐶", petDesconocido.getIconEmoji())
    }
}
