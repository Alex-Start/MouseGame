package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.GameEngine
import com.example.model.GameSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Mouse Game", appName)
  }

  @Test
  fun `test mouse custom renaming in game settings and engine`() {
    val settings = GameSettings(
      playerCount = 4,
      customNames = mapOf(1 to "Speedy", 2 to "Jerry")
    )
    assertEquals("Speedy", settings.getPlayerName(1))
    assertEquals("Jerry", settings.getPlayerName(2))
    assertEquals("Player 3", settings.getPlayerName(3))

    val engine = GameEngine(settings)
    assertEquals("Speedy", engine.players[0].name)
    assertEquals("Jerry", engine.players[1].name)
    assertEquals("Player 3", engine.players[2].name)

    engine.renamePlayer(3, "Mighty")
    assertEquals("Mighty", engine.players[2].name)
  }

  @Test
  fun `test cat laser advance and strike`() {
    val settings = GameSettings(
      laserMode = com.example.model.LaserMode.KILL,
      winningScore = 10,
      fastCat = false
    )
    val engine = GameEngine(settings)
    val maxTime = engine.getEffectiveLaserTimeMax()

    // Advance laser through one full cycle so laserLineNumber becomes 1
    for (i in 0..maxTime) {
      engine.advanceLaserTime()
    }
    assertEquals(1, engine.laserLineNumber)

    // Advance to strike time of the new cycle
    for (i in 1..maxTime) {
      engine.advanceLaserTime()
    }
    assertEquals(maxTime, engine.laserLineTime)
    val isStrike = engine.laserLineTime == engine.getEffectiveLaserTimeMax() && engine.laserLineNumber > 0
    assertTrue(isStrike)
  }
}

