    import korlibs.image.bitmap.BmpSlice
    import korlibs.image.color.Colors
    import korlibs.korge.view.*
    import korlibs.korge.view.align.centerXOn
    import kotlin.time.Duration
    import korlibs.event.*
    import korlibs.korge.input.InputKeys
    
    
    //class Fighter(val isPlayer: Boolean) : Container() {
    //    private val body = solidRect(50.0, 50.0, if (isPlayer) Colors.BLUE else Colors.RED)
    //
    //    init {
    //        // Position the body relative to the Container's (0,0)
    //        body.xy(0.0, 0.0)
    //    }
    //
    //    fun walk(direction: Double) {
    //        // Move the entire container left or right
    //        this.x += direction * 5.0
    //    }
    //}
    
    enum class CharacterState { IDLE, RUNNING, JUMPING, ATTACKING}
    
    data class AnimationConfig(
        val frames: List<BmpSlice>,
        val loop: Boolean
    )


    
    
    
    class Character(
        val isPlayer: Boolean,
        private val idleAnims: List<BmpSlice>,
        private val runAnims: List<BmpSlice>,
        private val jumpAnims: List<BmpSlice>,
        private val attackAnims: List<BmpSlice>

    ) : Container() {
        val characterWidth = 50.0
        val characterHeight = 60.0
    //    private val body = solidRect(characterWidth, characterHeight, if (isPlayer) Colors.BLUE else Colors.RED)
        private val body = image(idleAnims[0])
    
        private var state: CharacterState = CharacterState.IDLE
            private set(value) {
                if (value != field) {
                    currentFrame = 0
                    frameTime = 0.0
                    field = value
                }
            }
        private var facingRight = true
    
        private var runningSpeed = 300.0
    
    
        // animation
        private var frameTime = 0.0
    
    
        var velocityY = 0.0
        val gravity = Constants.GRAVITY // How fast they fall
        val jumpForce = -600.0 // Initial upward boost
        val groundY = Constants.GROUND // The "floor" (Constants.GROUND_Y - player height)
    
        private var currentTime = 0.0
        private val frameDuration = 0.12
    
        private var jumpIndex = 0
    
        private var currentFrame = 0
    
        private var attacking: Boolean = false
        private var jumping: Boolean = false
        private var running: Boolean = false
        private var idle: Boolean = false
    
        private var attackingIndex: Int = 0
        private var jumpingIndex: Int = 0
        private var runningIndex: Int = 0
        private var idleIndex: Int = 0
    //    private val animationMap = mapOf(
    //        "idle" to AnimationConfig(idleAnims, loop = true, 0),
    //        "run" to AnimationConfig(runAnims, loop = true, 0),
    //        "jump" to AnimationConfig(jumpAnims, loop = false, 0),
    //        "attack" to AnimationConfig(jumpAnims, loop = false, 0) // temp - replace when you add real attack anim
    //    )
    
        private val animationMap = mapOf(
            CharacterState.IDLE to AnimationConfig(idleAnims, loop = true),
            CharacterState.RUNNING to AnimationConfig(runAnims, loop = true),
            CharacterState.JUMPING to AnimationConfig(jumpAnims, loop = false),
            CharacterState.ATTACKING to AnimationConfig(attackAnims, loop = false) // temp - replace when you add real attack anim
        )
    
    
    
        init {
    //        body.xy(0.0, 0.0)
            body.anchor(0.5, 0.5)
        }
    
    
    
        fun updateAnimation(dt: Double) {
            val config = animationMap[state]?: animationMap[CharacterState.IDLE]!!
            frameTime += dt
            if (frameTime >= frameDuration) {
                frameTime = 0.0
                currentFrame++
                if (currentFrame >= config.frames.size) {
                    if (config.loop) {
                        currentFrame = 0
                    } else {
                        currentFrame = config.frames.size - 1
    //                    attacking = false
                    }
                }
    
            }
    
            body.bitmap = config.frames[currentFrame]
        }
    
    //    fun animate(
    //        animation: String,
    //        dt:Double,
    //    ) {
    //        currentTime += dt
    //        if (currentTime >= frameDuration) {
    //            var image = frames[index]
    //            index += 1
    //        }
    //    }
    
        fun jumpAnimation() {
    
        }
        fun idleAnimation() {
    
        }
        fun runAnimation() {
    
        }
        fun attackAnimation() {
    
        }
    
    
    
        fun move(direction: Double, dt: Double) { // good
    
            if (direction != 0.0) {
                this.x += direction * (runningSpeed * dt)
                this.x = this.x.coerceIn(0.0, Constants.SCREEN_WIDTH.toDouble()) // correct, don't change
                facingRight = direction > 0
                this.scaleX = if (facingRight) 1.0 else -1.0
                body.centerXOn(this)
    
            }
        }
    
    
    
    
        fun jump() { // good
            if (isOnGround()) {
                velocityY = jumpForce
            }
        }
    
    
        fun updatePhysics(dt: Double) { // good
            velocityY += gravity * dt
            this.y += velocityY * dt
    
            if (isOnGround()) {
                this.y = groundY - characterHeight
                velocityY = 0.0
            }
    //        println("y=${this.y}, bottom=${this.y + characterHeight}, ground=$groundY")
        }
    
    //    fun handleAnimation(dt: Double) {
    //
    //        if (state == CharacterState.RUNNING_JUMPING) {
    //            updateAnimation(dt, jumpAnims, false)
    //            if (state == CharacterState.RUNNING) {
    //                return
    //            }
    //        }
    //        when (state) {
    //            CharacterState.JUMPING -> updateAnimation(dt, jumpAnims, false)
    //            CharacterState.ATTACKING -> updateAnimation(dt, jumpAnims, false) // temp
    //            CharacterState.RUNNING -> updateAnimation(dt, runAnims, true)
    //            else -> updateAnimation(dt, idleAnims, true)
    //        }
    //    }
    
    
    
    
    
        // HELPERS
        fun isOnGround(): Boolean {
            // check if the player bottom is on the ground, and if no velocity
            return this.y + characterHeight >= groundY - 0.1 && velocityY >= 0.0
        }
    
    //    fun isJumping(): Boolean {
    //        return if (this.y + characterHeight >= groundY) false else true
    //    }
    
        // input class
        private data class PlayerInput(
            val direction: Double,
            val jump: Boolean,
            val attack: Boolean
        )
    
        // CORE

        private fun readInput(keys: InputKeys): PlayerInput {
            val moveLeft = keys[Key.LEFT] || keys[Key.A] || TouchInput.left
            val moveRight = keys[Key.RIGHT] || keys[Key.D] || TouchInput.right
            val jumpPressed = keys[Key.UP] || keys[Key.SPACE] || keys[Key.W] || TouchInput.jump
            val attackPressed = keys[Key.E] || TouchInput.attack

            val direction = when {
                moveRight -> 1.0
                moveLeft -> -1.0
                else -> 0.0
            }

            return PlayerInput(
                direction = direction,
                jump = jumpPressed,
                attack = attackPressed
            )
        }
    
    
        fun update(dt: Double, views: Views) {
            val keys: InputKeys = views.input.keys
    
            val input:PlayerInput = readInput(keys)
    
            move(input.direction, dt)
    
            if (input.jump && isOnGround()) {
                jump()
            }
    
            updatePhysics(dt)
    
    //        println(input.attack)
    //        println(state)
    //        println(attacking)
    //        println()
    
    //        if (input.attack) {
    //            state = CharacterState.ATTACKING
    //            attacking = true
    //        } else if (!attacking) {
            if (state == CharacterState.ATTACKING) {
                val config = animationMap[CharacterState.ATTACKING]!!
                if (currentFrame == config.frames.size - 1) {
                    // animation finished
                    state = CharacterState.IDLE
                }
            } else {
                if (input.attack) {
                    state = CharacterState.ATTACKING
                } else {
                    state = when {
                        !isOnGround() -> CharacterState.JUMPING
                        input.direction != 0.0 -> CharacterState.RUNNING
                        else -> CharacterState.IDLE
                    }
                }
            }
    
    
    
    
            updateAnimation(dt)
    
            }
    
    
    
    //    fun handleInput(keys: InputKeys) {
    //
    //        val moveLeft = keys[Key.LEFT] || keys[Key.A]
    //        val moveRight = keys[Key.RIGHT] || keys[Key.D]
    //        val jump = keys[Key.UP] || keys[Key.SPACE] || keys[Key.W]
    //        val attack = keys[Key.E]
    //
    //
    //
    //
    //        if (attack) {
    //            state = CharacterState.ATTACKING
    //        }
    //
    //        else if ((jump) && (moveLeft || moveRight)) {
    //            state = CharacterState.RUNNING_JUMPING
    //        }
    //        else if (moveRight) {
    //            state = CharacterState.RUNNING
    //            subState = CharacterState.RUNNING_RIGHT
    //        }
    //        else if (moveLeft) {
    //            state = CharacterState.RUNNING
    //            subState = CharacterState.RUNNING_LEFT
    //        }
    //        else if (jump) {
    //            state = CharacterState.JUMPING
    //        }
    //
    //
    //        else {
    //            state = CharacterState.IDLE
    //        }
    //
    //
    //
    //    }
    
    
    
    //    fun update(dt: Double, views: Views) {
    //        val keys = views.input.keys
    //
    //        handleInput(keys)
    //
    //        when (state) {
    //            CharacterState.JUMPING -> jump()
    //            CharacterState.ATTACKING -> jump()
    //            CharacterState.RUNNING -> when (subState) {
    //                CharacterState.RUNNING_LEFT -> move(-1.0, dt)
    //                else -> move(1.0, dt)
    //            }
    //            CharacterState.RUNNING_JUMPING -> when (subState) {
    //                CharacterState.RUNNING_LEFT -> {
    //                    move(-1.0, dt)
    //                    jump()
    //                }
    //                else -> {
    //                    move(1.0, dt)
    //                    jump()
    //                }
    //            }
    //            else -> move(0.0, dt)
    //
    //        }
    //
    //        updatePhysics(dt)
    //        handleAnimation(dt)
    //    }
    
    
    
    
    
    
    
    
    
    
    }
    
