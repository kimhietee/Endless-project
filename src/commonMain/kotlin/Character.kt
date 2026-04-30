import korlibs.image.bitmap.BmpSlice
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.event.*
import korlibs.korge.input.InputKeys
import korlibs.image.color.Colors
import korlibs.math.geom.Rectangle

enum class CharacterState { IDLE, RUNNING, JUMPING, ATTACKING, SKILL }

data class AnimationConfig(
    val frames: List<BmpSlice>,
    val loop:   Boolean
)

object ManaCost {
    const val ATTACK  = 0
    const val SKILL_1 = 5
    const val SKILL_2 = 5
    const val SKILL_3 = 5
    const val SKILL_4 = 5
}

class Character(
    val isPlayer: Boolean,
    private val idleAnims:      List<BmpSlice>,
    private val runAnims:       List<BmpSlice>,
    private val jumpAnims:      List<BmpSlice>,
    private val attackAnims:    List<BmpSlice>,
    private val skillAnims:     List<BmpSlice>,
    private val basicAtkFrames: List<BmpSlice>,
    private val skill1Frames:   List<BmpSlice>,
    private val skill2Frames:   List<BmpSlice>,
    private val skill3Frames:   List<BmpSlice>,
    private val skill4Frames:   List<BmpSlice>
) : Container(), Damageable {

    // -------------------------------------------------------
    // SIZE
    // -------------------------------------------------------
    val characterWidth  = 140.0
    val characterHeight = characterWidth * 1.14
    private val body    = image(idleAnims[0])

    // -------------------------------------------------------
    // STATS
    // -------------------------------------------------------
    val maxHealth = 200.0
    val maxMana   = 100.0
    var health    = maxHealth; private set
    var mana      = maxMana;   private set
    private val manaRegen = 5.0

    // -------------------------------------------------------
    // DAMAGEABLE
    // -------------------------------------------------------
    override fun takeDamage(amount: Double) {
        if (!isAlive()) return
        health = (health - amount).coerceAtLeast(0.0)
    }
    override fun isAlive() = health > 0.0
    override fun hitboxRect(): Rectangle {
        val w = characterWidth  * 0.6
        val h = characterHeight * 0.8
        return Rectangle(
            (this.x - w / 2).toFloat(),
            (this.y - h).toFloat(),
            w.toFloat(),
            h.toFloat()
        )
    }

    // -------------------------------------------------------
    // DEBUG BODY OUTLINE  (blue = player body)
    // -------------------------------------------------------
    private val debugOutline: Container? = if (Constants.SHOW_HITBOX) {
        container {
            val t = 2.0
            solidRect(1.0, t, Colors["#0044ff"]).name("top")
            solidRect(1.0, t, Colors["#0044ff"]).name("bot")
            solidRect(t, 1.0, Colors["#0044ff"]).name("lft")
            solidRect(t, 1.0, Colors["#0044ff"]).name("rgt")
        }
    } else null

    // -------------------------------------------------------
    // ATTACK CONFIGS
    // -------------------------------------------------------
    private fun buildBasicAtkConfig() = AttackConfig(
        frames          = basicAtkFrames,
        frameDuration   = 0.08,
        damage          = 15.0,
        moving          = false,       // melee = stationary hitbox in front
        speed           = 0.0,
        hitboxScaleX    = 0.6,
        hitboxScaleY    = 0.6,
        repeatAnimation = 1
    )
    private fun buildSkill1Config() = AttackConfig(
        frames          = skill1Frames,
        frameDuration   = 0.10,
        damage          = 20.0,
        moving          = true,
        speed           = if (facingRight) 400.0 else -400.0,
        hitboxScaleX    = 0.7,
        hitboxScaleY    = 0.7,
        repeatAnimation = 1
    )
    private fun buildSkill2Config() = AttackConfig(
        frames          = skill2Frames,
        frameDuration   = 0.10,
        damage          = 25.0,
        moving          = false,
        speed           = 0.0,
        hitboxScaleX    = 0.8,
        hitboxScaleY    = 0.8,
        repeatAnimation = 2
    )
    private fun buildSkill3Config() = AttackConfig(
        frames          = skill3Frames,
        frameDuration   = 0.10,
        damage          = 30.0,
        moving          = true,
        speed           = if (facingRight) 350.0 else -350.0,
        hitboxScaleX    = 0.7,
        hitboxScaleY    = 0.7,
        repeatAnimation = 1
    )
    private fun buildSkill4Config() = AttackConfig(
        frames          = skill4Frames,
        frameDuration   = 0.08,
        damage          = 40.0,
        moving          = false,
        speed           = 0.0,
        hitboxScaleX    = 1.0,
        hitboxScaleY    = 1.0,
        repeatAnimation = 3
    )

    // -------------------------------------------------------
    // STATE
    // -------------------------------------------------------
    private var state: CharacterState = CharacterState.IDLE
        set(value) {
            if (value != field) { currentFrame = 0; frameTime = 0.0; field = value }
        }
    var facingRight       = true
        private set
    private val runningSpeed  = 300.0
    private var frameTime     = 0.0
    private var currentFrame  = 0
    private val frameDuration = 0.12
    private val charSpeed     = 1.0
    private var actionPlaying = false

    var velocityY = 0.0
    val gravity   = Constants.GRAVITY
    val jumpForce = -600.0
    val groundY   = Constants.GROUND

    private val animationMap = mapOf(
        CharacterState.IDLE      to AnimationConfig(idleAnims,   loop = true),
        CharacterState.RUNNING   to AnimationConfig(runAnims,    loop = true),
        CharacterState.JUMPING   to AnimationConfig(jumpAnims,   loop = false),
        CharacterState.ATTACKING to AnimationConfig(attackAnims, loop = false),
        CharacterState.SKILL     to AnimationConfig(skillAnims,  loop = false)
    )

    init {
        body.anchor(0.5, 1.0)
        body.width  = characterWidth
        body.height = characterHeight
    }

    // -------------------------------------------------------
    // ANIMATION
    // -------------------------------------------------------
    private fun updateAnimation(dt: Double) {
        val config = animationMap[state] ?: animationMap[CharacterState.IDLE]!!
        frameTime += dt
        if (frameTime >= frameDuration) {
            frameTime = 0.0
            currentFrame++
            if (currentFrame >= config.frames.size)
                currentFrame = if (config.loop) 0 else config.frames.size - 1
        }
        body.bitmap = config.frames[currentFrame]
    }

    // -------------------------------------------------------
    // DEBUG OUTLINE UPDATE
    // -------------------------------------------------------
//    private fun updateDebugOutline() {
//        debugOutline?.let { c ->
//            val r  = hitboxRect()
//            val w  = r.width.toDouble()
//            val h  = r.height.toDouble()
//            val ox = r.x.toDouble()
//            val oy = r.y.toDouble()
//            val t  = 2.0
//            (c.firstOrNull { it.name == "top" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy) }
//            (c.firstOrNull { it.name == "bot" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy + h - t) }
//            (c.firstOrNull { it.name == "lft" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox, oy) }
//            (c.firstOrNull { it.name == "rgt" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox + w - t, oy) }
//        }
//    }

    // -------------------------------------------------------
    // MOVEMENT
    // -------------------------------------------------------
    private fun move(direction: Double, dt: Double) {
        if (direction != 0.0) {
            this.x  = (this.x + direction * runningSpeed * dt)
                .coerceIn(0.0, Constants.SCREEN_WIDTH.toDouble())
            facingRight = direction > 0
            this.scaleX = if (facingRight) 1.0 else -1.0
        }
    }
    private fun jump() { if (isOnGround()) velocityY = jumpForce }

    // -------------------------------------------------------
    // PHYSICS
    // -------------------------------------------------------
    private fun updatePhysics(dt: Double) {
        velocityY += gravity * dt
        this.y    += velocityY * dt
        if (this.y >= groundY) { this.y = groundY; velocityY = 0.0 }
    }
    fun isOnGround() = this.y >= groundY - 2.0

    // -------------------------------------------------------
    // MANA
    // -------------------------------------------------------
    private fun hasMana(cost: Int) = mana >= cost
    private fun spendMana(cost: Int) { mana = (mana - cost).coerceAtLeast(0.0) }
    private fun regenMana(dt: Double) { mana = (mana + manaRegen * dt).coerceAtMost(maxMana) }

    // -------------------------------------------------------
    // ATTACK SPAWNING
    // Basic attack: stationary melee hitbox spawned in front of player
    // Skills: moving projectiles in facing direction
    // -------------------------------------------------------
    private fun spawnAttack(
        atkConfig: AttackConfig,
        enemies:   List<Damageable>,
        container: Container
    ) {
        // For melee (non-moving): spawn hitbox directly in front, at body center height
        // For projectiles (moving): same spawn point, speed already encodes direction
        val offsetX    = if (facingRight) characterWidth * 0.6 else -characterWidth * 0.6
        val spawnX     = this.x + offsetX
        val spawnY     = this.y - characterHeight * 0.5
        val goingRight = atkConfig.speed >= 0.0
        AttackDisplay.spawn(atkConfig, spawnX, spawnY, enemies, container, movingRight = goingRight)
    }

    // -------------------------------------------------------
    // INPUT
    // -------------------------------------------------------
    private data class PlayerInput(
        val direction: Double,
        val jump:      Boolean,
        val attack:    Boolean,
        val skill1:    Boolean,
        val skill2:    Boolean,
        val skill3:    Boolean,
        val skill4:    Boolean
    )

    private fun readInput(keys: InputKeys): PlayerInput {
        val moveLeft  = keys[Key.LEFT]  || keys[Key.A] || TouchInput.left
        val moveRight = keys[Key.RIGHT] || keys[Key.D] || TouchInput.right
        return PlayerInput(
            direction = when { moveRight -> charSpeed; moveLeft -> -charSpeed; else -> 0.0 },
            jump      = keys[Key.UP] || keys[Key.SPACE] || keys[Key.W] || TouchInput.jump,
            attack    = keys[Key.E]  || TouchInput.attack,
            skill1    = keys[Key.Z]  || TouchInput.skill1,
            skill2    = keys[Key.X]  || TouchInput.skill2,
            skill3    = keys[Key.C]  || TouchInput.skill3,
            skill4    = keys[Key.V]  || TouchInput.skill4
        )
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
    fun update(
        dt:        Double,
        views:     Views,
        enemies:   List<Damageable>,
        container: Container
    ) {
        val input = readInput(views.input.keys)
        regenMana(dt)

        if (!actionPlaying) {
            when {
                input.attack && isOnGround() -> {
                    actionPlaying = true
                    state = CharacterState.ATTACKING
                    spawnAttack(buildBasicAtkConfig(), enemies, container)
                }
                isOnGround() && (input.skill1 || input.skill2 || input.skill3 || input.skill4) -> {
                    val (cost, cfg) = when {
                        input.skill1 -> ManaCost.SKILL_1 to buildSkill1Config()
                        input.skill2 -> ManaCost.SKILL_2 to buildSkill2Config()
                        input.skill3 -> ManaCost.SKILL_3 to buildSkill3Config()
                        else         -> ManaCost.SKILL_4 to buildSkill4Config()
                    }
                    if (hasMana(cost)) {
                        spendMana(cost)
                        actionPlaying = true
                        state = CharacterState.SKILL
                        spawnAttack(cfg, enemies, container)
                    }
                }
                input.jump -> jump()
            }
            move(input.direction, dt)
        }

        updatePhysics(dt)

        if (state == CharacterState.ATTACKING || state == CharacterState.SKILL) {
            if (currentFrame >= animationMap[state]!!.frames.size - 1) {
                actionPlaying = false
                state = CharacterState.IDLE
            }
        } else {
            state = when {
                !isOnGround()          -> CharacterState.JUMPING
                input.direction != 0.0 -> CharacterState.RUNNING
                else                   -> CharacterState.IDLE
            }
        }

        updateAnimation(dt)
//        updateDebugOutline()
    }
}
