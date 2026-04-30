import korlibs.image.bitmap.BmpSlice
import korlibs.image.color.Colors
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
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

    override fun takeDamage(amount: Double) {
        if (!isAlive()) return
        health = (health - amount).coerceAtLeast(0.0)
        damageTakenTimer = HP_BAR_SHOW_DURATION
        if (!isAlive()) state = EnemyState.DEAD
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
        private const val HP_BAR_Y_OFFSET      = 20.0

        private fun framePath(cfg: FrameConfig, index: Int): String {
            val num = if (cfg.zeroPad > 0) index.toString().padStart(cfg.zeroPad, '0')
            else index.toString()
            return "${cfg.folder}/${cfg.prefix}${num}.${cfg.extension}"
        }

        suspend fun loadFrames(cfg: FrameConfig): List<BmpSlice> =
            (cfg.startIndex until cfg.startIndex + cfg.count).map { i ->
                resourcesVfs[framePath(cfg, i)].readBitmapSlice()
            }

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
            if (value != field) { currentFrame = 0; frameTime = 0.0; field = value }
        }

    private var facingRight   = false
    private var frameTime     = 0.0
    private var currentFrame  = 0
    private var attackTimer   = 0.0
    private var deathAnimDone = false   // true once last death frame is reached
    private var deathTimer    = 0.0    // only counts after deathAnimDone = true
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

        if (Constants.SHOW_HITBOX) {
            debugOutline = container {
                val t = 2.0
                solidRect(1.0, t, Colors["#00ff44"]).name("top")
                solidRect(1.0, t, Colors["#00ff44"]).name("bot")
                solidRect(t, 1.0, Colors["#00ff44"]).name("lft")
                solidRect(t, 1.0, Colors["#00ff44"]).name("rgt")
            }
        }
    }

    // -------------------------------------------------------
    // HP BAR UPDATE
    // -------------------------------------------------------
    private fun updateHpBar() {
        val show = damageTakenTimer > 0.0
        hpBarBg.visible   = show
        hpBarFill.visible = show
        if (show) {
            val ratio = (health / config.maxHealth).coerceIn(0.0, 1.0)
            hpBarFill.width    = HP_BAR_WIDTH * ratio
            hpBarFill.colorMul = when {
                ratio > 0.5  -> Colors["#22cc44"]
                ratio > 0.25 -> Colors["#ffcc00"]
                else         -> Colors["#cc2222"]
            }
        }
    }

    // -------------------------------------------------------
    // DEBUG OUTLINE UPDATE
    // -------------------------------------------------------
    private fun updateDebugOutline() {
        debugOutline?.let { c ->
            val r  = hitboxRect()
            val w  = r.width.toDouble()
            val h  = r.height.toDouble()
            val ox = r.x.toDouble()
            val oy = r.y.toDouble()
            val t  = 2.0
            (c.firstOrNull { it.name == "top" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy) }
            (c.firstOrNull { it.name == "bot" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy + h - t) }
            (c.firstOrNull { it.name == "lft" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox, oy) }
            (c.firstOrNull { it.name == "rgt" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox + w - t, oy) }
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
        frameTime += dt
        if (frameTime >= config.frameDuration) {
            frameTime = 0.0
            if (state == EnemyState.DEAD) {
                if (!deathAnimDone) {
                    currentFrame++
                    if (currentFrame >= frames.size - 1) {
                        currentFrame  = frames.size - 1
                        deathAnimDone = true   // NOW start linger timer
                    }
                }
                // deathAnimDone = true → freeze on last frame, do nothing
            } else {
                val loops = state == EnemyState.IDLE || state == EnemyState.RUNNING
                currentFrame++
                if (currentFrame >= frames.size)
                    currentFrame = if (loops) 0 else frames.size - 1
            }
        }
        body.bitmap = currentFrameList()[currentFrame]
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
    // ATTACK SPAWNING — fully direction-aware
    // -------------------------------------------------------
    private fun spawnAttack(targets: List<Damageable>, container: Container) {
        val offsetX = if (facingRight) config.width * 0.5 else -config.width * 0.5
        val spawnX  = this.x + offsetX
        val spawnY  = this.y - config.height * 0.5

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
            targets     = targets,
            container   = container,
            movingRight = facingRight
        )
    }

    // -------------------------------------------------------
    // AI
    // -------------------------------------------------------
    private fun updateAI(
        dt:        Double,
        playerX:   Double,
        targets:   List<Damageable>,
        container: Container
    ) {
        if (!isAlive()) return

        attackTimer -= dt
        val dx       = playerX - this.x
        val distance = kotlin.math.abs(dx)

        facingRight = dx > 0
        this.scaleX = if (facingRight) 1.0 else -1.0

        if (state == EnemyState.ATTACKING) {
            if (currentFrame >= attackFrames.size - 1) state = EnemyState.IDLE
            return
        }

        when (config.behavior) {
            EnemyBehavior.MELEE, EnemyBehavior.RANGED -> {
                if (distance <= config.attackRange) {
                    if (attackTimer <= 0.0) {
                        state       = EnemyState.ATTACKING
                        attackTimer = config.attackCooldown
                        spawnAttack(targets, container)
                    } else {
                        state = EnemyState.IDLE
                    }
                } else {
                    state = EnemyState.RUNNING
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
        if (Constants.SHOW_HITBOX) updateDebugOutline()
    }
}
