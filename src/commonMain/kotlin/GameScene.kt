import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.image.color.Colors
import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.time.seconds
import korlibs.math.geom.Point

class GameScene : Scene() {

    override suspend fun SContainer.sceneMain() {

        // -------------------------------------------------------
        // BACKGROUND
        // -------------------------------------------------------
        val bgSlice = resourcesVfs["bg/background.png"].readBitmapSlice()
        val bg = image(bgSlice).apply {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
            y         = Constants.GROUND - Constants.SCREEN_HEIGHT
        }
        addChild(bg)

        // -------------------------------------------------------
        // CHARACTER ASSETS
        // -------------------------------------------------------
        val idleFrames   = loadFrames("fireWizard", "idle_pngs",    "image_0-",  7)
        val runFrames    = loadFrames("fireWizard", "run_pngs",      "Run_",      8)
        val jumpFrames   = loadFrames("fireWizard", "jump_pngs",     "Jump_",     6)
        val attackFrames = loadFrames("fireWizard", "slash_pngs",    "Attack_1_", 10)
        val skillFrames  = loadFrames("fireWizard", "fireball_pngs", "image_0-",  8)

        val basicAtkFrames = loadFrames("fireWizard", "slash_pngs",    "Attack_1_", 10)
        val skill1Frames   = loadFrames("fireWizard", "fireball_pngs", "image_0-",   8)
        val skill2Frames   = loadFrames("fireWizard", "fireball_pngs", "image_0-",   8)
        val skill3Frames   = loadFrames("fireWizard", "fireball_pngs", "image_0-",   8)
        val skill4Frames   = loadFrames("fireWizard", "fireball_pngs", "image_0-",   8)

        // -------------------------------------------------------
        // PLAYER
        // -------------------------------------------------------
        val player = Character(
            isPlayer       = true,
            idleAnims      = idleFrames,
            runAnims       = runFrames,
            jumpAnims      = jumpFrames,
            attackAnims    = attackFrames,
            skillAnims     = skillFrames,
            basicAtkFrames = basicAtkFrames,
            skill1Frames   = skill1Frames,
            skill2Frames   = skill2Frames,
            skill3Frames   = skill3Frames,
            skill4Frames   = skill4Frames
        )
        addChild(player)
        player.xy(100.0, Constants.GROUND)

        // -------------------------------------------------------
        // ENEMY CONFIGS
        // NOTE: for ranged attackDisplayConfig, speed is ALWAYS positive here.
        // Enemy.spawnAttack() flips it based on facingRight at spawn time.
        // -------------------------------------------------------
        val enemyAtkFrames    = loadFrames("fireWizard", "slash_pngs",    "Attack_1_", 10)
        val enemyRangedFrames = loadFrames("fireWizard", "fireball_pngs", "image_0-",   8)

        val meleeEnemyConfig = EnemyConfig(
            idleConfig   = FrameConfig("fireWizard/idle_pngs",  "image_0-",  startIndex = 0, count = 7),
            runConfig    = FrameConfig("fireWizard/run_pngs",   "Run_",      startIndex = 0, count = 8),
            attackConfig = FrameConfig("fireWizard/slash_pngs", "Attack_1_", startIndex = 0, count = 10),
            deathConfig  = FrameConfig("fireWizard/jump_pngs",  "Jump_",     startIndex = 0, count = 6),
            attackDisplayConfig = AttackConfig(
                frames          = enemyAtkFrames,
                frameDuration   = 0.08,
                damage          = 10.0,
                moving          = false,   // melee = stationary
                speed           = 0.0,
                hitboxScaleX    = 0.7,
                hitboxScaleY    = 0.7,
                repeatAnimation = 1
            ),
            behavior    = EnemyBehavior.MELEE,
            attackRange = 50.0,
            moveSpeed   = 130.0
        )

        val rangedEnemyConfig = EnemyConfig(
            idleConfig   = FrameConfig("fireWizard/idle_pngs",     "image_0-", startIndex = 0, count = 7),
            runConfig    = FrameConfig("fireWizard/run_pngs",      "Run_",     startIndex = 0, count = 8),
            attackConfig = FrameConfig("fireWizard/fireball_pngs", "image_0-", startIndex = 0, count = 8),
            deathConfig  = FrameConfig("fireWizard/jump_pngs",     "Jump_",    startIndex = 0, count = 6),
            attackDisplayConfig = AttackConfig(
                frames          = enemyRangedFrames,
                frameDuration   = 0.10,
                damage          = 8.0,
                moving          = true,
                speed           = 300.0,  // POSITIVE — direction flipped at spawn by Enemy
                hitboxScaleX    = 0.6,
                hitboxScaleY    = 0.6,
                repeatAnimation = 1
            ),
            behavior    = EnemyBehavior.RANGED,
            attackRange = 300.0,
            moveSpeed   = 100.0
        )

        val enemies = mutableListOf<Enemy>()
        val enemy1  = Enemy.create(meleeEnemyConfig).also  { it.xy(900.0,  Constants.GROUND); addChild(it); enemies.add(it) }
        val enemy2  = Enemy.create(rangedEnemyConfig).also { it.xy(1100.0, Constants.GROUND); addChild(it); enemies.add(it) }

        val hud = HUD(player)
        addChild(hud)

        // -------------------------------------------------------
        // BUTTON ASSETS
        // -------------------------------------------------------
        val leftSlice   = resourcesVfs["ui/buttons/btn_left.png"].readBitmapSlice()
        val rightSlice  = resourcesVfs["ui/buttons/btn_right.png"].readBitmapSlice()
        val jumpSlice   = resourcesVfs["ui/buttons/btn_jump.png"].readBitmapSlice()
        val attackSlice = resourcesVfs["ui/buttons/btn_attack.png"].readBitmapSlice()
        val skill1Slice = resourcesVfs["skill_icons/fire_wizard/1.png"].readBitmapSlice()
        val skill2Slice = resourcesVfs["skill_icons/fire_wizard/2.png"].readBitmapSlice()
        val skill3Slice = resourcesVfs["skill_icons/fire_wizard/3.png"].readBitmapSlice()
        val skill4Slice = resourcesVfs["skill_icons/fire_wizard/4.png"].readBitmapSlice()

        // -------------------------------------------------------
        // BUTTON LAYOUT
        // -------------------------------------------------------
        val btnSize = 100.0
        val gap     =   8.0
        val rowY    = Constants.GROUND + (Constants.SCREEN_HEIGHT - Constants.GROUND - btnSize) / 2.0

        val leftBtn   = TouchButton(btnSize, btnSize, leftSlice  ).xy(20.0,                            rowY)
        val rightBtn  = TouchButton(btnSize, btnSize, rightSlice ).xy(20.0 + btnSize + gap,            rowY)
        val skillsX   = 20.0 + (btnSize + gap) * 2
        val skillBtn1 = TouchButton(btnSize, btnSize, skill1Slice).xy(skillsX + (btnSize + gap) * 0,   rowY)
        val skillBtn2 = TouchButton(btnSize, btnSize, skill2Slice).xy(skillsX + (btnSize + gap) * 1,   rowY)
        val skillBtn3 = TouchButton(btnSize, btnSize, skill3Slice).xy(skillsX + (btnSize + gap) * 2,   rowY)
        val skillBtn4 = TouchButton(btnSize, btnSize, skill4Slice).xy(skillsX + (btnSize + gap) * 3,   rowY)
        val jumpX     = Constants.SCREEN_WIDTH - 20.0 - btnSize
        val attackX   = jumpX - gap - btnSize
        val jumpBtn   = TouchButton(btnSize, btnSize, jumpSlice  ).xy(jumpX,   rowY)
        val attackBtn = TouchButton(btnSize, btnSize, attackSlice).xy(attackX, rowY)

        listOf(leftBtn, rightBtn, skillBtn1, skillBtn2, skillBtn3, skillBtn4, attackBtn, jumpBtn)
            .forEach { addChild(it) }

        // -------------------------------------------------------
        // MAIN UPDATE LOOP
        // -------------------------------------------------------
        addUpdater { dt ->
            val dtSec = dt.seconds

            // reset touch
            TouchInput.left   = false; TouchInput.right  = false
            TouchInput.jump   = false; TouchInput.attack = false
            TouchInput.skill1 = false; TouchInput.skill2 = false
            TouchInput.skill3 = false; TouchInput.skill4 = false

            for (touch in views.input.activeTouches) {
                val point = Point(touch.x, touch.y)
                if (leftBtn.hitTest(point)   != null) TouchInput.left   = true
                if (rightBtn.hitTest(point)  != null) TouchInput.right  = true
                if (jumpBtn.hitTest(point)   != null) TouchInput.jump   = true
                if (attackBtn.hitTest(point) != null) TouchInput.attack = true
                if (skillBtn1.hitTest(point) != null) TouchInput.skill1 = true
                if (skillBtn2.hitTest(point) != null) TouchInput.skill2 = true
                if (skillBtn3.hitTest(point) != null) TouchInput.skill3 = true
                if (skillBtn4.hitTest(point) != null) TouchInput.skill4 = true
            }

            leftBtn.isPressed   = TouchInput.left;   rightBtn.isPressed  = TouchInput.right
            jumpBtn.isPressed   = TouchInput.jump;   attackBtn.isPressed = TouchInput.attack
            skillBtn1.isPressed = TouchInput.skill1; skillBtn2.isPressed = TouchInput.skill2
            skillBtn3.isPressed = TouchInput.skill3; skillBtn4.isPressed = TouchInput.skill4

            player.update(dtSec, views, enemies.filterIsInstance<Damageable>(), this)

            val toRemove = mutableListOf<Enemy>()
            for (enemy in enemies) {
                enemy.update(dtSec, player.x, listOf(player), this)
                if (enemy.shouldRemove) toRemove.add(enemy)
            }
            toRemove.forEach { removeChild(it); enemies.remove(it) }

            AttackDisplay.updateAll(dtSec)
            hud.update()
        }
    }

    suspend fun loadFrames(
        hero:   String,
        folder: String,
        prefix: String,
        count:  Int
    ): List<BmpSlice> = (0 until count).map { i ->
        resourcesVfs["$hero/$folder/${prefix}$i.png"].readBitmapSlice()
    }
}
