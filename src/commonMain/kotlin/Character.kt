import korlibs.image.bitmap.BmpSlice
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.event.*
import korlibs.korge.input.InputKeys


enum class CharacterState { IDLE, RUNNING, JUMPING, ATTACKING, SKILL }

data class AnimationConfig(
    val frames: List<BmpSlice>,
    val loop: Boolean
)


class Character(
    val isPlayer: Boolean,
    private val idleAnims: List<BmpSlice>,
    private val runAnims: List<BmpSlice>,
    private val jumpAnims: List<BmpSlice>,
    private val attackAnims: List<BmpSlice>,
    private val skillAnims: List<BmpSlice>   // slash animation — shared by all 4 skills

) : Container() {
    val characterWidth  = 50.0
    val characterHeight = 60.0
    private val body = image(idleAnims[0])

    private var state: CharacterState = CharacterState.IDLE
        private set(value) {
            if (value != field) {
                currentFrame = 0
                frameTime    = 0.0
                field        = value
            }
        }

    private var facingRight  = true
    private var runningSpeed = 300.0
    private var frameTime    = 0.0
    private var currentFrame = 0
    private val frameDuration = 0.12
    private var characterSpeed = 1.0

    // tracks whether a non-looping action is playing (attack or skill)
    private var actionPlaying = false

    var velocityY  = 0.0
    val gravity    = Constants.GRAVITY
    val jumpForce  = -600.0
    val groundY    = Constants.GROUND

    private val animationMap = mapOf(
        CharacterState.IDLE      to AnimationConfig(idleAnims,   loop = true),
        CharacterState.RUNNING   to AnimationConfig(runAnims,    loop = true),
        CharacterState.JUMPING   to AnimationConfig(jumpAnims,   loop = false),
        CharacterState.ATTACKING to AnimationConfig(attackAnims, loop = false),
        CharacterState.SKILL     to AnimationConfig(skillAnims,  loop = false)
    )



    init {
        body.anchor(0.5, 0.5)
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
            if (currentFrame >= config.frames.size) {
                currentFrame = if (config.loop) 0 else config.frames.size - 1
            }
        }
        body.bitmap = config.frames[currentFrame]
    }

    // -------------------------------------------------------
    // MOVEMENT
    // -------------------------------------------------------
    private fun move(direction: Double, dt: Double) {
        if (direction != 0.0) {
            this.x += direction * (runningSpeed * dt)
            this.x = this.x.coerceIn(0.0, Constants.SCREEN_WIDTH.toDouble())
            facingRight  = direction > 0
            this.scaleX  = if (facingRight) 1.0 else -1.0
            body.centerXOn(this)
        }
    }

    private fun jump() {
        if (isOnGround()) velocityY = jumpForce
    }

    // -------------------------------------------------------
    // PHYSICS
    // -------------------------------------------------------
    private fun updatePhysics(dt: Double) {
        velocityY += gravity * dt
        this.y    += velocityY * dt
        if (this.y + characterHeight >= groundY) {
            this.y    = groundY - characterHeight
            velocityY = 0.0
        }
    }

    fun isOnGround() = this.y + characterHeight >= groundY - 2.0

    // -------------------------------------------------------
    // INPUT
    // -------------------------------------------------------
    private data class PlayerInput(
        val direction: Double,
        val jump:      Boolean,
        val attack:    Boolean,
        val skill:     Boolean   // any of the 4 skills — same animation
    )

    private fun readInput(keys: InputKeys): PlayerInput {
        val moveLeft    = keys[Key.LEFT]  || keys[Key.A] || TouchInput.left
        val moveRight   = keys[Key.RIGHT] || keys[Key.D] || TouchInput.right
        val jumpPressed = keys[Key.UP]    || keys[Key.SPACE] || keys[Key.W] || TouchInput.jump
        val attackPressed = keys[Key.E]   || TouchInput.attack

        // Z X C V on keyboard, or any skill touch button
        val skillPressed = keys[Key.Z] || keys[Key.X] || keys[Key.C] || keys[Key.V] ||
            TouchInput.skill1 || TouchInput.skill2 ||
            TouchInput.skill3 || TouchInput.skill4

        val direction = when {
            moveRight -> characterSpeed
            moveLeft  -> -characterSpeed
            else      -> 0.0
        }

        return PlayerInput(
            direction = direction,
            jump      = jumpPressed,
            attack    = attackPressed,
            skill     = skillPressed
        )
    }

    // -------------------------------------------------------
    // UPDATE  (called every frame from GameScene)
    // -------------------------------------------------------
    fun update(dt: Double, views: Views) {
        val input = readInput(views.input.keys)

        // === INPUT — only accept new action if none is playing ===
        if (!actionPlaying) {
            when {
                input.attack && isOnGround() -> {
                    actionPlaying = true
                    state = CharacterState.ATTACKING
                }
                input.skill && isOnGround() -> {
                    actionPlaying = true
                    state = CharacterState.SKILL
                }
                input.jump -> jump()
            }
            // movement always allowed
            move(input.direction, dt)
        }

        // === PHYSICS ===
        updatePhysics(dt)

        // === STATE MANAGEMENT ===
        if (state == CharacterState.ATTACKING || state == CharacterState.SKILL) {
            val config = animationMap[state]!!
            if (currentFrame >= config.frames.size - 1) {
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

        // === ANIMATION ===
        updateAnimation(dt)
    }
}
