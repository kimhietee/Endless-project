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

enum class CharacterState { IDLE, RUNNING, JUMPING, ATTACKING, SKILL }

data class AnimationConfig(
    val frames: List<BmpSlice>,
    val loop:   Boolean
)

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
    val characterHeight = 140.0
    private val body    = image(idleAnims[0])

    // -------------------------------------------------------
    // STATS
    // -------------------------------------------------------
    var maxHealth = 200.0; private set
    val maxMana   = 100.0
    var health    = maxHealth; private set
    var mana      = maxMana;   private set
    private val manaRegen   = 5.0
    private val healthRegen = 0.5

    // -------------------------------------------------------
    // OVERHEAD HP BAR (player only)
    // -------------------------------------------------------
    private companion object PlayerHpBar {
        const val WIDTH    = 80.0
        const val HEIGHT   = 8.0
        const val Y_OFFSET = 5.0
    }

    private lateinit var hpBarBg: SolidRect
    private lateinit var hpBarFill: SolidRect
    private var hpBarDamageFlash = 0.0

    // -------------------------------------------------------
    // DEBUG BODY OUTLINE
    // FIX: declared as lateinit and initialized inside init {}
    // to avoid the Android Kotlin compiler bug with container {}
    // assigned directly as a property initializer.
    // -------------------------------------------------------
    private lateinit var debugOutline: Container

    // -------------------------------------------------------
    // DAMAGEABLE
    // -------------------------------------------------------
    override fun takeDamage(amount: Double) {
        if (!isAlive()) return
        if (isPlayer && GameSettings.developerMode && GameSettings.godMode) return
        health = (health - amount).coerceAtLeast(0.0)
        if (isPlayer) hpBarDamageFlash = 5.0
    }

    /** Heal the player by the given amount. Caps at maxHealth. */
    fun heal(amount: Double) {
        health = (health + amount).coerceAtMost(maxHealth)
    }

    /** Increase max health permanently (e.g., from passive skill upgrade). */
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

    // -------------------------------------------------------
    // PER-SKILL CONFIGS
    // -------------------------------------------------------
    val basicAttackSkill = SkillConfig(
        name                  = "Basic Attack",
        cooldownMax           = 0.0,
        manaCost              = 0,
        damage                = 5.0,
        unlockLevel           = 1,
        requiresPointUnlock   = false,
        damagePerUpgrade      = 1.0,
        maxUpgrades           = 10
    )
    val skill1Config = SkillConfig(
        name                        = "Skill 1",
        cooldownMax                 = 4.0,
        manaCost                    = 20,
        damage                      = 10.0,
        unlockLevel                 = 4,
        damagePerUpgrade            = 3.0,
        cooldownReductionPerUpgrade = 0.3,
        maxUpgrades                 = 5
    )
    val skill2Config = SkillConfig(
        name                        = "Skill 2",
        cooldownMax                 = 10.0,
        manaCost                    = 40,
        damage                      = 15.0,
        unlockLevel                 = 8,
        damagePerUpgrade            = 3.0,
        cooldownReductionPerUpgrade = 0.5,
        maxUpgrades                 = 5
    )
    val skill3Config = SkillConfig(
        name                        = "Skill 3",
        cooldownMax                 = 12.0,
        manaCost                    = 50,
        damage                      = 30.0,
        unlockLevel                 = 12,
        damagePerUpgrade            = 4.0,
        cooldownReductionPerUpgrade = 0.5,
        maxUpgrades                 = 5
    )
    val skill4Config = SkillConfig(
        name                        = "Skill 4",
        cooldownMax                 = 20.0,
        manaCost                    = 100,
        damage                      = 50.0,
        unlockLevel                 = 18,
        damagePerUpgrade            = 5.0,
        cooldownReductionPerUpgrade = 1.0,
        maxUpgrades                 = 5
    )

    val healingSkillConfig = SkillConfig(
        name                        = "Healing",
        cooldownMax                 = 3.0,
        manaCost                    = 30,
        damage                      = 10.0,
        unlockLevel                 = 1,
        requiresPointUnlock         = true,
        damagePerUpgrade            = 2.0,
        cooldownReductionPerUpgrade = 0.0,
        minCooldown                 = 3.0,
        maxUpgrades                 = 10
    )

    val maxHealthSkillConfig = SkillConfig(
        name                        = "Max Health",
        cooldownMax                 = 0.0,
        manaCost                    = 0,
        damage                      = 0.0,
        unlockLevel                 = 1,
        requiresPointUnlock         = true,
        damagePerUpgrade            = 0.0,
        cooldownReductionPerUpgrade = 0.0,
        maxUpgrades                 = 10
    )

    private var lastMaxHealthUpgradeCount = 0

    val allSkills = listOf(basicAttackSkill, skill1Config, skill2Config, skill3Config, skill4Config, healingSkillConfig, maxHealthSkillConfig)

    // -------------------------------------------------------
    // ATTACK CONFIG BUILDERS
    // -------------------------------------------------------
    private fun buildBasicAtkConfig() = AttackConfig(
        frames          = basicAtkFrames,
        frameDuration   = 0.08,
        damage          = basicAttackSkill.damage,
        moving          = true,
        speed           = 0.0,
        hitboxScaleX    = 1.1,
        hitboxScaleY    = 0.9,
        repeatAnimation = 2,
        displayScale    = 2.0,
        offsetX         = -20.0,
        offsetY         = 30.0
    )
    private fun buildSkill1Config() = AttackConfig(
        frames          = skill1Frames,
        frameDuration   = 0.07,
        damage          = skill1Config.damage,
        moving          = true,
        speed           = if (facingRight) 400.0 else -400.0,
        hitboxScaleX    = 0.8,
        hitboxScaleY    = 0.8,
        repeatAnimation = 1,
        displayScale    = 3.0,
        offsetX         = -130.0,
        offsetY         = 25.0
    )

    val repeatani = 5
    private fun buildSkill2Config() = AttackConfig(
        frames          = skill2Frames,
        frameDuration   = 0.07,
        damage          = skill2Config.damage * repeatani,
        moving          = false,
        speed           = 0.0,
        hitboxScaleX    = 0.4,
        hitboxScaleY    = 0.5,
        repeatAnimation = 5,
        displayScale    = 0.4,
        offsetX         = -20.0,
        offsetY         = 5.0
    )
    private fun buildSkill3Config() = AttackConfig(
        frames          = skill3Frames,
        frameDuration   = 0.06,
        damage          = skill3Config.damage,
        moving          = false,
        speed           = 0.0,
        hitboxScaleX    = 0.5,
        hitboxScaleY    = 0.5,
        repeatAnimation = 1,
        displayScale    = 0.3,
        offsetX         = -20.0,
        offsetY         = 15.0
    )
    private fun buildSkill4Config() = AttackConfig(
        frames          = skill4Frames,
        frameDuration   = 0.08,
        damage          = skill4Config.damage,
        moving          = false,
        speed           = 0.0,
        hitboxScaleX    = 1.0,
        hitboxScaleY    = 1.0,
        repeatAnimation = 1,
        displayScale    = 1.2,
        offsetX         = 50.0,
        offsetY         = -110.0
    )

    // -------------------------------------------------------
    // STATE
    // -------------------------------------------------------
    private var state: CharacterState = CharacterState.IDLE
        set(value) {
            if (value != field) { currentFrame = 0; frameTime = 0.0; field = value }
        }
    var facingRight = true
        private set

    private val runningSpeed  = 200.0
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

    // -------------------------------------------------------
    // INIT
    // -------------------------------------------------------
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

        // FIX: initialize debugOutline here instead of as a property initializer
        // to avoid the Android Kotlin compiler internal error in ExternalPackageParentPatcherLowering
        debugOutline = container {
            val t = 2.0
            solidRect(1.0, t, Colors["#0044ff"]).name("top")
            solidRect(1.0, t, Colors["#0044ff"]).name("bot")
            solidRect(t, 1.0, Colors["#0044ff"]).name("lft")
            solidRect(t, 1.0, Colors["#0044ff"]).name("rgt")
            visible = false
        }
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
    private fun updateDebugOutline() {
        val c  = debugOutline
        val r  = hitboxRect()
        val w  = r.width.toDouble()
        val h  = r.height.toDouble()
        val ox = r.x.toDouble() - this.x
        val oy = r.y.toDouble() - this.y
        val t  = 2.0
        (c.children.firstOrNull { it.name == "top" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy) }
        (c.children.firstOrNull { it.name == "bot" } as? SolidRect)?.also { it.width = w; it.height = t; it.xy(ox, oy + h - t) }
        (c.children.firstOrNull { it.name == "lft" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox, oy) }
        (c.children.firstOrNull { it.name == "rgt" } as? SolidRect)?.also { it.width = t; it.height = h; it.xy(ox + w - t, oy) }
    }

    // -------------------------------------------------------
    // MOVEMENT
    // -------------------------------------------------------
    private fun move(direction: Double, dt: Double) {
        if (direction != 0.0) {
            this.x      = (this.x + direction * runningSpeed * dt)
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
    // RESET
    // -------------------------------------------------------
    override fun reset() {
        maxHealth     = 200.0
        health        = maxHealth
        mana          = maxMana
        lastMaxHealthUpgradeCount = 0
        velocityY     = 0.0
        currentFrame  = 0
        frameTime     = 0.0
        actionPlaying = false
        state         = CharacterState.IDLE
        facingRight   = true
        this.scaleX   = 1.0
        body.bitmap   = idleAnims[0]
        for (skill in allSkills) skill.resetCooldown()
    }

    // -------------------------------------------------------
    // MANA
    // -------------------------------------------------------
    private fun hasMana(cost: Int)      = mana >= cost
    private fun spendMana(cost: Int)    { mana = (mana - cost).coerceAtLeast(0.0) }
    private fun regenMana(dt: Double)   { mana = (mana + manaRegen * dt).coerceAtMost(maxMana) }
    private fun regenHealth(dt: Double) { health = (health + healthRegen * dt).coerceAtMost(maxHealth) }

    // -------------------------------------------------------
    // ATTACK SPAWNING
    // -------------------------------------------------------
    private fun spawnAttack(
        atkConfig:  AttackConfig,
        getEnemies: () -> List<Damageable>,
        container:  Container
    ) {
        val horizontalOffset = if (facingRight)  characterWidth * 0.6 else -characterWidth * 0.6
        val mirroredOffsetX  = if (facingRight)  atkConfig.offsetX    else -atkConfig.offsetX
        val spawnX = this.x + horizontalOffset + mirroredOffsetX
        val spawnY = this.y - characterHeight * 0.5 + atkConfig.offsetY
        AttackDisplay.spawn(atkConfig, spawnX, spawnY, getEnemies, container, movingRight = facingRight)
    }

    // -------------------------------------------------------
    // HEALING SKILL
    // -------------------------------------------------------
    fun castHealingSkill(playerLevel: Int): Boolean {
        if (!healingSkillConfig.isUnlockedForUse(playerLevel)) return false
        if (!healingSkillConfig.isUsable(mana)) return false
        spendMana(healingSkillConfig.manaCost)
        healingSkillConfig.startCooldown()
        heal(healingSkillConfig.damage)
        return true
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
            jump      = keys[Key.UP]    || keys[Key.SPACE] || keys[Key.W] || TouchInput.jump,
            attack    = keys[Key.E]     || TouchInput.attack,
            skill1    = keys[Key.Z]     || TouchInput.skill1,
            skill2    = keys[Key.X]     || TouchInput.skill2,
            skill3    = keys[Key.C]     || TouchInput.skill3,
            skill4    = keys[Key.V]     || TouchInput.skill4,
        )
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
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

        val currentUpgradeCount = maxHealthSkillConfig.upgradeCount
        if (currentUpgradeCount > lastMaxHealthUpgradeCount && currentUpgradeCount > 0) {
            val upgradesDifference = currentUpgradeCount - lastMaxHealthUpgradeCount
            val healthIncrease = upgradesDifference * 10.0
            increaseMaxHealth(healthIncrease)
            lastMaxHealthUpgradeCount = currentUpgradeCount
        }

        if (!actionPlaying) {
            when {
                input.attack && isOnGround() && basicAttackSkill.isUsable(mana) -> {
                    spendMana(basicAttackSkill.manaCost)
                    basicAttackSkill.startCooldown()
                    actionPlaying = true
                    state         = CharacterState.ATTACKING
                    spawnAttack(buildBasicAtkConfig(), getEnemies, container)
                }

                isOnGround() && (input.skill1 || input.skill2 || input.skill3 || input.skill4) -> {
                    val (skill, cfg) = when {
                        input.skill1 -> skill1Config to buildSkill1Config()
                        input.skill2 -> skill2Config to buildSkill2Config()
                        input.skill3 -> skill3Config to buildSkill3Config()
                        else         -> skill4Config to buildSkill4Config()
                    }
                    if (skill.isUnlockedForUse(playerProgress.level) && skill.isUsable(mana)) {
                        spendMana(skill.manaCost)
                        skill.startCooldown()
                        actionPlaying = true
                        state         = CharacterState.SKILL
                        spawnAttack(cfg, getEnemies, container)
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
                state         = CharacterState.IDLE
            }
        } else {
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