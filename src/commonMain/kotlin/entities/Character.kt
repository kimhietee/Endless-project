package entities

import korlibs.image.bitmap.BmpSlice
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.event.*
import korlibs.korge.input.InputKeys
import korlibs.image.color.Colors
import korlibs.math.geom.Rectangle
import korlibs.korge.view.SolidRect
import utils.*
import managers.*
import entities.heroes.HeroConfig

enum class CharacterState { IDLE, RUNNING, JUMPING, ATTACKING, SKILL }

data class AnimationConfig(
    val frames: List<BmpSlice>,
    val loop:   Boolean
)

class Character(
    val isPlayer: Boolean,
    val heroConfig: HeroConfig,
    private val idleAnims:      List<BmpSlice>,
    private val runAnims:       List<BmpSlice>,
    private val jumpAnims:      List<BmpSlice>,
    private val attackAnims:    List<BmpSlice>,
    private val skillAnims:     List<BmpSlice>,
    private val basicAtkFrames: List<BmpSlice>,
    private val skill1Frames:   List<BmpSlice>,
    private val skill2Frames:   List<BmpSlice>,
    private val skill3Frames:   List<BmpSlice>,
    private val skill4Frames:   List<BmpSlice>,
    /** Body cast animation during skill 3 (e.g. Wanderer 334 sheet). Empty → use [skillAnims]. */
    private val skill3CastBodyFrames: List<BmpSlice> = emptyList(),
    /** Charge frames on body before skill4 orb. Empty → orb spawns immediately. */
    private val skill4ChargeFrames: List<BmpSlice> = emptyList()
) : Container(), Damageable {

    val characterWidth  = 140.0
    val characterHeight = 140.0
    private val body    = image(idleAnims[0])

    var maxHealth = heroConfig.maxHealth; private set
    val maxMana   = heroConfig.maxMana
    var health    = heroConfig.maxHealth; private set
    var mana      = heroConfig.maxMana; private set
    private val manaRegen   = heroConfig.manaRegen
    private val healthRegen = heroConfig.healthRegen

    private companion object PlayerHpBar {
        const val WIDTH    = 80.0
        const val HEIGHT   = 8.0
        const val Y_OFFSET = 5.0
    }

    private lateinit var hpBarBg: SolidRect
    private lateinit var hpBarFill: SolidRect
    private var hpBarDamageFlash = 0.0

    private lateinit var debugOutline: Container

    override fun takeDamage(amount: Double) {
        if (!isAlive()) return
        if (isPlayer && GameSettings.developerMode && GameSettings.godMode) return
        health = (health - amount).coerceAtLeast(0.0)
        if (isPlayer) hpBarDamageFlash = 5.0
    }

    fun heal(amount: Double) {
        health = (health + amount).coerceAtMost(maxHealth)
    }

    fun increaseMaxHealth(amount: Double) {
        maxHealth += amount
        heal(amount)
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

    val basicAttackSkill     = heroConfig.basicAttackSkill
    val skill1Config         = heroConfig.skill1Config
    val skill2Config         = heroConfig.skill2Config
    val skill3Config         = heroConfig.skill3Config
    val skill4Config         = heroConfig.skill4Config
    val healingSkillConfig   = heroConfig.healingSkillConfig
    val maxHealthSkillConfig = heroConfig.maxHealthSkillConfig

    private var lastMaxHealthUpgradeCount = 0

    val allSkills = heroConfig.allSkills

    private fun buildBasicAtkConfig(): AttackConfig {
        val t = heroConfig.basicAttackTuning
        return AttackConfig(
            frames          = basicAtkFrames,
            frameDuration   = t.frameDuration,
            damage          = basicAttackSkill.damage,
            moving          = t.moving,
            speed           = t.speed,
            hitboxScaleX    = t.hitboxScaleX,
            hitboxScaleY    = t.hitboxScaleY,
            repeatAnimation = t.repeatAnimation,
            displayScale    = t.displayScale,
            offsetX         = t.offsetX,
            offsetY         = t.offsetY
        )
    }

    private fun buildSkill1Config(): AttackConfig {
        val t = heroConfig.skill1Tuning
        val spd = if (facingRight) t.projectileSpeed else -t.projectileSpeed
        return AttackConfig(
            frames          = skill1Frames,
            frameDuration   = t.frameDuration,
            damage          = skill1Config.damage,
            moving          = t.moving,
            speed           = spd,
            hitboxScaleX    = t.hitboxScaleX,
            hitboxScaleY    = t.hitboxScaleY,
            repeatAnimation = t.repeatAnimation,
            displayScale    = t.displayScale,
            offsetX         = t.offsetX,
            offsetY         = t.offsetY
        )
    }

    private fun buildSkill2Config(): AttackConfig {
        val t = heroConfig.skill2Tuning
        if (heroConfig.skill2AuraTotalHeal > 0.0) {
            val ticks = (skill2Frames.size * t.repeatAnimation).coerceAtLeast(1)
            val healPer = heroConfig.skill2AuraTotalHeal / ticks.toDouble()
            return AttackConfig(
                frames          = skill2Frames,
                frameDuration   = t.frameDuration,
                damage          = 0.0,
                moving          = false,
                speed           = 0.0,
                hitboxScaleX    = t.hitboxScaleX,
                hitboxScaleY    = t.hitboxScaleY,
                repeatAnimation = t.repeatAnimation,
                displayScale    = t.displayScale,
                offsetX         = t.offsetX,
                offsetY         = t.offsetY,
                healSelfPerAnimationFrame = healPer,
                damageEnemies   = false
            )
        }
        val n = heroConfig.skill2RepeatCount
        return AttackConfig(
            frames          = skill2Frames,
            frameDuration   = t.frameDuration,
            damage          = skill2Config.damage * n,
            moving          = t.moving,
            speed           = if (facingRight) t.projectileSpeed else -t.projectileSpeed,
            hitboxScaleX    = t.hitboxScaleX,
            hitboxScaleY    = t.hitboxScaleY,
            repeatAnimation = t.repeatAnimation,
            displayScale    = t.displayScale,
            offsetX         = t.offsetX,
            offsetY         = t.offsetY
        )
    }

    private fun buildSkill3Config(): AttackConfig {
        val t = heroConfig.skill3Tuning
        return AttackConfig(
            frames          = skill3Frames,
            frameDuration   = t.frameDuration,
            damage          = skill3Config.damage,
            moving          = t.moving,
            speed           = if (facingRight) t.projectileSpeed else -t.projectileSpeed,
            hitboxScaleX    = t.hitboxScaleX,
            hitboxScaleY    = t.hitboxScaleY,
            repeatAnimation = t.repeatAnimation,
            displayScale    = t.displayScale,
            offsetX         = t.offsetX,
            offsetY         = t.offsetY
        )
    }

    private fun buildSkill4Config(): AttackConfig {
        val t = heroConfig.skill4Tuning
        return AttackConfig(
            frames          = skill4Frames,
            frameDuration   = t.frameDuration,
            damage          = skill4Config.damage,
            moving          = t.moving,
            speed           = if (facingRight) t.projectileSpeed else -t.projectileSpeed,
            hitboxScaleX    = t.hitboxScaleX,
            hitboxScaleY    = t.hitboxScaleY,
            repeatAnimation = t.repeatAnimation,
            displayScale    = t.displayScale,
            offsetX         = t.offsetX,
            offsetY         = t.offsetY
        )
    }

    private var state: CharacterState = CharacterState.IDLE
        set(value) {
            if (value != field) { currentFrame = 0; frameTime = 0.0; field = value }
        }
    var facingRight = true
        private set

    private val runningSpeed  = heroConfig.runningSpeed
    private var frameTime     = 0.0
    private var currentFrame  = 0
    private val frameDuration = heroConfig.animFrameDuration
    private val charSpeed     = heroConfig.charSpeed
    private var actionPlaying = false

    private var activeSkillSlot = 0

    private var skill4ChargePhase = false
    private var skill4ChargeIndex = 0
    private var skill4ChargeElapsed = 0.0
    private var skill4OrbSpawned = false
    private var skill4BallWaitElapsed = 0.0

    private var skill2AuraTimer = 0.0

    var velocityY = 0.0
    val gravity   = heroConfig.gravity
    val jumpForce = heroConfig.jumpForce
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

        if (isPlayer) {
            val barX = -PlayerHpBar.WIDTH / 2
            val barY = -(characterHeight + PlayerHpBar.Y_OFFSET)
            hpBarBg = solidRect(PlayerHpBar.WIDTH, PlayerHpBar.HEIGHT, Colors["#330000"]).also {
                it.xy(barX, barY)
            }
            hpBarFill = solidRect(PlayerHpBar.WIDTH, PlayerHpBar.HEIGHT, Colors["#22cc44"]).also {
                it.xy(barX, barY)
            }
            addChild(hpBarBg)
            addChild(hpBarFill)
        }

        debugOutline = Container()
        addChild(debugOutline)
        debugOutline.visible = false
    }

    private fun skill3BodyFrames(): List<BmpSlice> =
        if (heroConfig.bodyAnimRules.skill3BodyUsesCastSheet && skill3CastBodyFrames.isNotEmpty()) skill3CastBodyFrames
        else skillAnims

    private fun skillSlotBodyFrames(): List<BmpSlice> = when (activeSkillSlot) {
        1 -> if (heroConfig.bodyAnimRules.skill1UsesAttackAnimForBody) attackAnims else skillAnims
        2 -> idleAnims
        3 -> skill3BodyFrames()
        4 -> when {
            skill4ChargePhase && skill4ChargeFrames.isNotEmpty() -> skill4ChargeFrames
            heroConfig.skill4BallMaxDurationSeconds > 0.0 && skill4OrbSpawned -> idleAnims
            else -> skillAnims
        }
        else -> skillAnims
    }

    private fun skillSlotBodyShouldLoop(): Boolean =
        activeSkillSlot == 2 && heroConfig.bodyAnimRules.skill2HealingAuraFollowPlayer

    private fun currentBodyAnimConfig(): AnimationConfig =
        if (state == CharacterState.SKILL) {
            AnimationConfig(skillSlotBodyFrames(), skillSlotBodyShouldLoop())
        } else {
            animationMap[state] ?: animationMap[CharacterState.IDLE]!!
        }

    private fun endSkillAction() {
        actionPlaying = false
        state = CharacterState.IDLE
        activeSkillSlot = 0
        skill4ChargePhase = false
        skill4ChargeIndex = 0
        skill4ChargeElapsed = 0.0
        skill4OrbSpawned = false
        skill4BallWaitElapsed = 0.0
        skill2AuraTimer = 0.0
        currentFrame = 0
        frameTime = 0.0
    }

    private fun updatePlayerHpBar(dt: Double) {
        if (!isPlayer) return
        if (hpBarDamageFlash > 0.0) hpBarDamageFlash -= dt
        val ratio = (health / maxHealth).coerceIn(0.0, 1.0)
        hpBarFill.width    = PlayerHpBar.WIDTH * ratio
        hpBarFill.colorMul = when {
            ratio > 0.5  -> Colors["#22cc44"]
            ratio > 0.25 -> Colors["#ffcc00"]
            else         -> Colors["#cc2222"]
        }
        hpBarBg.alpha = if (hpBarDamageFlash > 0.0) 1.0 else 0.75
    }

    private fun updateAnimation(dt: Double) {
        if (state == CharacterState.SKILL && skill4ChargePhase && heroConfig.bodyAnimRules.skill4ChargeBeforeBall && skill4ChargeFrames.isNotEmpty()) {
            if (skill4ChargeFrames.isNotEmpty()) {
                val idx = skill4ChargeIndex.coerceIn(0, skill4ChargeFrames.lastIndex)
                body.bitmap = skill4ChargeFrames[idx]
            }
            return
        }

        val config = currentBodyAnimConfig()
        frameTime += dt
        if (frameTime >= frameDuration) {
            frameTime = 0.0
            currentFrame++
            if (currentFrame >= config.frames.size)
                currentFrame = if (config.loop) 0 else config.frames.size - 1
        }
        if (config.frames.isNotEmpty()) {
            body.bitmap = config.frames[currentFrame.coerceIn(0, config.frames.lastIndex)]
        }
    }

    private var dbTop: SolidRect? = null
    private var dbBot: SolidRect? = null
    private var dbLft: SolidRect? = null
    private var dbRgt: SolidRect? = null

    private fun ensureDebugRects() {
        if (dbTop != null) return
        val t = 2.0
        dbTop = solidRect(1.0, t, Colors["#0044ff"]).also { debugOutline.addChild(it) }
        dbBot = solidRect(1.0, t, Colors["#0044ff"]).also { debugOutline.addChild(it) }
        dbLft = solidRect(t, 1.0, Colors["#0044ff"]).also { debugOutline.addChild(it) }
        dbRgt = solidRect(t, 1.0, Colors["#0044ff"]).also { debugOutline.addChild(it) }
    }

    private fun updateDebugOutline() {
        ensureDebugRects()
        val r  = hitboxRect()
        val w  = r.width.toDouble()
        val h  = r.height.toDouble()
        val ox = r.x.toDouble() - this.x
        val oy = r.y.toDouble() - this.y
        val t  = 2.0
        dbTop?.also { it.width = w; it.height = t; it.xy(ox, oy) }
        dbBot?.also { it.width = w; it.height = t; it.xy(ox, oy + h - t) }
        dbLft?.also { it.width = t; it.height = h; it.xy(ox, oy) }
        dbRgt?.also { it.width = t; it.height = h; it.xy(ox + w - t, oy) }
    }

    private fun move(direction: Double, dt: Double) {
        if (direction != 0.0) {
            this.x      = (this.x + direction * runningSpeed * dt)
                .coerceIn(0.0, Constants.SCREEN_WIDTH.toDouble())
            facingRight = direction > 0
            this.scaleX = if (facingRight) 1.0 else -1.0
        }
    }
    private fun jump() { if (isOnGround()) velocityY = jumpForce }

    private fun updatePhysics(dt: Double) {
        velocityY += gravity * dt
        this.y    += velocityY * dt
        if (this.y >= groundY) { this.y = groundY; velocityY = 0.0 }
    }
    fun isOnGround() = this.y >= groundY - 2.0

    override fun reset() {
        maxHealth     = heroConfig.maxHealth
        health        = maxHealth
        mana          = maxMana
        lastMaxHealthUpgradeCount = 0
        velocityY     = 0.0
        endSkillAction()
        facingRight   = true
        this.scaleX   = 1.0
        body.bitmap   = idleAnims[0]
        for (skill in allSkills) skill.resetCooldown()
    }

    private fun regenMana(dt: Double)   { mana = (mana + manaRegen * dt).coerceAtMost(maxMana) }
    private fun regenHealth(dt: Double) { health = (health + healthRegen * dt).coerceAtMost(maxHealth) }

    private fun spawnAttack(
        atkConfig:  AttackConfig,
        getEnemies: () -> List<Damageable>,
        container:  Container
    ) {
        val resolved = if (!atkConfig.damageEnemies && atkConfig.healSelfPerAnimationFrame > 0.0) {
            atkConfig.copy(followParent = this, followOffsetX = 0.0, followOffsetY = -characterHeight * 0.45)
        } else atkConfig

        val horizontalOffset = if (facingRight)  characterWidth * 0.6 else -characterWidth * 0.6
        val mirroredOffsetX  = if (facingRight)  resolved.offsetX    else -resolved.offsetX
        val spawnX = this.x + horizontalOffset + mirroredOffsetX
        val spawnY = this.y - characterHeight * 0.5 + resolved.offsetY

        val healer: ((Double) -> Unit)? =
            if (resolved.healSelfPerAnimationFrame > 0.0) { v -> heal(v) } else null

        AttackDisplay.spawn(resolved, spawnX, spawnY, getEnemies, container, facingRight, healer)
    }

    fun castHealingSkill(playerLevel: Int): Boolean {
        if (!healingSkillConfig.isUnlockedForUse(playerLevel)) return false
        if (!healingSkillConfig.isUsable(mana)) return false
        spendMana(healingSkillConfig.manaCost)
        healingSkillConfig.startCooldown()
        heal(healingSkillConfig.damage)
        return true
    }

    private data class PlayerInput(
        val direction: Double,
        val jump:      Boolean,
        val attack:    Boolean,
        val skill1:    Boolean,
        val skill2:    Boolean,
        val skill3:    Boolean,
        val skill4:    Boolean,
        val heal:      Boolean
    )

    private fun readInput(keys: InputKeys): PlayerInput {
        val moveLeft  = keys[Key.LEFT]  || keys[Key.A] || TouchInput.left
        val moveRight = keys[Key.RIGHT] || keys[Key.D] || TouchInput.right
        return PlayerInput(
            direction = when { moveRight -> charSpeed; moveLeft -> -charSpeed; else -> 0.0 },
            jump      = keys[Key.UP]    || keys[Key.SPACE] || keys[Key.W] || TouchInput.jump,
            attack    = keys[Key.E]     || TouchInput.attack,
            skill1    = keys[Key.Z]     || TouchInput.skill1,
            skill2    = keys[Key.X]     || TouchInput.skill2,
            skill3    = keys[Key.C]     || TouchInput.skill3,
            skill4    = keys[Key.V]     || TouchInput.skill4,
            heal      = keys[Key.Q]     || TouchInput.heal
        )
    }

    private fun spendMana(cost: Int) { mana = (mana - cost).coerceAtLeast(0.0) }

    fun update(
        dt:             Double,
        views:          Views,
        getEnemies:     () -> List<Damageable>,
        container:      Container,
        playerProgress: PlayerProgress
    ) {
        val input = readInput(views.input.keys)
        regenHealth(dt)
        regenMana(dt)

        for (skill in allSkills) skill.tickCooldown(dt)

        if (input.heal) {
            castHealingSkill(playerProgress.level)
        }

        val currentUpgradeCount = maxHealthSkillConfig.upgradeCount
        if (currentUpgradeCount > lastMaxHealthUpgradeCount && currentUpgradeCount > 0) {
            val upgradesDifference = currentUpgradeCount - lastMaxHealthUpgradeCount
            val healthIncrease = upgradesDifference * 10.0
            increaseMaxHealth(healthIncrease)
            lastMaxHealthUpgradeCount = currentUpgradeCount
        }

        if (actionPlaying && activeSkillSlot == 4 && skill4ChargePhase) {
            skill4ChargeElapsed += dt
            while (skill4ChargeElapsed >= heroConfig.skill4ChargeFrameDuration && skill4ChargeIndex < skill4ChargeFrames.size) {
                skill4ChargeElapsed -= heroConfig.skill4ChargeFrameDuration
                skill4ChargeIndex++
            }
            if (skill4ChargeIndex >= skill4ChargeFrames.size && !skill4OrbSpawned && skill4ChargeFrames.isNotEmpty()) {
                skill4ChargePhase = false
                spawnAttack(buildSkill4Config(), getEnemies, container)
                skill4OrbSpawned = true
                skill4BallWaitElapsed = 0.0
            }
        }

        if (actionPlaying && activeSkillSlot == 4 && skill4OrbSpawned && heroConfig.skill4BallMaxDurationSeconds > 0.0) {
            skill4BallWaitElapsed += dt
            if (skill4BallWaitElapsed >= heroConfig.skill4BallMaxDurationSeconds) {
                endSkillAction()
            }
        }

        if (actionPlaying && activeSkillSlot == 2 && heroConfig.skill2AuraTotalHeal > 0.0 && heroConfig.skill2AuraDurationSeconds > 0.0) {
            skill2AuraTimer += dt
            if (skill2AuraTimer >= heroConfig.skill2AuraDurationSeconds) {
                endSkillAction()
            }
        }

        if (!actionPlaying) {
            when {
                input.attack && isOnGround() && basicAttackSkill.isUsable(mana) -> {
                    spendMana(basicAttackSkill.manaCost)
                    basicAttackSkill.startCooldown()
                    actionPlaying = true
                    activeSkillSlot = 0
                    state         = CharacterState.ATTACKING
                    spawnAttack(buildBasicAtkConfig(), getEnemies, container)
                }

                isOnGround() && input.skill1 && skill1Config.isUnlockedForUse(playerProgress.level) && skill1Config.isUsable(mana) -> {
                    spendMana(skill1Config.manaCost)
                    skill1Config.startCooldown()
                    actionPlaying = true
                    activeSkillSlot = 1
                    state         = CharacterState.SKILL
                    spawnAttack(buildSkill1Config(), getEnemies, container)
                }

                isOnGround() && input.skill2 && skill2Config.isUnlockedForUse(playerProgress.level) && skill2Config.isUsable(mana) -> {
                    spendMana(skill2Config.manaCost)
                    skill2Config.startCooldown()
                    actionPlaying = true
                    activeSkillSlot = 2
                    skill2AuraTimer = 0.0
                    state         = CharacterState.SKILL
                    spawnAttack(buildSkill2Config(), getEnemies, container)
                }

                isOnGround() && input.skill3 && skill3Config.isUnlockedForUse(playerProgress.level) && skill3Config.isUsable(mana) -> {
                    spendMana(skill3Config.manaCost)
                    skill3Config.startCooldown()
                    actionPlaying = true
                    activeSkillSlot = 3
                    state         = CharacterState.SKILL
                    spawnAttack(buildSkill3Config(), getEnemies, container)
                }

                isOnGround() && input.skill4 && skill4Config.isUnlockedForUse(playerProgress.level) && skill4Config.isUsable(mana) -> {
                    spendMana(skill4Config.manaCost)
                    skill4Config.startCooldown()
                    actionPlaying = true
                    activeSkillSlot = 4
                    state         = CharacterState.SKILL
                    if (heroConfig.bodyAnimRules.skill4ChargeBeforeBall && skill4ChargeFrames.isNotEmpty()) {
                        skill4ChargePhase = true
                        skill4ChargeIndex = 0
                        skill4ChargeElapsed = 0.0
                        skill4OrbSpawned = false
                        skill4BallWaitElapsed = 0.0
                    } else {
                        spawnAttack(buildSkill4Config(), getEnemies, container)
                        skill4OrbSpawned = heroConfig.skill4BallMaxDurationSeconds > 0.0
                        skill4BallWaitElapsed = 0.0
                    }
                }

                input.jump -> jump()
            }
            move(input.direction, dt)
        }

        updatePhysics(dt)

        val useTimerSkill2 = actionPlaying && activeSkillSlot == 2 && heroConfig.skill2AuraTotalHeal > 0.0 && heroConfig.skill2AuraDurationSeconds > 0.0
        val useTimerSkill4Ball = actionPlaying && activeSkillSlot == 4 && skill4OrbSpawned && heroConfig.skill4BallMaxDurationSeconds > 0.0

        if (actionPlaying && !useTimerSkill2 && !useTimerSkill4Ball && !skill4ChargePhase) {
            val anim = currentBodyAnimConfig()
            if ((state == CharacterState.ATTACKING || state == CharacterState.SKILL) && !anim.loop && anim.frames.isNotEmpty()) {
                if (currentFrame >= anim.frames.size - 1) {
                    if (activeSkillSlot == 4 && !skill4OrbSpawned && skill4ChargeFrames.isEmpty()) {
                        endSkillAction()
                    } else if (activeSkillSlot != 4) {
                        endSkillAction()
                    }
                }
            }
        }

        if (!actionPlaying) {
            state = when {
                !isOnGround()          -> CharacterState.JUMPING
                input.direction != 0.0 -> CharacterState.RUNNING
                else                   -> CharacterState.IDLE
            }
        }

        updateAnimation(dt)
        updatePlayerHpBar(dt)
        debugOutline.visible = GameSettings.showHitbox
        if (GameSettings.showHitbox) updateDebugOutline()
    }
}
