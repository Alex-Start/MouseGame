package com.example.model

data class Player(
    val id: Int,
    val name: String,
    val score: Int = 0,
    val isAlive: Boolean = true
)

