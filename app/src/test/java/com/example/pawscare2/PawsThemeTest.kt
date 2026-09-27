package com.example.pawscare2

import org.junit.Assert.assertEquals
import org.junit.Test

class PawsThemeTest {

    @Test
    fun testPawsThemeValues() {
        val ocean = PawsTheme.OCEAN
        val forest = PawsTheme.FOREST
        val sunset = PawsTheme.SUNSET

        assertEquals(R.color.dark_blue, ocean.primary)
        assertEquals(R.color.white, ocean.background)
        assertEquals(R.color.pink, ocean.accent)
        assertEquals(R.color.light_pink, ocean.accentBg)

        assertEquals(R.color.dark_green, forest.primary)
        assertEquals(R.color.dark_yellow, forest.accent)

        assertEquals(R.color.cherry_red, sunset.primary)
        assertEquals(R.color.dark_blue, sunset.accent)
    }
}
