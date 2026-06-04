package com.example.game

data class FallingBubble(
    var x: Float,
    var y: Float,
    var z: Float = 0f,
    var vx: Float,
    var vy: Float,
    var vz: Float = 0f,
    val color: BubbleColor,
    var alpha: Float = 1.0f,
    var scale: Float = 1.0f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Int,
    var alpha: Float = 1.0f,
    var scale: Float = 1.0f,
    var life: Int,
    val maxLife: Int
)
