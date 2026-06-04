package com.example.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.*

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Runnable {

    var gameEngine = GameEngine()
    @Volatile
    private var isPlaying = false
    private var gameThread: Thread? = null
    private val surfaceHolder: SurfaceHolder = holder

    // Rendering Paints mapping for colors
    private val bubblePaintCache = mutableMapOf<BubbleColor, Paint>()
    private val bubbleStrokePaintCache = mutableMapOf<BubbleColor, Paint>()
    private val backgroundPaint = Paint()
    private val gridLinePaint = Paint()
    private val boundaryPaint = Paint()
    private val trajectoryPaint = Paint()
    private val particlePaint = Paint()
    private val textPaint = Paint()
    private val launcherPaint = Paint()

    // Screen scale mappings
    private var scaleFactor = 1.0f
    private var offsetX = 0.0f
    private var offsetY = 0.0f

    // Focal length/perspective depth properties
    private val cameraFocalLength = 1200f // Controls 3D curving rate

    init {
        surfaceHolder.addCallback(this)
        setupPaints()
    }

    private fun setupPaints() {
        backgroundPaint.style = Paint.Style.FILL

        gridLinePaint.color = 0x1A6366F1.toInt() // Faint Indigo gridlines matching #6366f1
        gridLinePaint.strokeWidth = 2f
        gridLinePaint.style = Paint.Style.STROKE

        boundaryPaint.color = 0x80FF3366.toInt() // Transparent reddish danger line
        boundaryPaint.strokeWidth = 6f
        boundaryPaint.style = Paint.Style.STROKE
        boundaryPaint.pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)

        trajectoryPaint.style = Paint.Style.FILL
        trajectoryPaint.color = 0xCCFFFFFF.toInt() // High-visibility bright white for aim trail

        particlePaint.style = Paint.Style.FILL

        launcherPaint.style = Paint.Style.FILL
        launcherPaint.color = 0xFF555555.toInt()

        textPaint.isAntiAlias = true
        textPaint.color = Color.WHITE
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    // Build or update shader paints for spherical 3D marble bubble look
    private fun ensureBubblePaints(radius: Float) {
        if (bubblePaintCache.isNotEmpty()) return

        for (bColor in BubbleColor.values()) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }
            bubblePaintCache[bColor] = paint

            val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = 0x33FFFFFF // subtle white trim at edges
            }
            bubbleStrokePaintCache[bColor] = stroke
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        resume()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Compute letterbox coordinates and sizing multipliers
        val scaleX = width.toFloat() / GameEngine.VIRTUAL_WIDTH
        val scaleY = height.toFloat() / GameEngine.VIRTUAL_HEIGHT
        scaleFactor = minOf(scaleX, scaleY)
        offsetX = (width - GameEngine.VIRTUAL_WIDTH * scaleFactor) / 2f
        offsetY = (height - GameEngine.VIRTUAL_HEIGHT * scaleFactor) / 2f

        // Initialize Paints using actual dimension ratios
        ensureBubblePaints(gameEngine.bubbleRadius)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    fun resume() {
        if (isPlaying) return
        isPlaying = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    fun pause() {
        isPlaying = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        gameThread = null
    }

    override fun run() {
        var lastTime = System.nanoTime()

        while (isPlaying) {
            val now = System.nanoTime()
            val elapsedSec = (now - lastTime) / 1_000_000_000f
            lastTime = now

            // Keep dt within reasonable limits to prevent clipping at extreme lag spikes
            val dt = minOf(elapsedSec, 0.1f)

            // Update physics & engine calculations
            gameEngine.update(dt)

            // Render visual frame
            if (isPlaying && surfaceHolder.surface.isValid) {
                var canvas: Canvas? = null
                try {
                    canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        drawFrame(canvas)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            // Cap frames loosely to maximize battery efficiency on mobile
            val frameTimeMs = (System.nanoTime() - now) / 1_000_000
            val sleepTime = 16L - frameTimeMs // target ~60fps
            if (sleepTime > 0 && isPlaying) {
                try {
                    Thread.sleep(sleepTime)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    // Touch event redirection
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameEngine.state != GameEngine.State.RUNNING) return false

        // Translate screen touch events to virtual canvas space
        val touchX = (event.x - offsetX) / scaleFactor
        val touchY = (event.y - offsetY) / scaleFactor

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                gameEngine.setAimTarget(touchX, touchY)
                return true
            }
            MotionEvent.ACTION_UP -> {
                gameEngine.fireActiveBubble()
                if (gameEngine.isHapticEnabled) {
                    performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun drawFrame(canvas: Canvas) {
        // Draw real environment background color matching premium styling: `#0F172A`
        canvas.drawColor(0xFF0F172A.toInt()) 

        // Save layout state, translate to centered scaled bounding box
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scaleFactor, scaleFactor)

        // Draw visual bounds border / clipping mask content
        drawGameBackground(canvas)

        // Draw 3D-cylindrical curved gridlines matching sci-fi dome cage appearance
        draw3DGridlines(canvas)

        // Draw danger deadline
        drawDangerDeadline(canvas)

        // Draw elements in back-to-front ordering
        drawStationaryBubbles(canvas)
        drawFallingBubbles(canvas)
        drawActiveShotBubble(canvas)
        drawAimTrajectory(canvas)
        drawParticles(canvas)
        drawLauncher(canvas)

        canvas.restore()
    }

    private fun drawGameBackground(canvas: Canvas) {
        // Draw subtle vignette gradient matching Vibrant Palette (Slate 800 to Slate 950)
        val gradPaint = Paint().apply {
            shader = RadialGradient(
                GameEngine.VIRTUAL_WIDTH / 2f,
                GameEngine.VIRTUAL_HEIGHT / 2f,
                GameEngine.VIRTUAL_HEIGHT * 0.7f,
                intArrayOf(0xFF1E293B.toInt(), 0xFF0F172A.toInt()),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, GameEngine.VIRTUAL_WIDTH, GameEngine.VIRTUAL_HEIGHT, gradPaint)
    }

    private fun draw3DGridlines(canvas: Canvas) {
        // Vertical perspective lines curving deep into cylinder background
        val numLines = 9
        val rowHeight = GameEngine.VIRTUAL_HEIGHT * 0.7f
        for (i in 0 until numLines) {
            val tX = GameEngine.VIRTUAL_WIDTH * (i.toFloat() / (numLines - 1))
            val path = Path()

            path.moveTo(tX, GameEngine.VIRTUAL_HEIGHT)
            var stepY = GameEngine.VIRTUAL_HEIGHT
            while (stepY > 50f) {
                stepY -= 50f
                // Apply 3D cylindrical curving equations to layout points
                val pt = project3DPoint(tX, stepY, 0f)
                path.lineTo(pt.x, pt.y)
            }
            canvas.drawPath(path, gridLinePaint)
        }

        // Horizontal circular curve guidelines
        for (y in 1..8) {
            val gridY = gameEngine.gridOffsetY + y * gameEngine.rowSpacing
            val path = Path()
            path.moveTo(0f, gridY)
            var stepX = 0f
            while (stepX < GameEngine.VIRTUAL_WIDTH) {
                stepX += 20f
                val pt = project3DPoint(stepX, gridY, 0f)
                if (stepX == 20f) {
                    path.moveTo(pt.x, pt.y)
                } else {
                    path.lineTo(pt.x, pt.y)
                }
            }
            canvas.drawPath(path, gridLinePaint)
        }
    }

    private fun drawDangerDeadline(canvas: Canvas) {
        // Row 11-12 boundary line
        val lineY = gameEngine.gridOffsetY + 11 * gameEngine.rowSpacing
        val path = Path()
        var stepX = 0f
        while (stepX < GameEngine.VIRTUAL_WIDTH) {
            stepX += 15f
            val pt = project3DPoint(stepX, lineY, 0f)
            if (stepX == 15f) {
                path.moveTo(pt.x, pt.y)
            } else {
                path.lineTo(pt.x, pt.y)
            }
        }
        canvas.drawPath(path, boundaryPaint)
    }

    private fun drawStationaryBubbles(canvas: Canvas) {
        for (r in 0 until GameEngine.GRID_ROWS) {
            val colsInRow = if (r % 2 == 0) GameEngine.GRID_COLS else GameEngine.GRID_COLS - 1
            for (c in 0 until colsInRow) {
                val bubble = gameEngine.grid[r][c] ?: continue
                drawSingleBubbleProjected(canvas, bubble.x, bubble.y, 0f, bubble.color, bubble.popScale)
            }
        }
    }

    private fun drawFallingBubbles(canvas: Canvas) {
        // Render pre-extracted physical dislodges with 3D rotation shifts
        for (fb in gameEngine.fallingBubbles) {
            canvas.save()
            // Project the falling coordinates including virtual depth fb.z
            val pt = project3DPoint(fb.x, fb.y, fb.z)
            val scaleMult = cameraFocalLength / (cameraFocalLength + fb.z)
            val finalRadius = gameEngine.bubbleRadius * scaleMult * fb.scale

            canvas.translate(pt.x, pt.y)
            canvas.rotate(fb.rotation)

            val paint = bubblePaintCache[fb.color] ?: continue
            val stroke = bubbleStrokePaintCache[fb.color] ?: continue

            // Setup light specular radial gradient aligned for falling coordinates
            paint.shader = RadialGradient(
                -finalRadius * 0.3f,
                -finalRadius * 0.3f,
                finalRadius * 1.3f,
                intArrayOf(0xFFFFFFFF.toInt(), fb.color.displayColor, darkenColor(fb.color.displayColor)),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.alpha = (fb.alpha * 255).toInt().coerceIn(0, 255)
            stroke.alpha = paint.alpha

            canvas.drawCircle(0f, 0f, finalRadius, paint)
            canvas.drawCircle(0f, 0f, finalRadius, stroke)

            // Speck gloss reflection
            val glossPaint = Paint().apply {
                color = Color.WHITE
                alpha = (fb.alpha * 120).toInt().coerceIn(0, 255)
                style = Paint.Style.FILL
            }
            canvas.drawCircle(-finalRadius * 0.4f, -finalRadius * 0.4f, finalRadius * 0.22f, glossPaint)

            canvas.restore()
        }
    }

    private fun drawActiveShotBubble(canvas: Canvas) {
        val shot = gameEngine.activeShotBubble ?: return
        drawSingleBubbleProjected(canvas, shot.x, shot.y, 0f, shot.color, 1.0f)
    }

    private fun drawAimTrajectory(canvas: Canvas) {
        if (gameEngine.activeShotBubble != null) return // Hide trajectory while active bullet flies

        val trajectory = gameEngine.calculateAimTrajectory()
        var size = 16f
        for (pt in trajectory) {
            val projected = project3DPoint(pt.x, pt.y, 0f)
            val scaleMult = cameraFocalLength / (cameraFocalLength + 0f)
            val renderR = size * scaleMult

            // Glow bubble-like small spheres for trajectories
            val paint = Paint().apply {
                isAntiAlias = true
                shader = RadialGradient(
                    projected.x - renderR * 0.2f,
                    projected.y - renderR * 0.2f,
                    renderR * 1.2f,
                    intArrayOf(0xFFFFFFFF.toInt(), 0xAAFFFFFF.toInt(), 0x00FFFFFF),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(projected.x, projected.y, renderR, paint)

            // Decline size gradually down the trail
            if (size > 6f) size -= 0.35f
        }
    }

    private fun drawParticles(canvas: Canvas) {
        for (p in gameEngine.particles) {
            val projected = project3DPoint(p.x, p.y, 0f)
            particlePaint.color = p.color
            particlePaint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)

            // Draw glowing core plus outer halos
            val haloRadius = 15f * p.scale
            val radGrad = RadialGradient(
                projected.x,
                projected.y,
                haloRadius,
                intArrayOf(Color.WHITE, p.color, 0x00000000),
                floatArrayOf(0f, 0.4f, 1f),
                Shader.TileMode.CLAMP
            )
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = radGrad
                alpha = particlePaint.alpha
            }
            canvas.drawCircle(projected.x, projected.y, haloRadius, glowPaint)
        }
    }

    private fun drawLauncher(canvas: Canvas) {
        val turretRadius = 130f
        val projectedLauncher = project3DPoint(gameEngine.launcherX, gameEngine.launcherY, 0f)

        // Draw turret base pedestal matching high contrast luxury visual
        val radialLauncherPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                projectedLauncher.x,
                projectedLauncher.y,
                turretRadius,
                intArrayOf(0xFF334155.toInt(), 0xFF1E293B.toInt(), 0xFF0F172A.toInt()),
                floatArrayOf(0f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(projectedLauncher.x, projectedLauncher.y, turretRadius, radialLauncherPaint)

        // Draw launcher aiming outline turret ring
        val trackRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0xFF6366F1.toInt() // Indigo 500
        }
        canvas.drawCircle(projectedLauncher.x, projectedLauncher.y, turretRadius * 0.75f, trackRingPaint)

        // Draw indicator targeting tube pointing at active angle
        canvas.save()
        canvas.translate(projectedLauncher.x, projectedLauncher.y)
        canvas.rotate(Math.toDegrees(gameEngine.aimAngle.toDouble()).toFloat() + 90f)

        val barrelHeight = 120f
        val barrelWidth = 24f
        val barrelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                -barrelWidth / 2f, 0f, barrelWidth / 2f, 0f,
                intArrayOf(0xFF4F46E5.toInt(), 0xFF818CF8.toInt(), 0xFF312E81.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        val p = Path()
        p.moveTo(-barrelWidth / 2f, 0f)
        p.lineTo(-barrelWidth / 3f, -barrelHeight)
        p.lineTo(barrelWidth / 3f, -barrelHeight)
        p.lineTo(barrelWidth / 2f, 0f)
        p.close()
        canvas.drawPath(p, barrelPaint)

        canvas.restore()

        // Render current launcher bubble sitting inside turret center
        drawSingleBubbleProjected(canvas, gameEngine.launcherX, gameEngine.launcherY, 0f, gameEngine.currentLauncherColor, 1.0f)
    }

    // Comprehensive rendering block of 3D marble bubble
    private fun drawSingleBubbleProjected(
        canvas: Canvas,
        worldX: Float,
        worldY: Float,
        worldZ: Float,
        color: BubbleColor,
        scale: Float
    ) {
        // Apply cylindrical vault translation projection
        val pt = project3DPoint(worldX, worldY, worldZ)

        // Calculate perspective scale factor
        val scaleMult = cameraFocalLength / (cameraFocalLength + worldZ)
        val finalRadius = gameEngine.bubbleRadius * scaleMult * scale

        if (finalRadius <= 0f) return

        val paint = bubblePaintCache[color] ?: return
        val stroke = bubbleStrokePaintCache[color] ?: return

        // Radial specular shader to simulate 3D lit sphere surface
        paint.shader = RadialGradient(
            pt.x - finalRadius * 0.33f,
            pt.y - finalRadius * 0.33f,
            finalRadius * 1.33f,
            intArrayOf(0xFFFFFFFF.toInt(), color.displayColor, darkenColor(color.displayColor)),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )

        canvas.drawCircle(pt.x, pt.y, finalRadius, paint)
        canvas.drawCircle(pt.x, pt.y, finalRadius, stroke)

        // Small gloss reflection speck at top-left
        val glossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            alpha = 150
            style = Paint.Style.FILL
        }
        canvas.drawCircle(pt.x - finalRadius * 0.42f, pt.y - finalRadius * 0.42f, finalRadius * 0.20f, glossPaint)
    }

    // Virtual cylindrical 3D back-wall project mathematical solver
    private fun project3DPoint(x: Float, y: Float, z: Float): PointF {
        // Curves coordinates horizontally (X) backwards dynamically based on distance from center X
        val midX = GameEngine.VIRTUAL_WIDTH / 2f
        val dist = x - midX
        val maxDist = midX

        // Curve angle representation (curves back in cylinder barrel shape)
        val theta = (dist / maxDist) * (Math.PI / 2).toFloat() * 0.70f // curving offset modifier
        val zCylinder = (1.0f - cos(theta)) * 250f + z

        // Calculate lens scale
        val scale = cameraFocalLength / (cameraFocalLength + zCylinder)

        // Screen projected mapping
        val rx = midX + dist * scale
        val ry = y * scale

        return PointF(rx, ry)
    }

    // Helper functions for sphere shaders
    private fun darkenColor(color: Int): Int {
        val r = (Color.red(color) * 0.45f).toInt()
        val g = (Color.green(color) * 0.45f).toInt()
        val b = (Color.blue(color) * 0.45f).toInt()
        return Color.rgb(r, g, b)
    }
}
