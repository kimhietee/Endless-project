package utils

import korlibs.image.bitmap.BmpSlice
import korlibs.image.color.Colors
import korlibs.korge.view.*
import korlibs.math.geom.Rectangle

/**
 * A spawned visual attack that handles its own animation, movement,
 * hitbox, and damage application.
 *
 * Damage logic
 * ─────────────
 * moving  = true  → projectile; hits each target AT MOST ONCE;
 *                   removed when it leaves the screen or animation finishes.
 * moving  = false → stationary; damagePerFrame = totalDamage / totalFrames;
 *                   applied every frame the hitbox overlaps a target.
 *
 * FIX: targets is now a lambda () -> List<Damageable> instead of a frozen
 * List<Damageable>. It is called fresh every frame so enemies that spawn
 * AFTER the skill is cast are included automatically.
 */
class AttackDisplay(
    private val config:     AttackConfig,
    startX:                 Double,
    startY:                 Double,
    private val getTargets: () -> List<Damageable>,   // ← lambda, evaluated every frame
    private val movingRight: Boolean = true,
    private val applySelfHeal: ((Double) -> Unit)? = null
) : Container() {

    companion object {
        val group: MutableList<AttackDisplay> = mutableListOf()

        fun spawn(
            config:      AttackConfig,
            startX:      Double,
            startY:      Double,
            getTargets:  () -> List<Damageable>,      // ← lambda
            container:   Container,
            movingRight: Boolean = true,
            applySelfHeal: ((Double) -> Unit)? = null
        ): AttackDisplay {
            val ad = AttackDisplay(config, startX, startY, getTargets, movingRight, applySelfHeal)
            group.add(ad)
            container.addChild(ad)
            return ad
        }

        fun updateAll(dt: Double) {
            val toRemove = mutableListOf<AttackDisplay>()
            for (ad in group) {
                ad.updateSelf(dt)
                if (ad.finished) toRemove.add(ad)
            }
            for (ad in toRemove) {
                group.remove(ad)
                ad.removeFromParent()
            }
        }

        fun clearAll() {
            for (ad in group) {
                ad.removeFromParent()
            }
            group.clear()
        }
    }

    // -------------------------------------------------------
    // BODY
    // -------------------------------------------------------
    private val body: Image = image(config.frames[0]) {
        anchor(0.5, 0.5)
        scaleX = if (movingRight) 1.0 else -1.0
    }

    // -------------------------------------------------------
    // ANIMATION STATE
    // -------------------------------------------------------
    private var currentFrame = 0
    private var frameTime    = 0.0
    private var repeatsDone  = 0
    var finished              = false
        private set

    // -------------------------------------------------------
    // DAMAGE STATE
    // -------------------------------------------------------
    private val totalFrames    = (config.frames.size * config.repeatAnimation).coerceAtLeast(1)
    private val damagePerFrame = config.damage / totalFrames.toDouble()
    // For moving attacks: track which targets have already been hit (hit-once logic)
    private val hitTargets: MutableSet<Damageable> = mutableSetOf()
    // Track which targets were damaged in the CURRENT animation frame
    // to avoid applying damage multiple times per animation frame when
    // the game's update rate exceeds the animation frame rate.
    private val damagedThisFrame: MutableSet<Damageable> = mutableSetOf()

    // -------------------------------------------------------
    // HITBOX
    // -------------------------------------------------------
    private var hitbox = Rectangle(0f, 0f, 0f, 0f)
    private val hitboxW get() = body.width  * config.hitboxScaleX
    private val hitboxH get() = body.height * config.hitboxScaleY

    // -------------------------------------------------------
    // DEBUG OUTLINE
    // -------------------------------------------------------
    // Always created; visibility toggled each frame via GameSettings.showHitbox.
    private val debugOutline: Container = container {
        val t = 2.0
        solidRect(1.0, t,   Colors["#ff0000"]).name("top")
        solidRect(1.0, t,   Colors["#ff0000"]).name("bot")
        solidRect(t,   1.0, Colors["#ff0000"]).name("lft")
        solidRect(t,   1.0, Colors["#ff0000"]).name("rgt")
        visible = false
    }

    // -------------------------------------------------------
    // INIT
    // -------------------------------------------------------
    init {
        this.xy(startX, startY)
        if (config.frames.isEmpty()) {
            finished = true
        } else {
            body.width  = config.frames[0].width.toDouble()  * config.displayScale
            body.height = config.frames[0].height.toDouble() * config.displayScale
        }
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
    private fun updateSelf(dt: Double) {
        if (finished) return

        val follow = config.followParent
        if (follow != null) {
            val ox = if (movingRight) config.followOffsetX else -config.followOffsetX
            this.x = follow.x + ox
            this.y = follow.y + config.followOffsetY
        } else if (config.moving) {
            this.x += config.speed * dt
            if (this.x < -200 || this.x > Constants.SCREEN_WIDTH + 200) {
                finished = true
                return
            }
        }

        // hitbox (world coords)
        hitbox = Rectangle(
            (this.x - hitboxW / 2).toFloat(),
            (this.y - hitboxH / 2).toFloat(),
            hitboxW.toFloat(),
            hitboxH.toFloat()
        )

        // debug outline — show/hide based on runtime setting
        debugOutline.visible = GameSettings.showHitbox
        debugOutline.let { c ->
            val w  = hitboxW
            val h  = hitboxH
            // The hitbox is centered on this container's position (this.x, this.y).
            // The debugOutline is a child of this container, so local origin = this.x/this.y.
            // Local offset to the hitbox top-left: (-w/2, -h/2).
            val ox = -w / 2
            val oy = -h / 2
            val t  = 2.0
            (c.children.firstOrNull { it.name == "top" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy) }
            (c.children.firstOrNull { it.name == "bot" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy + h - t) }
            (c.children.firstOrNull { it.name == "lft" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox, oy) }
            (c.children.firstOrNull { it.name == "rgt" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox + w - t, oy) }
        }

        // -------------------------------------------------------
        // COLLISION & DAMAGE
        // getTargets() is called fresh every frame — includes all
        // enemies alive right now, even ones spawned after skill cast.
        // -------------------------------------------------------
        if (config.damageEnemies) {
            for (target in getTargets()) {
                if (!target.isAlive()) continue
                if (!hitbox.intersects(target.hitboxRect())) continue
                if (config.moving) {
                    if (target !in hitTargets) {
                        target.takeDamage(config.damage)
                        hitTargets.add(target)
                    }
                } else {
                    if (target !in damagedThisFrame) {
                        target.takeDamage(damagePerFrame)
                        damagedThisFrame.add(target)
                    }
                }
            }
        }

        // animation
        frameTime += dt
        if (frameTime >= config.frameDuration) {
            frameTime = 0.0
            if (config.healSelfPerAnimationFrame > 0.0 && applySelfHeal != null) {
                applySelfHeal(config.healSelfPerAnimationFrame)
            }
            damagedThisFrame.clear()
            currentFrame++
            if (currentFrame >= config.frames.size) {
                currentFrame = 0
                repeatsDone++
                if (repeatsDone >= config.repeatAnimation) {
                    finished = true
                    return
                }
            }
            if (config.frames.isNotEmpty()) {
                body.bitmap = config.frames[currentFrame.coerceIn(0, config.frames.lastIndex)]
            }
        }
    }
}

// -------------------------------------------------------
// DAMAGEABLE INTERFACE
// -------------------------------------------------------
interface Damageable {
    fun takeDamage(amount: Double)
    fun isAlive(): Boolean
    fun hitboxRect(): Rectangle
}