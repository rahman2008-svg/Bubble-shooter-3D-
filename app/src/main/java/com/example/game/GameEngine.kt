package com.example.game

import android.graphics.Color
import android.graphics.PointF
import java.util.LinkedList
import java.util.Queue
import kotlin.math.*
import kotlin.random.Random

class GameEngine {

    companion object {
        const val VIRTUAL_WIDTH = 1000f
        const val VIRTUAL_HEIGHT = 1600f
        const val GRID_ROWS = 15
        const val GRID_COLS = 8
        const val BASE_BUBBLE_RADIUS = 50f
        const val LAUNCH_SPEED = 1800f // pixels per second
        const val GRAVITY = 1500f // pixels per second squared
    }

    // Game state enum
    enum class State { MENU, RUNNING, PAUSED, VICTORY, GAME_OVER }

    var state = State.MENU
    var score = 0
    var level = 1
    var isHapticEnabled = true
    var isSoundEnabled = true

    // Grid layout properties
    val bubbleRadius = BASE_BUBBLE_RADIUS
    val rowSpacing = bubbleRadius * sqrt(3f) // Tight honeycomb vertical spacing
    val gridOffsetY = 100f

    // Grid representing bubbles. grid[row][col]
    val grid: Array<Array<Bubble?>> = Array(GRID_ROWS) { Array(GRID_COLS) { null } }

    // Active shoot bubble
    var activeShotBubble: Bubble? = null
    var activeVx = 0f
    var activeVy = 0f

    // Queue of bubbles for the launcher
    var nextLauncherColor: BubbleColor = BubbleColor.getRandomColor()
    var currentLauncherColor: BubbleColor = BubbleColor.getRandomColor()

    // Aim systems
    var aimAngle = -Math.PI.toFloat() / 2f // Angle in radians (default: straight up)
    val launcherX = VIRTUAL_WIDTH / 2f
    val launcherY = VIRTUAL_HEIGHT - 180f

    // Active visual effects
    val fallingBubbles = mutableListOf<FallingBubble>()
    val particles = mutableListOf<Particle>()

    // High score listener
    var onScoreSavedListener: ((Int, Int) -> Unit)? = null

    init {
        resetGame()
    }

    fun resetGame() {
        score = 0
        level = 1
        clearGrid()
        generateLevelLayout()
        resetLauncher()
        fallingBubbles.clear()
        particles.clear()
        state = State.RUNNING
    }

    fun nextLevel() {
        level++
        clearGrid()
        generateLevelLayout()
        resetLauncher()
        fallingBubbles.clear()
        particles.clear()
        state = State.RUNNING
    }

    private fun clearGrid() {
        for (r in 0 until GRID_ROWS) {
            for (c in 0 until GRID_COLS) {
                grid[r][c] = null
            }
        }
    }

    private fun resetLauncher() {
        currentLauncherColor = nextLauncherColor
        nextLauncherColor = BubbleColor.getRandomColor(minOf(3 + level, BubbleColor.values().size))
        activeShotBubble = null
        activeVx = 0f
        activeVy = 0f
    }

    // Generate puzzle layouts based on level
    fun generateLevelLayout() {
        val colorsCount = minOf(3 + level, BubbleColor.values().size)
        // Levels populate more or fewer rows
        val numRows = minOf(4 + level, 9)
        for (r in 0 until numRows) {
            val colsInRow = if (r % 2 == 0) GRID_COLS else GRID_COLS - 1
            for (c in 0 until colsInRow) {
                // Keep some random slots empty for cooler patterns on higher levels
                if (level > 1 && r > 2 && (0..10).random() > 8) {
                    continue
                }
                val color = BubbleColor.getRandomColor(colorsCount)
                val pos = getBubbleCenter(r, c)
                grid[r][c] = Bubble(
                    row = r,
                    col = c,
                    x = pos.x,
                    y = pos.y,
                    color = color
                )
            }
        }
    }

    // Honeycomb coordinates math
    fun getBubbleCenter(row: Int, col: Int): PointF {
        val staggerX = if (row % 2 == 0) 0f else bubbleRadius
        val startX = (VIRTUAL_WIDTH - (GRID_COLS * 2 * bubbleRadius)) / 2f + bubbleRadius
        val x = startX + col * 2 * bubbleRadius + staggerX
        val y = gridOffsetY + row * rowSpacing
        return PointF(x, y)
    }

    fun setAimTarget(touchX: Float, touchY: Float) {
        val dx = touchX - launcherX
        val dy = touchY - launcherY
        // Only allow aiming upwards
        if (dy < -20f) {
            aimAngle = atan2(dy, dx)
            // Constrain aim angle to not aim completely flat sideways (keep inside screen)
            val minAngle = -Math.PI.toFloat() + 0.15f
            val maxAngle = -0.15f
            aimAngle = aimAngle.coerceIn(minAngle, maxAngle)
        }
    }

    fun fireActiveBubble() {
        if (activeShotBubble != null || state != State.RUNNING) return

        activeShotBubble = Bubble(
            x = launcherX,
            y = launcherY,
            color = currentLauncherColor
        )
        activeVx = cos(aimAngle) * LAUNCH_SPEED
        activeVy = sin(aimAngle) * LAUNCH_SPEED

        // Prime the next bubble colors
        currentLauncherColor = nextLauncherColor
        nextLauncherColor = BubbleColor.getRandomColor(minOf(3 + level, BubbleColor.values().size))
    }

    // Calculate bouncing aim trajectory path for the HUD display
    fun calculateAimTrajectory(): List<PointF> {
        val points = mutableListOf<PointF>()
        var curX = launcherX
        var curY = launcherY
        var vx = cos(aimAngle)
        var vy = sin(aimAngle)

        val leftBoundary = bubbleRadius
        val rightBoundary = VIRTUAL_WIDTH - bubbleRadius

        points.add(PointF(curX, curY))

        // Step trajectory forward simulating bounces
        val stepSize = 15f
        var distanceTraveled = 0f
        val maxDistance = 1400f

        while (distanceTraveled < maxDistance && curY > gridOffsetY - 50f) {
            curX += vx * stepSize
            curY += vy * stepSize
            distanceTraveled += stepSize

            // Wall collision bounces
            if (curX <= leftBoundary) {
                curX = leftBoundary
                vx = -vx
                points.add(PointF(curX, curY))
            } else if (curX >= rightBoundary) {
                curX = rightBoundary
                vx = -vx
                points.add(PointF(curX, curY))
            }

            // Standard interval capture
            if (distanceTraveled % 60f < stepSize) {
                points.add(PointF(curX, curY))
            }

            // Check if trajectory point intersects any bubbles on grid, if so, end path early
            var intersected = false
            for (r in 0 until GRID_ROWS) {
                for (c in 0 until GRID_COLS) {
                    val gBubble = grid[r][c] ?: continue
                    val dx = curX - gBubble.x
                    val dy = curY - gBubble.y
                    val distSq = dx * dx + dy * dy
                    if (distSq < (bubbleRadius * 2) * (bubbleRadius * 2)) {
                        intersected = true
                        points.add(PointF(curX, curY))
                        break
                    }
                }
                if (intersected) break
            }
            if (intersected) break
        }

        return points
    }

    // Engine main-loop updates
    fun update(dt: Float) {
        if (state != State.RUNNING) return

        updateActiveBubble(dt)
        updatePoppingBubbles(dt)
        updateFallingBubbles(dt)
        updateParticles(dt)
    }

    private fun updateActiveBubble(dt: Float) {
        val shot = activeShotBubble ?: return

        // Save previous coordinates for precise snapping interpolation
        val prevX = shot.x
        val prevY = shot.y

        // Move bubble
        shot.x += activeVx * dt
        shot.y += activeVy * dt

        // Corner wall bounces
        val leftBoundary = bubbleRadius
        val rightBoundary = VIRTUAL_WIDTH - bubbleRadius

        if (shot.x <= leftBoundary) {
            shot.x = leftBoundary
            activeVx = -activeVx
        } else if (shot.x >= rightBoundary) {
            shot.x = rightBoundary
            activeVx = -activeVx
        }

        // Ceiling collision
        if (shot.y <= gridOffsetY + (bubbleRadius / 2f)) {
            shot.y = gridOffsetY + (bubbleRadius / 2f)
            snapBubbleToGrid(shot)
            return
        }

        // Bubble-Bubble grid collisions
        var collided = false
        val collisionDistSq = (bubbleRadius * 2f * 0.90f).let { it * it } // slight tolerance overlap for snappy gameplay

        for (r in 0 until GRID_ROWS) {
            for (c in 0 until GRID_COLS) {
                val gridBValue = grid[r][c] ?: continue
                if (gridBValue.isPopping) continue

                val dx = shot.x - gridBValue.x
                val dy = shot.y - gridBValue.y
                val distSq = dx * dx + dy * dy
                if (distSq <= collisionDistSq) {
                    collided = true
                    break
                }
            }
            if (collided) break
        }

        if (collided) {
            // Revert slightly to snap accurately to empty neighbors
            shot.x = prevX
            shot.y = prevY
            snapBubbleToGrid(shot)
        }
    }

    private fun snapBubbleToGrid(shot: Bubble) {
        // Find nearest empty grid slot coordinates
        var nearestRow = -1
        var nearestCol = -1
        var minDistSq = Float.MAX_VALUE

        for (r in 0 until GRID_ROWS) {
            val colsInRow = if (r % 2 == 0) GRID_COLS else GRID_COLS - 1
            for (c in 0 until colsInRow) {
                if (grid[r][c] != null) continue

                val centerPos = getBubbleCenter(r, c)
                val dx = shot.x - centerPos.x
                val dy = shot.y - centerPos.y
                val distSq = dx * dx + dy * dy
                if (distSq < minDistSq) {
                    minDistSq = distSq
                    nearestRow = r
                    nearestCol = c
                }
            }
        }

        // Snap and launch popping evaluations
        if (nearestRow != -1 && nearestCol != -1) {
            val centerPos = getBubbleCenter(nearestRow, nearestCol)
            val newBubble = Bubble(
                row = nearestRow,
                col = nearestCol,
                x = centerPos.x,
                y = centerPos.y,
                color = shot.color
            )
            grid[nearestRow][nearestCol] = newBubble
            activeShotBubble = null

            // Grid snapped - run flood fill color matching!
            evaluateGridMatches(nearestRow, nearestCol)
        } else {
            // Fallback - snap was too low down: trigger game over
            activeShotBubble = null
            triggerGameOver()
        }
    }

    private fun evaluateGridMatches(startRow: Int, startCol: Int) {
        val firstSnappedBubble = grid[startRow][startCol] ?: return
        val targetColor = firstSnappedBubble.color

        // Breadth-First-Search (BFS) logic to locate matching neighbor color elements
        val matchesQueue: Queue<Bubble> = LinkedList()
        val visited = mutableSetOf<Bubble>()

        matchesQueue.add(firstSnappedBubble)
        visited.add(firstSnappedBubble)

        while (matchesQueue.isNotEmpty()) {
            val current = matchesQueue.poll() ?: continue
            val neighbors = getNeighbors(current.row, current.col)

            for (neighbor in neighbors) {
                val r = neighbor.first
                val c = neighbor.second
                if (r in 0 until GRID_ROWS && c in 0 until GRID_COLS) {
                    val candidate = grid[r][c]
                    if (candidate != null && !visited.contains(candidate) && candidate.color == targetColor && !candidate.isPopping) {
                        visited.add(candidate)
                        matchesQueue.add(candidate)
                    }
                }
            }
        }

        // If target size >= 3, trigger explosion/deAttachment drops!
        if (visited.size >= 3) {
            score += visited.size * 10

            // Trigger poppy sequence
            for (matchedBubble in visited) {
                matchedBubble.isPopping = true
                matchedBubble.popScale = 1.0f
                // Spawning sparkles particle system
                spawnSparkles(matchedBubble.x, matchedBubble.y, matchedBubble.color.displayColor)
            }

            // Immediately run BFS dislodging checks to find disconnected components
            checkForDislodgedBubbles()
        } else {
            // No matches, check if we hit any game over deadline
            checkLossCondition()
        }
    }

    private fun getNeighbors(row: Int, col: Int): List<Pair<Int, Int>> {
        val neighbors = mutableListOf<Pair<Int, Int>>()
        // Directions differ based on odd/even row staggered offset
        if (row % 2 == 0) {
            // Even row neighbors
            neighbors.add(Pair(row - 1, col - 1))
            neighbors.add(Pair(row - 1, col))
            neighbors.add(Pair(row, col - 1))
            neighbors.add(Pair(row, col + 1))
            neighbors.add(Pair(row + 1, col - 1))
            neighbors.add(Pair(row + 1, col))
        } else {
            // Odd row neighbors
            neighbors.add(Pair(row - 1, col))
            neighbors.add(Pair(row - 1, col + 1))
            neighbors.add(Pair(row, col - 1))
            neighbors.add(Pair(row, col + 1))
            neighbors.add(Pair(row + 1, col))
            neighbors.add(Pair(row + 1, col + 1))
        }
        return neighbors
    }

    private fun checkForDislodgedBubbles() {
        val connectedToCeiling = mutableSetOf<Bubble>()
        val queue: Queue<Bubble> = LinkedList()

        // Seed BFS with active row 0 tiles
        val colsInRow0 = GRID_COLS
        for (c in 0 until colsInRow0) {
            val root = grid[0][c]
            if (root != null && !root.isPopping) {
                connectedToCeiling.add(root)
                queue.add(root)
            }
        }

        // BFS traversal
        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: continue
            val neighbors = getNeighbors(current.row, current.col)
            for (nb in neighbors) {
                val r = nb.first
                val c = nb.second
                if (r in 0 until GRID_ROWS && c in 0 until GRID_COLS) {
                    val candidate = grid[r][c]
                    if (candidate != null && !candidate.isPopping && !connectedToCeiling.contains(candidate)) {
                        connectedToCeiling.add(candidate)
                        queue.add(candidate)
                    }
                }
            }
        }

        // Identify and process dislodged unvisited items
        var dislodgedCount = 0
        for (r in 0 until GRID_ROWS) {
            val colsInRow = if (r % 2 == 0) GRID_COLS else GRID_COLS - 1
            for (c in 0 until colsInRow) {
                val bubble = grid[r][c]
                if (bubble != null && !bubble.isPopping && !connectedToCeiling.contains(bubble)) {
                    // Turn it into gravity falling entity!
                    grid[r][c] = null
                    dislodgedCount++

                    // Add nice physical velocities
                    val randVx = (-300..300).random().toFloat()
                    val randVy = (-200..0).random().toFloat() // slight initial bounce up
                    val randVz = (40..150).random().toFloat() // virtual 3D depth push
                    fallingBubbles.add(
                        FallingBubble(
                            x = bubble.x,
                            y = bubble.y,
                            z = 0f,
                            vx = randVx,
                            vy = randVy,
                            vz = randVz,
                            color = bubble.color,
                            rotationSpeed = (-360..360).random().toFloat()
                        )
                    )
                }
            }
        }

        if (dislodgedCount > 0) {
            score += dislodgedCount * 20
        }

        // Post updates evaluation - check if screen fully clear!
        checkWinCondition()
    }

    private fun spawnSparkles(x: Float, y: Float, colorCode: Int) {
        val numParticles = 12
        for (i in 0 until numParticles) {
            val angle = (2f * Math.PI.toFloat() * i / numParticles) + (Random.nextFloat() * 0.4f - 0.2f)
            val speed = (150..500).random().toFloat()
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            val life = (30..60).random()
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = colorCode,
                    life = life,
                    maxLife = life,
                    scale = 0.4f + Random.nextFloat() * 0.8f
                )
            )
        }
    }

    private fun updatePoppingBubbles(dt: Float) {
        for (r in 0 until GRID_ROWS) {
            val colsInRow = if (r % 2 == 0) GRID_COLS else GRID_COLS - 1
            for (c in 0 until colsInRow) {
                val b = grid[r][c]
                if (b != null && b.isPopping) {
                    b.popScale -= 7.5f * dt // Rapid shrink
                    if (b.popScale <= 0f) {
                        grid[r][c] = null // completely gone!
                    }
                }
            }
        }
    }

    private fun updateFallingBubbles(dt: Float) {
        val iterator = fallingBubbles.iterator()
        while (iterator.hasNext()) {
            val fb = iterator.next()
            // Translate 3D physics coords
            fb.vy += GRAVITY * dt
            fb.x += fb.vx * dt
            fb.y += fb.vy * dt
            fb.z += fb.vz * dt // Move into depth (3D effect)
            fb.rotation += fb.rotationSpeed * dt

            // Fade out
            fb.alpha -= 1.2f * dt
            fb.scale -= 0.6f * dt

            if (fb.y > VIRTUAL_HEIGHT + 100f || fb.alpha <= 0.05f || fb.scale <= 0.05f) {
                iterator.remove()
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life--

            p.alpha = p.life.toFloat() / p.maxLife.toFloat()
            p.scale = p.alpha * 1.5f

            if (p.life <= 0) {
                iterator.remove()
            }
        }
    }

    private fun checkWinCondition() {
        var bubblesLeft = 0
        for (r in 0 until GRID_ROWS) {
            val colsInRow = if (r % 2 == 0) GRID_COLS else GRID_COLS - 1
            for (c in 0 until colsInRow) {
                if (grid[r][c] != null) {
                    bubblesLeft++
                }
            }
        }

        if (bubblesLeft == 0) {
            state = State.VICTORY
            onScoreSavedListener?.invoke(score, level)
        }
    }

    private fun checkLossCondition() {
        // Find if bubbles have penetrated the grid limits (safety deadline)
        // Usually, if grid items exist below row 11, the user is dangerously low
        for (r in 11 until GRID_ROWS) {
            val colsInRow = if (r % 2 == 0) GRID_COLS else GRID_COLS - 1
            for (c in 0 until colsInRow) {
                val b = grid[r][c]
                if (b != null && !b.isPopping) {
                    triggerGameOver()
                    return
                }
            }
        }
    }

    private fun triggerGameOver() {
        state = State.GAME_OVER
        onScoreSavedListener?.invoke(score, level)
    }
}
