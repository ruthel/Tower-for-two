package com.crabscode.towerfortwo

import com.crabscode.towerfortwo.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Test

class GameStateTest {
    @Test
    fun initialStateTargetsLevelOneAction() {
        val game = GameState(blocksPlaced = 0)
        assertEquals(1, game.nextLevel)
        assertEquals(1, game.nextSlot)
    }

    @Test
    fun thirdBlockCompletesLevelOneAndNextTargetsLevelTwo() {
        val game = GameState(blocksPlaced = 3)
        assertEquals(2, game.nextLevel)
        assertEquals(1, game.nextSlot)
        assertEquals(1, game.reachedLevel)
    }

    @Test
    fun thirtiethBlockReachesLevelTen() {
        val game = GameState(blocksPlaced = 30)
        assertEquals(10, game.reachedLevel)
        assertEquals(10, game.nextLevel)
    }
}