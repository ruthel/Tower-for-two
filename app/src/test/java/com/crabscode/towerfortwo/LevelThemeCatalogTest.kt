package com.crabscode.towerfortwo

import com.crabscode.towerfortwo.model.LevelThemeCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class LevelThemeCatalogTest {
    @Test
    fun everyLevelHasItsOwnTheme() {
        val titles = (1..10).map { LevelThemeCatalog.forLevel(it).title }
        assertEquals(10, titles.toSet().size)
        assertEquals("Briser la glace", LevelThemeCatalog.forLevel(1).title)
        assertEquals("Final", LevelThemeCatalog.forLevel(10).title)
    }

    @Test
    fun themeLookupIsClamped() {
        assertEquals(1, LevelThemeCatalog.forLevel(0).level)
        assertEquals(10, LevelThemeCatalog.forLevel(99).level)
    }
}
