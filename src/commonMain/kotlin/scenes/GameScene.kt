package scenes

import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.input.*
import korlibs.image.color.Colors
import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.time.seconds
import korlibs.math.geom.Point
import korlibs.io.async.launchImmediately
import korlibs.event.Key
import entities.*
import ui.*
import managers.*
import utils.*

class GameScene : Scene() {

    private var isPaused = false
    private var gameTime = 0.0

    override suspend fun SContainer.sceneMain() {

        // -------------------------------------------------------
        // BACKGROUND
        // -------------------------------------------------------
        val bgSlice = GameAssets.bgSlice
        val bg = image(bgSlice).apply {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
            y         = Constants.GROUND - Constants.SCREEN_HEIGHT
        }
        addChild(bg)

//        If your files are zero-padded like frame_00.png, frame_01.png, add:
//        kotlin    zeroPad = 2   // pads to 2 digits: 0 → "00"


//        // PNG SEQUENCE
//        val basicAtkFrames = GameAssets.loadFrames(FrameConfig(
//            folder     = "your_folder",
//            prefix     = "slash_",
//            startIndex = 0,
//            count      = 6
//        ))
//
//        // SPRITESHEET
//        val skill1Frames = GameAssets.loadFrames(FrameConfig(
//            folder = "your_folder",
//            sheet  = SpriteSheetConfig(fileName = "fireball_sheet", columns = 8, rows = 1),
//            count  = 8
//        ))

        // -------------------------------------------------------
        // CHARACTER ASSETS (from cached GameAssets)
        // -------------------------------------------------------
        val idleFrames   = GameAssets.idleFrames
        val runFrames    = GameAssets.runFrames
        val jumpFrames   = GameAssets.jumpFrames
        val attackFrames = GameAssets.attackFrames
        val skillFrames  = GameAssets.skillFrames

//        val basicAtkFrames = GameAssets.loadFrames(FrameConfig("fireWizard/slash_pngs",    "Attack_1_", startIndex = 0, count = 10))
        val basicAtkFrames = GameAssets.loadFrames(FrameConfig(
            folder = "fireWizard/skills/slash",
            sheet  = SpriteSheetConfig(fileName = "playerSlash", columns = 5, rows = 1),
            count  = 5
        ))
        val skill1Frames   = GameAssets.loadFrames(FrameConfig("fireWizard/skills/skill_1", "tile", startIndex = 0, count = 12, zeroPad = 3))
        val skill2Frames   = GameAssets.loadFrames(FrameConfig("fireWizard/skills/skill_2", "",     startIndex = 0, count = 53, zeroPad = 2))
        val skill3Frames   = GameAssets.loadFrames(FrameConfig("fireWizard/skills/skill_3", "png_",  startIndex = 0, count = 34, zeroPad = 2))
        val skill4Frames   = GameAssets.loadFrames(FrameConfig("fireWizard/skills/skill_4", "",     startIndex = 0, count = 28, zeroPad = 2))

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

        val progress = PlayerProgress()
        addChild(player)
        player.xy(100.0, Constants.GROUND)

        val enemyContainer = container()
        val spawner = EnemySpawner(enemyContainer)

        val hud = HUD(player, progress)
        addChild(hud)


        GameAssets.load()

        WaveSystem.apply(spawner)

//         spawner.schedule(
//             // Wave 1 (0:00 - 1:30)
//             SpawnEvent(1.0, "skeleton", 1000.0),
//             SpawnEvent(5.0, "skeleton", 1000.0, 2, 30.0),


//             SpawnEvent(10.0, "skeleton", 1000.0, 1, 10.0),
//             SpawnEvent(10.0, "skeleton_archer", 950.0),

//             SpawnEvent(15.0, "skeleton", 1000.0, 1, 20.0),
//             SpawnEvent(15.0, "skeleton_archer", 1000.0, 1, 20.0),
//             SpawnEvent(17.0, "skeleton", 1000.0, 1, 20.0),


//             SpawnEvent(20.0, "skeleton_spearman", 900.0),
//             SpawnEvent(20.0, "skeleton", 1000.0, 1, 20.0),
//             SpawnEvent(25.0, "skeleton_archer", 1000.0, 2, 50.0),

//             SpawnEvent(27.0, "skeleton_spearman", 1000.0),
//             SpawnEvent(30.0, "skeleton_archer", 1100.0, 1, 20.0),

//             SpawnEvent(43.0, "skeleton_spearman", 900.0, 1, 50.0),
//             SpawnEvent(43.0, "skeleton", 1000.0, 2, 40.0),
//             SpawnEvent(43.0, "skeleton_archer", 1000.0, 2, 50.0),

//             SpawnEvent(45.0, "skeleton", 1000.0, 1, 40.0),

//             SpawnEvent(45.0, "skeleton_archer", 1100.0, 1, 60.0),
//             SpawnEvent(50.0, "skeleton_spearman", 1000.0, 1, 20.0),
//             SpawnEvent(50.0, "skeleton", 1000.0, 1, 20.0),

//             SpawnEvent(60.0, "skeleton_boss", 640.0),



//             // Wave 2 (1:30 - 2:30)
//             SpawnEvent(90.0 + 1.0, "skeleton", 1000.0),

//             SpawnEvent(90.0 + 4.0, "skeleton_spearman", 900.0),
//             SpawnEvent(90.0 + 4.0, "skeleton", 1000.0, 2, 20.0),
//             SpawnEvent(90.0 + 4.0, "skeleton_archer", 1000.0, 2, 50.0),

//             SpawnEvent(90.0 + 12.0, "wolf1", 900.0),
//             SpawnEvent(90.0 + 15.0, "skeleton_archer", 1000.0, 2, 50.0),

//             SpawnEvent(90.0 + 25.0, "skeleton_spearman", 1100.0, 1, 40.0),
//             SpawnEvent(90.0 + 25.0, "skeleton", 1000.0, 2, 20.0),
//             SpawnEvent(90.0 + 25.0, "skeleton_archer", 1000.0, 1, 50.0),

//             SpawnEvent(90.0 + 35.0, "skeleton", 100.0, 1, 50.0),
//             SpawnEvent(90.0 + 35.0, "skeleton_archer", 100.0, 1, 40.0),
//             SpawnEvent(90.0 + 35.0, "skeleton", 1100.0, 1, 50.0),
//             SpawnEvent(90.0 + 35.0, "skeleton_archer", 1100.0, 1, 40.0),

//             SpawnEvent(90.0 + 40.0, "wolf1", 50.0),
//             SpawnEvent(90.0 + 40.0, "skeleton_archer", 50.0, 1, 20.0),
//             SpawnEvent(90.0 + 40.0, "skeleton_spearman", 50.0, 1, 10.0),
//             SpawnEvent(90.0 + 42.0, "skeleton", 50.0, 1, 50.0),

//             SpawnEvent(90.0 + 45.0, "skeleton_archer", 1000.0, 1, 20.0),
//             SpawnEvent(90.0 + 45.0, "skeleton_spearman", 1000.0, 1, 10.0),
//             SpawnEvent(90.0 + 47.0, "skeleton", 1100.0, 2, 50.0),

//             SpawnEvent(90.0 + 60.0, "skeleton_boss", 640.0 * 1.3),
//             SpawnEvent(90.0 + 68.0, "skeleton", 100.0, 2, 100.0),
//             SpawnEvent(90.0 + 68.0, "skeleton_archer", 100.0, 2, 30.0),
//             SpawnEvent(90.0 + 68.0, "skeleton_spearman", 100.0, 1, 30.0),

//             SpawnEvent(90.0 + 80.0, "skeleton", 1000.0, 2, 30.0),
//             SpawnEvent(90.0 + 80.0, "skeleton_archer", 1000.0, 2, 30.0),

//             // Wave 3 (1:30 - 2:30)
//             SpawnEvent(150.0 + 80.0, "skeleton", 1000.0, 2, 30.0),

//             // SpawnEvent(150.0 + 10.0, "wolf3", 1200.0),







//             // Add spearmen
// //            SpawnEvent(15.0, "skeleton_spearman", 900.0),
// //            SpawnEvent(18.0, "skeleton_spearman", 800.0),
// //
// //            // Add archers from far away
// //            SpawnEvent(10.0, "skeleton_archer", 950.0),
// //            SpawnEvent(15.0, "skeleton_archer", 750.0),
// //
// //            // Add skeleton boss
// //            SpawnEvent(25.0, "skeleton_boss", 640.0),
// //
// //            // Add wolves
// //            SpawnEvent(35.0, "wolf1", 900.0),
// //            SpawnEvent(38.0, "wolf2", 850.0),
// //            SpawnEvent(40.0, "wolf3", 800.0),
// //
// //            // Mixed waves
// //            SpawnEvent(50.0, "skeleton", 900.0),
// //            SpawnEvent(50.5, "wolf1", 850.0),
// //            SpawnEvent(51.0, "skeleton_archer", 950.0),
// //            SpawnEvent(52.0, "skeleton_spearman", 800.0)
//         )

        

        // -------------------------------------------------------
        // BUTTON ASSETS
        // -------------------------------------------------------
        val leftSlice   = GameAssets.leftSlice
        val rightSlice  = GameAssets.rightSlice
        val jumpSlice   = GameAssets.jumpSlice
        val attackSlice = GameAssets.attackSlice
        val skill1Slice = GameAssets.skill1Slice
        val skill2Slice = GameAssets.skill2Slice
        val skill3Slice = GameAssets.skill3Slice
        val skill4Slice = GameAssets.skill4Slice
        val healingRamenSlice = GameAssets.healingRamenSlice
        val healingBentoSlice = GameAssets.healingBentoSlice
        val maxHealthSlice = GameAssets.maxHealthSlice
        val pauseSlice  = GameAssets.pauseSlice
        val upgradeSlice = GameAssets.upgradeSlice

        // -------------------------------------------------------
        // BUTTON LAYOUT
        // -------------------------------------------------------
        val btnSize = 100.0
        val gap     =   8.0
        val rowY    = Constants.GROUND + (Constants.SCREEN_HEIGHT - Constants.GROUND - btnSize) / 2.0

        // Right-side positions
        val jumpX      = Constants.SCREEN_WIDTH - 20.0 - btnSize
        val attackX    = jumpX - gap - btnSize
        val healingX   = attackX - gap - btnSize
        val maxHealthX = healingX - gap - btnSize

        val leftBtn   = TouchButton(btnSize, btnSize, leftSlice  ).xy(20.0,                            rowY)
        val rightBtn  = TouchButton(btnSize, btnSize, rightSlice ).xy(20.0 + btnSize + gap,            rowY)
        val skillsX   = 20.0 + (btnSize + gap) * 2
        val skillBtn1 = SkillButton(btnSize, btnSize, skill1Slice, upgradeSlice, player.skill1Config).xy(skillsX + (btnSize + gap) * 0, rowY)
        val skillBtn2 = SkillButton(btnSize, btnSize, skill2Slice, upgradeSlice, player.skill2Config).xy(skillsX + (btnSize + gap) * 1, rowY)
        val skillBtn3 = SkillButton(btnSize, btnSize, skill3Slice, upgradeSlice, player.skill3Config).xy(skillsX + (btnSize + gap) * 2, rowY)
        val skillBtn4 = SkillButton(btnSize, btnSize, skill4Slice, upgradeSlice, player.skill4Config).xy(skillsX + (btnSize + gap) * 3, rowY)

        val attackBtn = SkillButton(btnSize, btnSize, attackSlice, upgradeSlice, player.basicAttackSkill).xy(attackX, rowY)
        val skillBtnHealing = SkillButton(btnSize, btnSize, healingRamenSlice, upgradeSlice, player.healingSkillConfig).xy(healingX, rowY)
        val skillBtnMaxHealth = SkillButton(btnSize, btnSize, maxHealthSlice, upgradeSlice, player.maxHealthSkillConfig).xy(maxHealthX, rowY)
        val jumpBtn   = TouchButton(btnSize, btnSize, jumpSlice  ).xy(jumpX,   rowY)

        val pauseBtnWidth = 120.0
        val pauseBtnHeight = 50.0
        val pauseBtn     = TouchButton(pauseBtnWidth, pauseBtnHeight, pauseSlice).xy(Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth, 20.0)

        listOf(leftBtn, rightBtn, skillBtn1, skillBtn2, skillBtn3, skillBtn4, attackBtn, skillBtnHealing, skillBtnMaxHealth, jumpBtn, pauseBtn)
            .forEach { addChild(it) }

        // -------------------------------------------------------
        // DEVELOPER MODE BUTTONS
        // Positioned to the left of the pause button.
        // Visibility is controlled each frame inside addUpdater.
        // -------------------------------------------------------
        val devBtnW = 120.0
        val devBtnH = 50.0
        val devBtnY = 20.0
        // Time-skip amount for "Next Wave" — jump the spawner forward by this many seconds
        val NEXT_WAVE_TIME_SKIP = 90.0

        // "Level Up" button — gives the player one free XP-level immediately
        val levelUpBtn = container {
            solidRect(devBtnW, devBtnH, korlibs.image.color.RGBA(30, 100, 30, 220)) {}
            text("Level Up", textSize = 16.0, color = Colors.WHITE, font = GameAssets.customFont) {
                xy(8.0, (devBtnH - fontSize) / 2.0)
            }
            onClick {
                if (GameSettings.developerMode && !isPaused && player.isAlive()) {
                    val needed = (progress.xpForNextLevel() - progress.currentXp + 1.0).coerceAtLeast(1.0)
                    if (!progress.isMaxLevel()) progress.addXp(needed)
                }
            }
            visible = false
            xy(Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth - 8.0 - devBtnW, devBtnY)
        }
        addChild(levelUpBtn)

        // "Next Wave" button — advances spawner time so upcoming enemies appear sooner
        val nextWaveBtn = container {
            solidRect(devBtnW, devBtnH, korlibs.image.color.RGBA(100, 50, 10, 220)) {}
            text("Next Wave", textSize = 16.0, color = Colors.WHITE, font = GameAssets.customFont) {
                xy(6.0, (devBtnH - fontSize) / 2.0)
            }
            onClick {
                if (GameSettings.developerMode && !isPaused && player.isAlive()) {
                    spawner.advanceTime(NEXT_WAVE_TIME_SKIP)
                    gameTime += NEXT_WAVE_TIME_SKIP
                }
            }
            visible = false
            xy(Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth - 8.0 - devBtnW * 2 - 8.0, devBtnY)
        }
        addChild(nextWaveBtn)

        // "God Mode" button — player takes no damage
        val godModeBtn = container {
            val bg = solidRect(devBtnW, devBtnH, korlibs.image.color.RGBA(100, 10, 10, 220)) {}
            val txt = text("God: OFF", textSize = 16.0, color = Colors.WHITE, font = GameAssets.customFont) {
                xy(6.0, (devBtnH - fontSize) / 2.0)
            }
            onClick {
                if (GameSettings.developerMode && !isPaused && player.isAlive()) {
                    GameSettings.godMode = !GameSettings.godMode
                    if (GameSettings.godMode) {
                        bg.color = korlibs.image.color.RGBA(10, 100, 10, 220)
                        txt.text = "God: ON"
                    } else {
                        bg.color = korlibs.image.color.RGBA(100, 10, 10, 220)
                        txt.text = "God: OFF"
                    }
                }
            }
            visible = false
            xy(Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth - 8.0 - devBtnW * 3 - 16.0, devBtnY)
        }
        addChild(godModeBtn)

        /**
         * Attempt to spend a skill point on [skillCfg].
         *
         * Developer Mode behaviour:
         *  - Upgrades are free (no point deducted from [progress]).
         *  - There is no upgrade-point display — skill slots upgrade immediately
         *    as long as canUpgrade is true.
         *
         * Normal behaviour:
         *  - First point on a slot with requiresPointUnlock sets paidUnlock.
         *  - Further points call upgrade().
         */
        fun trySpendSkillPoint(skillCfg: SkillConfig, btn: SkillButton) {
            if (progress.level < skillCfg.unlockLevel) return

            if (GameSettings.developerMode) {
                // Dev mode: free upgrades, no point check
                if (skillCfg.requiresPointUnlock && !skillCfg.paidUnlock) {
                    skillCfg.paidUnlock = true
                } else if (skillCfg.canUpgrade) {
                    skillCfg.upgrade()
                }
                btn.updateLabels()
                return
            }

            // Normal mode: spend a point
            if (skillCfg.requiresPointUnlock && !skillCfg.paidUnlock) {
                if (progress.spendUpgradePoint()) {
                    skillCfg.paidUnlock = true
                    btn.updateLabels()
                }
                return
            }
            if (skillCfg.canUpgrade && progress.spendUpgradePoint()) {
                skillCfg.upgrade()
                btn.updateLabels()
            }
        }


        attackBtn.onUpgradeClick = { trySpendSkillPoint(player.basicAttackSkill, attackBtn) }
        skillBtn1.onUpgradeClick = { trySpendSkillPoint(player.skill1Config, skillBtn1) }
        skillBtn2.onUpgradeClick = { trySpendSkillPoint(player.skill2Config, skillBtn2) }
        skillBtn3.onUpgradeClick = { trySpendSkillPoint(player.skill3Config, skillBtn3) }
        skillBtn4.onUpgradeClick = { trySpendSkillPoint(player.skill4Config, skillBtn4) }
        skillBtnHealing.onUpgradeClick = { trySpendSkillPoint(player.healingSkillConfig, skillBtnHealing) }
        skillBtnMaxHealth.onUpgradeClick = { trySpendSkillPoint(player.maxHealthSkillConfig, skillBtnMaxHealth) }

        // -------------------------------------------------------
        // PAUSE MENU SETUP
        // -------------------------------------------------------
        var pauseMenuContainer: Container? = null
        fun createPauseMenu(): Container {
            return container {
                solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) {
                    alpha = 0.7
                }
                text("PAUSED", textSize = 80.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    x = Constants.SCREEN_WIDTH / 2.0 - 150.0
                    y = 150.0
                }

                val menuBtnW = 240.0
                val menuBtnH = 80.0
                val menuCx   = Constants.SCREEN_WIDTH / 2.0
                val menuStartY = 350.0
                val menuGap = 100.0

                // Resume button
                solidRect(menuBtnW, menuBtnH, Colors.DARKGREEN) {
                    x = menuCx - menuBtnW / 2
                    y = menuStartY
                    onOver { alpha = 0.7 }
                    onOut  { alpha = 1.0 }
                    onClick {
                        isPaused = false
                        pauseMenuContainer?.removeFromParent()
                        pauseMenuContainer = null
                    }
                }
                text("Resume", textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    x = menuCx - 80.0
                    y = menuStartY + (menuBtnH - fontSize) / 2
                }

                // Restart button
                solidRect(menuBtnW, menuBtnH, Colors.DARKBLUE) {
                    x = menuCx - menuBtnW / 2
                    y = menuStartY + menuGap
                    onOver { alpha = 0.7 }
                    onOut  { alpha = 1.0 }
                    onClick {
                        isPaused = false
                        AttackDisplay.clearAll()
                        launchImmediately { sceneContainer.changeTo { GameScene() } }
                    }
                }
                text("Restart", textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    x = menuCx - 80.0
                    y = menuStartY + menuGap + (menuBtnH - fontSize) / 2
                }

                // Quit button
                solidRect(menuBtnW, menuBtnH, Colors.DARKRED) {
                    x = menuCx - menuBtnW / 2
                    y = menuStartY + menuGap * 2
                    onOver { alpha = 0.7 }
                    onOut  { alpha = 1.0 }
                    onClick {
                        isPaused = false
                        AttackDisplay.clearAll()
                        launchImmediately { sceneContainer.changeTo { MenuScene() } }
                    }
                }
                text("Quit", textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    x = menuCx - 60.0
                    y = menuStartY + menuGap * 2 + (menuBtnH - fontSize) / 2
                }
            }
        }

        // -------------------------------------------------------
        // DEATH SCREEN SETUP
        // -------------------------------------------------------
        var deathScreenContainer: Container? = null
        fun createDeathScreen(): Container {
            return container {
                solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) {
                    alpha = 0.8
                }
                text("YOU DIED", textSize = 100.0, color = Colors["#cc2222"], font = GameAssets.customFont) {
                    x = Constants.SCREEN_WIDTH / 2.0 - 200.0
                    y = 200.0
                }

                val deathBtnW = 240.0
                val deathBtnH = 80.0
                val deathCx   = Constants.SCREEN_WIDTH / 2.0
                val deathStartY = 420.0
                val deathGap = 100.0

                // Restart button
                solidRect(deathBtnW, deathBtnH, Colors.DARKGREEN) {
                    x = deathCx - deathBtnW / 2
                    y = deathStartY
                    onOver { alpha = 0.7 }
                    onOut  { alpha = 1.0 }
                    onClick {
                        AttackDisplay.clearAll()
                        launchImmediately { sceneContainer.changeTo { GameScene() } }
                    }
                }
                text("Restart", textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    x = deathCx - 80.0
                    y = deathStartY + (deathBtnH - fontSize) / 2
                }

                // Quit button
                solidRect(deathBtnW, deathBtnH, Colors.DARKRED) {
                    x = deathCx - deathBtnW / 2
                    y = deathStartY + deathGap
                    onOver { alpha = 0.7 }
                    onOut  { alpha = 1.0 }
                    onClick {
                        AttackDisplay.clearAll()
                        launchImmediately { sceneContainer.changeTo { MenuScene() } }
                    }
                }
                text("Quit", textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    x = deathCx - 60.0
                    y = deathStartY + deathGap + (deathBtnH - fontSize) / 2
                }
            }
        }

        // -------------------------------------------------------
        // TIMER UI — positioned directly below the pause button
        // pauseBtn: x = SCREEN_WIDTH - 20 - pauseBtnWidth, y = 20, h = pauseBtnHeight
        // -------------------------------------------------------
        val timerText = text("Time: 0:00", textSize = 20.0, color = Colors.WHITE, font = GameAssets.customFont) {
            x = Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth  // left-aligned with pause button
            y = 20.0 + pauseBtnHeight + 6.0                    // directly below pause button
        }

        // -------------------------------------------------------
        // WAVE UI — positioned below the timer
        // -------------------------------------------------------
        val waveText = text("Wave 1", textSize = 18.0, color = Colors.YELLOW, font = GameAssets.customFont) {
            x = Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth  // left-aligned with pause button
            y = 20.0 + pauseBtnHeight + 6.0 + 28.0             // below timer
        }

        fun formatTime(seconds: Double): String {
            val mins = (seconds / 60).toInt()
            val secs = (seconds % 60).toInt()
            return String.format("Time: %d:%02d", mins, secs)
        }

        // Debug: Esc → main menu (edge-triggered so it does not repeat while held)
        var prevEscapeDown = false

        // -------------------------------------------------------
        // MAIN UPDATE LOOP
        // -------------------------------------------------------
        addUpdater { dt ->
            val dtSec = dt.seconds

            val escapeDown = views.input.keys[Key.ESCAPE]
            if (escapeDown && !prevEscapeDown) {
                isPaused = false
                pauseMenuContainer?.removeFromParent()
                pauseMenuContainer = null
                deathScreenContainer?.removeFromParent()
                deathScreenContainer = null
                AttackDisplay.clearAll()
                launchImmediately { sceneContainer.changeTo { MenuScene() } }
                prevEscapeDown = escapeDown
                return@addUpdater
            }
            prevEscapeDown = escapeDown

            // Update timer (only if game is not paused and player is alive)
            if (!isPaused && player.isAlive()) {
                gameTime += dtSec
                timerText.text = formatTime(gameTime)
                val currentWave = WaveSystem.getWaveNumber(gameTime)
                waveText.text = "Wave $currentWave"
            }

            // --- INPUT HANDLING (Touch + Mouse for Desktop) ---
            val touches     = views.input.activeTouches
            val mousePos    = views.input.mousePos
            val isMouseDown = views.input.mouseButtons != 0
            val inputPoints = mutableListOf<Point>()
            touches.forEach { inputPoints.add(Point(it.x, it.y)) }
            if (isMouseDown) inputPoints.add(Point(mousePos.x, mousePos.y))

            // Pause button check
            for (point in inputPoints) {
                if (pauseBtn.hitTest(point) != null && !isPaused && player.isAlive()) {
                    isPaused = true
                    pauseMenuContainer = createPauseMenu()
                    this@sceneMain.addChild(pauseMenuContainer!!)
                    break
                }
            }

            // ── Developer Mode button visibility ─────────────────────────
            val devOn = GameSettings.developerMode
            levelUpBtn.visible  = devOn
            nextWaveBtn.visible = devOn
            godModeBtn.visible  = devOn

            if (!devOn && GameSettings.godMode) {
                GameSettings.godMode = false
                val bg = godModeBtn.children.getOrNull(0) as? korlibs.korge.view.SolidRect
                val txt = godModeBtn.children.getOrNull(1) as? korlibs.korge.view.Text
                bg?.color = korlibs.image.color.RGBA(100, 10, 10, 220)
                txt?.text = "God: OFF"
            }

            // If paused or player dead, skip gameplay updates
            if (isPaused) {
                pauseBtn.isPressed = false
                return@addUpdater
            }

            // Check if player died
            if (!player.isAlive() && deathScreenContainer == null) {
                deathScreenContainer = createDeathScreen()
                this@sceneMain.addChild(deathScreenContainer!!)
                
                // --- SCORE INTEGRATION ---
                ScoreManager.onGameEnd(
                    currentScore = progress.score,
                    timeSurvived = gameTime,
                    wavesCleared = (WaveSystem.getWaveNumber(gameTime) - 1).coerceAtLeast(0),
                    kills = progress.totalKills
                )
            }

            // Skip further updates if player is dead
            if (!player.isAlive()) {
                return@addUpdater
            }

            // reset touch
            TouchInput.left   = false; TouchInput.right  = false
            TouchInput.jump   = false; TouchInput.attack = false
            TouchInput.skill1 = false; TouchInput.skill2 = false
            TouchInput.skill3 = false; TouchInput.skill4 = false

            for (point in inputPoints) {
                if (leftBtn.hitTest(point)   != null) TouchInput.left   = true
                if (rightBtn.hitTest(point)  != null) TouchInput.right  = true
                if (jumpBtn.hitTest(point)   != null) TouchInput.jump   = true
                if (attackBtn.hitTest(point) != null) TouchInput.attack = true
                // "+" upgrade hit area must win over the skill icon (same as SkillButton docs).
                if (!skillBtn1.isUpgradeBtnHit(point) && skillBtn1.hitTest(point) != null) TouchInput.skill1 = true
                if (!skillBtn2.isUpgradeBtnHit(point) && skillBtn2.hitTest(point) != null) TouchInput.skill2 = true
                if (!skillBtn3.isUpgradeBtnHit(point) && skillBtn3.hitTest(point) != null) TouchInput.skill3 = true
                if (!skillBtn4.isUpgradeBtnHit(point) && skillBtn4.hitTest(point) != null) TouchInput.skill4 = true
                // Healing skill cast (active skill) — check upgrade button first
                if (!skillBtnHealing.isUpgradeBtnHit(point) && skillBtnHealing.hitTest(point) != null) {
                    player.castHealingSkill(progress.level)
                }
                // Max health passive: clicking does nothing (passive only)
                // No special handling needed; just let the upgrade button work
                if (!skillBtnMaxHealth.isUpgradeBtnHit(point) && skillBtnMaxHealth.hitTest(point) != null) {
                    // Passive skill does nothing on click — no effect
                }
            }

            leftBtn.isPressed   = TouchInput.left;   rightBtn.isPressed  = TouchInput.right
            jumpBtn.isPressed   = TouchInput.jump;   attackBtn.isPressed = TouchInput.attack
            skillBtn1.isPressed = TouchInput.skill1; skillBtn2.isPressed = TouchInput.skill2
            skillBtn3.isPressed = TouchInput.skill3; skillBtn4.isPressed = TouchInput.skill4

            player.update(dtSec, views, { spawner.getEnemies().filterIsInstance<Damageable>() }, this@sceneMain, progress)

//            val toRemove = mutableListOf<Enemy>()
//            for (enemy in enemies) {
//                enemy.update(dtSec, player.x, listOf(player), this@sceneMain)
//                if (enemy.shouldRemove) toRemove.add(enemy)
//            }
//            toRemove.forEach { removeChild(it); enemies.remove(it) }

            // Update the spawner
            // This spawns enemies at the right time, updates their AI,
            // and removes dead enemies
            spawner.update(
                dt = dtSec,
                playerX = player.x,
                targets = listOf(player)
            )

            // Drain pending spawns — launchImmediately is valid here (Scene scope)
            if (spawner.pendingSpawns.isNotEmpty()) {
                val toSpawn = spawner.pendingSpawns.toList()
                spawner.pendingSpawns.clear()
                launchImmediately {
                    for (event in toSpawn) {
                        for (i in 0 until event.count.coerceAtLeast(1)) {
                            val enemy = EnemyFactory.create(event.enemyType)
                            enemy.onDeath = {
                                if (!GameSettings.developerMode) {
                                    progress.addXp(enemy.xpGain)
                                    progress.addKill()
                                }
                            }
                            enemy.x = event.x + i * event.offsetX
                            enemy.y = Constants.GROUND
                            spawner.addEnemy(enemy)
                            enemyContainer.addChild(enemy)
                        }
                    }
                }
            }


            AttackDisplay.updateAll(dtSec)
            hud.update()

            // Update skill button overlays (cooldown + mana)
            attackBtn.update(player.mana, progress.level, progress.upgradePoints)
            skillBtn1.update(player.mana, progress.level, progress.upgradePoints)
            skillBtn2.update(player.mana, progress.level, progress.upgradePoints)
            skillBtn3.update(player.mana, progress.level, progress.upgradePoints)
            skillBtn4.update(player.mana, progress.level, progress.upgradePoints)
            skillBtnHealing.update(player.mana, progress.level, progress.upgradePoints)
            skillBtnMaxHealth.update(player.mana, progress.level, progress.upgradePoints)

            // --- HEALING SKILL ICON SWAP AT LEVEL 6 ---
            // At level 6+, swap from ramen to bento icon
            val healingLevel = player.healingSkillConfig.upgradeCount + 1
            if (healingLevel >= 6) {
                skillBtnHealing.updateIcon(healingBentoSlice)
            } else {
                skillBtnHealing.updateIcon(healingRamenSlice)
            }


        }
    }
}