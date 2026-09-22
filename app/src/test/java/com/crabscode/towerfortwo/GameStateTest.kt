package com.crabscode.towerfortwo

import com.crabscode.towerfortwo.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateTest {
    @Test
    fun initialStateTargetsLevelOneAction() {
        val game = GameState()
        assertEquals(1, game.nextLevel)
        assertEquals(1, game.nextSlot)
        assertEquals(0, game.reachedLevel)
        assertFalse(game.canAdvanceFloor)
    }

    @Test
    fun arbitraryPlacedPositionsKeepRealBlockCount() {
        val game = GameState(
            blocksPlaced = 2,
            targetLevel = 2,
            targetSlot = 1,
            placedPositions = setOf("1:1", "1:3"),
        )
        assertEquals(2, game.blocksPlaced)
        assertEquals(1, game.reachedLevel)
        assertTrue(game.isPlaced(1, 3))
        assertFalse(game.isPlaced(1, 2))
        assertFalse(game.canAdvanceFloor)
    }

    @Test
    fun targetCanPointToLaterBlockWithoutFakingProgress() {
        val game = GameState(
            blocksPlaced = 1,
            targetLevel = 1,
            targetSlot = 3,
            placedPositions = setOf("1:1"),
        )
        assertEquals(1, game.nextLevel)
        assertEquals(3, game.nextSlot)
        assertEquals(1, game.reachedLevel)
        assertTrue(game.canAdvanceFloor)
    }

    @Test
    fun cannotAdvanceFromEmptyCurrentFloorEvenIfEarlierFloorHasBlocks() {
        val game = GameState(
            blocksPlaced = 1,
            targetLevel = 2,
            targetSlot = 1,
            placedPositions = setOf("1:1"),
        )
        assertFalse(game.canAdvanceFloor)
        assertFalse(game.hasPlacedOnLevel(2))
    }

    @Test
    fun oneBlockOnCurrentFloorUnlocksNextFloor() {
        val game = GameState(
            blocksPlaced = 2,
            targetLevel = 2,
            targetSlot = 3,
            placedPositions = setOf("1:1", "2:2"),
        )
        assertTrue(game.hasPlacedOnLevel(2))
        assertTrue(game.canAdvanceFloor)
    }

    @Test
    fun finishedGameChecksAllLevels() {
        val game = GameState(
            targetLevel = 10,
            targetSlot = 3,
            isFinished = true,
            placedPositions = setOf("10:3"),
            blocksPlaced = 1,
        )
        assertEquals(10, game.completedLevelCount)
        assertEquals(10, game.reachedLevel)
        assertFalse(game.canAdvanceFloor)
    }
}
