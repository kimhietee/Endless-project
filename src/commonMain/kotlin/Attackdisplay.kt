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
 */
class AttackDisplay(
    private val config:  AttackConfig,
    startX:              Double,
    startY:              Double,
    private val targets: List<Damageable>,
    /** true = moving right, false = moving left. Controls sprite flip. */
    private val movingRight: Boolean = true
) : Container() {

    companion object {
        val group: MutableList<AttackDisplay> = mutableListOf()

        fun spawn(
            config:      AttackConfig,
            startX:      Double,
            startY:      Double,
            targets:     List<Damageable>,
            container:   Container,
            movingRight: Boolean = true
        ): AttackDisplay {
            val ad = AttackDisplay(config, startX, startY, targets, movingRight)
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
        // flip sprite to face direction of travel
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
    private val totalFrames    = config.frames.size * config.repeatAnimation
    private val damagePerFrame = config.damage / totalFrames.toDouble()
    private val hitTargets: MutableSet<Damageable> = mutableSetOf()

    // -------------------------------------------------------
    // HITBOX
    // -------------------------------------------------------
    private var hitbox = Rectangle(0f, 0f, 0f, 0f)
    private val hitboxW get() = body.width  * config.hitboxScaleX
    private val hitboxH get() = body.height * config.hitboxScaleY

    // -------------------------------------------------------
    // DEBUG OUTLINE  (red, outline only, attack hitbox)
    // Only created when Constants.SHOW_HITBOX = true
    // -------------------------------------------------------
    private val debugOutline: Container? = if (Constants.SHOW_HITBOX) {
        container {
            // 4 thin rects forming an outline — 2px border
            val t = 2.0
            solidRect(1.0, t,   Colors["#ff0000"]).name("top")
            solidRect(1.0, t,   Colors["#ff0000"]).name("bot")
            solidRect(t,   1.0, Colors["#ff0000"]).name("lft")
            solidRect(t,   1.0, Colors["#ff0000"]).name("rgt")
        }
    } else null

    // -------------------------------------------------------
    // INIT
    // -------------------------------------------------------
    init {
        this.xy(startX, startY)
        body.width  = config.frames[0].width.toDouble()  * config.displayScale
        body.height = config.frames[0].height.toDouble() * config.displayScale
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
    private fun updateSelf(dt: Double) {
        if (finished) return

        // movement
        if (config.moving) {
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

        // debug outline
        debugOutline?.let { c ->
            val w = hitboxW
            val h = hitboxH
            val ox = this.x - w / 2
            val oy = this.y - h / 2
            val t = 2.0
            (c.children.firstOrNull { it.name == "top" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy) }
            (c.children.firstOrNull { it.name == "bot" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy + h - t) }
            (c.children.firstOrNull { it.name == "lft" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox, oy) }
            (c.children.firstOrNull { it.name == "rgt" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox + w - t, oy) }
        }

        // collision & damage
        for (target in targets) {
            if (!target.isAlive()) continue
            if (!hitbox.intersects(target.hitboxRect())) continue
            if (config.moving) {
                if (target !in hitTargets) {
                    target.takeDamage(config.damage)
                    hitTargets.add(target)
                }
            } else {
                target.takeDamage(damagePerFrame)
            }
        }

        // animation
        frameTime += dt
        if (frameTime >= config.frameDuration) {
            frameTime = 0.0
            currentFrame++
            if (currentFrame >= config.frames.size) {
                currentFrame = 0
                repeatsDone++
                if (repeatsDone >= config.repeatAnimation) {
                    finished = true
                    return
                }
            }
            body.bitmap = config.frames[currentFrame]
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
