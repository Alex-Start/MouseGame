package com.example.model

data class Player(
    val id: Int,
    val name: String,
    val score: Int = 0,
    val isAlive: Boolean = true,
    val keyPosition: Int? = null,
    val hasKey: Boolean = false,
    val isKeyCollected: Boolean = false,
    val isCaged: Boolean = false,
    val cheesePosition: Int? = null,
    val isCheeseCollected: Boolean = false,
    val trapPosition: Int? = null,
    val isTrapTriggered: Boolean = false,
    val stunnedTurns: Int = 0
)

