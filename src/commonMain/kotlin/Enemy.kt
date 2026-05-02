import korlibs.image.bitmap.BmpSlice
import korlibs.image.color.Colors
import korlibs.korge.view.*
import korlibs.math.geom.Rectangle

enum class EnemyState { IDLE, RUNNING, ATTACKING, DEAD }

class Enemy(
    private val config: EnemyConfig
) : Container(), Damageable {

    // -------------------------------------------------------
    // SPRITES
    // -------------------------------------------------------
    private lateinit var idleFrames:   List<BmpSlice>
    private lateinit var runFrames:    List<BmpSlice>
    private lateinit var attackFrames: List<BmpSlice>
    private lateinit var deathFrames:  List<BmpSlice>
    private lateinit var body: Image

    // -------------------------------------------------------
    // PHYSICS
    // -------------------------------------------------------
    private val groundY   = Constants.GROUND
    private val gravity   = Constants.GRAVITY
    private var velocityY = 0.0

    // -------------------------------------------------------
    // STATS
    // -------------------------------------------------------
    var health = config.maxHealth
        private set

    /** XP awarded to the player when this enemy is killed. */
    val xpGain: Double get() = config.xpGain

    /**
     * Callback fired exactly ONCE when the enemy's health first reaches zero.
     * Set this in GameScene after spawning the enemy to award XP.
     */
    var onDeath: (() -> Unit)? = null
    private var deathCallbackFired = false

    override fun takeDamage(amount: Double) {
        if (!isAlive()) return
        health = (health - amount).coerceAtLeast(0.0)
        damageTakenTimer = HP_BAR_SHOW_DURATION
        if (!isAlive() && !deathCallbackFired) {
            deathCallbackFired = true
            state = EnemyState.DEAD
            onDeath?.invoke()
        }
    }
    override fun isAlive() = health > 0.0
    override fun hitboxRect(): Rectangle {
        val w = config.width  * 0.6
        val h = config.height * 0.8
        return Rectangle(
            (this.x - w / 2).toFloat(),
            (this.y - h).toFloat(),
            w.toFloat(),
            h.toFloat()
        )
    }

    // -------------------------------------------------------
    // COMPANION
    // -------------------------------------------------------
    companion object {
        private const val HP_BAR_SHOW_DURATION = 5.0
        private const val HP_BAR_WIDTH         = 80.0
        private const val HP_BAR_HEIGHT        = 8.0
        private const val HP_BAR_Y_OFFSET      = 5.0

        suspend fun loadFrames(cfg: FrameConfig): List<BmpSlice> =
            GameAssets.loadFrames(cfg)

        suspend fun create(config: EnemyConfig): Enemy {
            val idle   = loadFrames(config.idleConfig)
            val run    = loadFrames(config.runConfig)
            val attack = loadFrames(config.attackConfig)
            val death  = loadFrames(config.deathConfig)
            return Enemy(config).also { it.initialize(idle, run, attack, death) }
        }
    }

    // -------------------------------------------------------
    // HP BAR
    // -------------------------------------------------------
    private lateinit var hpBarBg:   SolidRect
    private lateinit var hpBarFill: SolidRect
    private var damageTakenTimer = 0.0

    // -------------------------------------------------------
    // DEBUG OUTLINE (green = enemy body)
    // -------------------------------------------------------
    private var debugOutline: Container? = null

    // -------------------------------------------------------
    // STATE
    // -------------------------------------------------------
    private var state: EnemyState = EnemyState.IDLE
        set(value) {
            if (value != field) {
                currentFrame = 0
                field = value
            }
        }

    private var facingRight   = false
    private var frameTime     = 0.0
    private var currentFrame  = 0
    private var attackTimer   = 0.0
    private var deathAnimDone = false
    private var deathTimer    = 0.0
    var shouldRemove          = false

    // -------------------------------------------------------
    // INITIALIZE
    // -------------------------------------------------------
    fun initialize(
        idle:   List<BmpSlice>,
        run:    List<BmpSlice>,
        attack: List<BmpSlice>,
        death:  List<BmpSlice>
    ) {
        idleFrames   = idle
        runFrames    = run
        attackFrames = attack
        deathFrames  = death

        body = image(idleFrames[0]) {
            anchor(0.5, 1.0)
            width  = config.width
            height = config.height
        }
        this.scaleX = -1.0

        val barX = -HP_BAR_WIDTH / 2
        val barY = -(config.height + HP_BAR_Y_OFFSET)
        hpBarBg = solidRect(HP_BAR_WIDTH, HP_BAR_HEIGHT, Colors["#330000"]).also {
            it.xy(barX, barY); it.visible = false
        }
        hpBarFill = solidRect(HP_BAR_WIDTH, HP_BAR_HEIGHT, Colors["#22cc44"]).also {
            it.xy(barX, barY); it.visible = false
        }

        // Always create; visibility toggled each frame via GameSettings.showHitbox
        debugOutline = container {
            val t = 2.0
            solidRect(1.0, t, Colors["#00ff44"]).name("top")
            solidRect(1.0, t, Colors["#00ff44"]).name("bot")
            solidRect(t, 1.0, Colors["#00ff44"]).name("lft")
            solidRect(t, 1.0, Colors["#00ff44"]).name("rgt")
            visible = false
        }
    }

    // -------------------------------------------------------
    // HP BAR UPDATE
    // -------------------------------------------------------
    private fun updateHpBar() {
        hpBarBg.visible   = true
        hpBarFill.visible = true
        val ratio = (health / config.maxHealth).coerceIn(0.0, 1.0)
        hpBarFill.width    = HP_BAR_WIDTH * ratio
        hpBarFill.colorMul = when {
            ratio > 0.5  -> Colors["#22cc44"]
            ratio > 0.25 -> Colors["#ffcc00"]
            else         -> Colors["#cc2222"]
        }
        hpBarBg.alpha = if (damageTakenTimer > 0.0) 1.0 else 0.75
    }

    // -------------------------------------------------------
    // DEBUG OUTLINE UPDATE
    // -------------------------------------------------------
    private fun updateDebugOutline() {
        debugOutline?.let { c ->
            val r  = hitboxRect()
            val w  = r.width.toDouble()
            val h  = r.height.toDouble()
            // hitboxRect() returns WORLD coordinates. Convert to LOCAL space by
            // subtracting the enemy container's own world position.
            val ox = r.x.toDouble() - this.x
            val oy = r.y.toDouble() - this.y
            val t  = 2.0
            (c.children.firstOrNull { it.name == "top" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy) }
            (c.children.firstOrNull { it.name == "bot" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy + h - t) }
            (c.children.firstOrNull { it.name == "lft" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox, oy) }
            (c.children.firstOrNull { it.name == "rgt" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox + w - t, oy) }
        }
    }

    // -------------------------------------------------------
    // ANIMATION
    // -------------------------------------------------------
    private fun currentFrameList() = when (state) {
        EnemyState.IDLE      -> idleFrames
        EnemyState.RUNNING   -> runFrames
        EnemyState.ATTACKING -> attackFrames
        EnemyState.DEAD      -> deathFrames
    }

    private fun updateAnimation(dt: Double) {
        val frames = currentFrameList()
        if (frames.isEmpty()) return
        if (frames.size == 1) {
            body.bitmap = frames[0]
            return
        }

        if (currentFrame >= frames.size) currentFrame = 0
        frameTime += dt

        while (frameTime >= config.frameDuration) {
            frameTime -= config.frameDuration
            when (state) {
                EnemyState.DEAD -> {
                    if (!deathAnimDone) {
                        currentFrame++
                        if (currentFrame >= frames.size - 1) {
                            currentFrame = frames.size - 1
                            deathAnimDone = true
                        }
                    }
                }
                EnemyState.ATTACKING -> {
                    currentFrame++
                    if (currentFrame >= frames.size) {
                        state = EnemyState.IDLE
                        currentFrame = 0
                        break
                    }
                }
                EnemyState.IDLE, EnemyState.RUNNING -> {
                    currentFrame = (currentFrame + 1) % frames.size
                }
            }
        }

        val list = currentFrameList()
        body.bitmap = list[currentFrame.coerceIn(0, list.lastIndex)]
    }

    // -------------------------------------------------------
    // PHYSICS
    // -------------------------------------------------------
    private fun updatePhysics(dt: Double) {
        velocityY += gravity * dt
        this.y    += velocityY * dt
        if (this.y >= groundY) { this.y = groundY; velocityY = 0.0 }
    }

    // -------------------------------------------------------
    // ATTACK SPAWNING
    // -------------------------------------------------------
    private fun spawnAttack(targets: List<Damageable>, container: Container) {
        val dir = if (facingRight) 1 else -1

        val centerY = this.y - config.height / 2.0
        val spawnX = this.x + (config.attackDisplayConfig.offsetX * dir)
        val spawnY = centerY + config.attackDisplayConfig.offsetY

        val directedConfig = if (config.attackDisplayConfig.moving) {
            val absSpeed = kotlin.math.abs(config.attackDisplayConfig.speed)
            config.attackDisplayConfig.copy(speed = if (facingRight) absSpeed else -absSpeed)
        } else {
            config.attackDisplayConfig
        }

        AttackDisplay.spawn(
            config      = directedConfig,
            startX      = spawnX,
            startY      = spawnY,
            getTargets  = { targets },
            container   = container,
            movingRight = facingRight
        )
    }

    // -------------------------------------------------------
    // AI
    // -------------------------------------------------------
    private fun updateAI(
        dt: Double,
        playerX: Double,
        targets: List<Damageable>,
        container: Container
    ) {
        if (!isAlive()) return

        val dx       = playerX - this.x
        val distance = kotlin.math.abs(dx)

        facingRight = dx > 0
        this.scaleX = if (facingRight) 1.0 else -1.0

        if (state != EnemyState.ATTACKING && attackTimer > 0.0) attackTimer -= dt
        if (state == EnemyState.ATTACKING) return

        when (config.behavior) {
            EnemyBehavior.MELEE, EnemyBehavior.RANGED -> {
                if (distance <= config.attackRange) {
                    if (attackTimer <= 0.0) {
                        state = EnemyState.ATTACKING
                        attackTimer = config.attackCooldown
                        spawnAttack(targets, container)
                    } else {
                        if (state != EnemyState.IDLE) state = EnemyState.IDLE
                    }
                } else {
                    if (state != EnemyState.RUNNING) state = EnemyState.RUNNING
                    this.x += (if (dx > 0) 1.0 else -1.0) * config.moveSpeed * dt
                }
            }
        }
    }

    // -------------------------------------------------------
    // DEATH — linger timer only starts after last death frame
    // -------------------------------------------------------
    private fun updateDeath(dt: Double) {
        if (deathAnimDone) {
            deathTimer += dt
            if (deathTimer >= config.deathLingerTime) shouldRemove = true
        }
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
    fun update(
        dt:        Double,
        playerX:   Double,
        targets:   List<Damageable>,
        container: Container
    ) {
        if (!isAlive()) {
            state = EnemyState.DEAD
            updateDeath(dt)
        } else {
            updateAI(dt, playerX, targets, container)
            if (damageTakenTimer > 0.0) damageTakenTimer -= dt
        }

        updatePhysics(dt)
        updateAnimation(dt)
        updateHpBar()
        debugOutline?.let { it.visible = GameSettings.showHitbox }
        if (GameSettings.showHitbox) updateDebugOutline()
    }
}