package scenes

import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.align.centerOn
import korlibs.korge.input.*
import korlibs.image.color.Colors
import korlibs.image.bitmap.BmpSlice
import korlibs.io.async.launchImmediately
import korlibs.event.Key
import korlibs.math.geom.Point
import korlibs.time.seconds
import entities.*
import entities.heroes.HeroRegistry
import entities.heroes.WandererMagicianHero
import ui.*
import managers.*
import utils.*

class GameScene : Scene() {

    private var isPaused = false
    private var gameTime = 0.0

    override suspend fun SContainer.sceneMain() {
        val heroId = managers.GameSession.selectedHeroId ?: entities.heroes.FireWizardHero.ID
        // Ensure assets are loaded before anything else
        GameAssets.loadGlobal()
        GameAssets.loadHeroAssets(heroId)

        // -------------------------------------------------------
        // BACKGROUND — starts on wave 1's background and updates
        // each time the wave number changes.
        // -------------------------------------------------------
        var lastRenderedWave = -1   // force a set on first frame

        val bg = image(GameAssets.backgroundForWave(1)).apply {
            scaledWidth  = Constants.SCREEN_WIDTH.toDouble()
            scaledHeight = Constants.SCREEN_HEIGHT.toDouble()
            smoothing    = true
            y         = Constants.GROUND - Constants.SCREEN_HEIGHT

        }
        addChild(bg)

        // -------------------------------------------------------
        // CHARACTER ASSETS (per selected hero)
        // -------------------------------------------------------
        val heroConfig = HeroRegistry.configForCurrentSession()
        val isWandererMagician = heroConfig.id == WandererMagicianHero.ID

        val idleFrames   = if (isWandererMagician) GameAssets.wmIdleFrames else GameAssets.idleFrames
        val runFrames    = if (isWandererMagician) GameAssets.wmRunFrames else GameAssets.runFrames
        val jumpFrames   = if (isWandererMagician) GameAssets.wmJumpFrames else GameAssets.jumpFrames
        val attackFrames = if (isWandererMagician) GameAssets.wmAttackFrames else GameAssets.attackFrames
        val skillFrames  = if (isWandererMagician) attackFrames else GameAssets.skillFrames

        val basicAtkFrames = if (isWandererMagician) GameAssets.wmBasicProjectileFrames else GameAssets.basicAtkFrames
        val skill1Frames   = if (isWandererMagician) GameAssets.wmSkill1Frames else GameAssets.skill1Frames
        val skill2Frames   = if (isWandererMagician) GameAssets.wmSkill2AuraFrames else GameAssets.skill2Frames
        val skill3Frames   = if (isWandererMagician) GameAssets.wmSkill3ExplodeFrames else GameAssets.skill3Frames
        val skill4Frames   = if (isWandererMagician) GameAssets.wmSkill4BallFrames else GameAssets.skill4Frames

        val skill3CastBodyFrames = if (isWandererMagician) GameAssets.wmSkill3CastFrames else GameAssets.skillFrames
        val skill4ChargeFrames = if (isWandererMagician) GameAssets.wmSkill4ChargeFrames else emptyList()

        // -------------------------------------------------------
        // PLAYER
        // -------------------------------------------------------
        val player = Character(
            isPlayer       = true,
            heroConfig     = heroConfig,
            idleAnims      = idleFrames,
            runAnims       = runFrames,
            jumpAnims      = jumpFrames,
            attackAnims    = attackFrames,
            skillAnims     = skillFrames,
            basicAtkFrames = basicAtkFrames,
            skill1Frames   = skill1Frames,
            skill2Frames   = skill2Frames,
            skill3Frames   = skill3Frames,
            skill4Frames   = skill4Frames,
            skill3CastBodyFrames = skill3CastBodyFrames,
            skill4ChargeFrames   = skill4ChargeFrames
        )

        val progress = PlayerProgress()
        addChild(player)
        player.xy(100.0, Constants.GROUND)

        val enemyContainer = container()
        val spawner = EnemySpawner(enemyContainer)

        val hud = HUD(player, progress)
        addChild(hud)

        var currentScore = 0.0
        val scoreDisplay = text("Score: 0", textSize = 40.0, color = Colors.WHITE, font = GameAssets.customFont).apply {
            x = Constants.SCREEN_WIDTH / 2.0 - 100.0
            y = 30.0
        }

        // GameAssets.load() // Already called at the top
        WaveSystem.apply(spawner)

        // -------------------------------------------------------
        // BUTTON ASSETS
        // -------------------------------------------------------
        val leftSlice         = GameAssets.leftSlice
        val rightSlice        = GameAssets.rightSlice
        val jumpSlice         = GameAssets.jumpSlice
        val attackSlice       = GameAssets.attackSlice
        val skill1Slice       = if (isWandererMagician) GameAssets.wmSkill1Icon else GameAssets.skill1Slice
        val skill2Slice       = if (isWandererMagician) GameAssets.wmSkill2Icon else GameAssets.skill2Slice
        val skill3Slice       = if (isWandererMagician) GameAssets.wmSkill3Icon else GameAssets.skill3Slice
        val skill4Slice       = if (isWandererMagician) GameAssets.wmSkill4Icon else GameAssets.skill4Slice
        val healingRamenSlice = GameAssets.healingRamenSlice
        val healingBentoSlice = GameAssets.healingBentoSlice
        val maxHealthSlice    = GameAssets.maxHealthSlice
        val pauseSlice        = GameAssets.pauseSlice
        val upgradeSlice      = GameAssets.upgradeSlice

        // -------------------------------------------------------
        // BUTTON LAYOUT
        // -------------------------------------------------------
        val btnSize = 100.0
        val gap     = 8.0
        val rowY    = Constants.GROUND + (Constants.SCREEN_HEIGHT - Constants.GROUND - btnSize) / 2.0

        val jumpX      = Constants.SCREEN_WIDTH - 20.0 - btnSize
        val attackX    = jumpX - gap - btnSize
        val healingX   = attackX - gap - btnSize
        val maxHealthX = healingX - gap - btnSize

        val leftBtn   = TouchButton(btnSize, btnSize, leftSlice ).xy(20.0, rowY)
        val rightBtn  = TouchButton(btnSize, btnSize, rightSlice).xy(20.0 + btnSize + gap, rowY)
        val skillsX   = 20.0 + (btnSize + gap) * 2
        val skillBtn1 = SkillButton(btnSize, btnSize, skill1Slice, upgradeSlice, player.skill1Config      ).xy(skillsX + (btnSize + gap) * 0, rowY)
        val skillBtn2 = SkillButton(
            btnSize, btnSize, skill2Slice, upgradeSlice, player.skill2Config,
            cornerHealTotalWhenDamageZero = if (isWandererMagician) player.skill2Config.heal else null
        ).xy(skillsX + (btnSize + gap) * 1, rowY)
        val skillBtn3 = SkillButton(btnSize, btnSize, skill3Slice, upgradeSlice, player.skill3Config      ).xy(skillsX + (btnSize + gap) * 2, rowY)
        val skillBtn4 = SkillButton(btnSize, btnSize, skill4Slice, upgradeSlice, player.skill4Config      ).xy(skillsX + (btnSize + gap) * 3, rowY)
        val attackBtn        = SkillButton(btnSize, btnSize, attackSlice,       upgradeSlice, player.basicAttackSkill   ).xy(attackX, rowY)
        val skillBtnHealing  = SkillButton(
            btnSize, btnSize, healingRamenSlice, upgradeSlice, player.healingSkillConfig,
            cornerNumberIsHealAmount = true
        ).xy(healingX, rowY)
        val skillBtnMaxHealth= SkillButton(btnSize, btnSize, maxHealthSlice,    upgradeSlice, player.maxHealthSkillConfig).xy(maxHealthX, rowY)
        val jumpBtn  = TouchButton(btnSize, btnSize, jumpSlice).xy(jumpX, rowY)

        val pauseBtnWidth  = 120.0
        val pauseBtnHeight = 50.0
        val pauseBtn = TouchButton(pauseBtnWidth, pauseBtnHeight, pauseSlice)
            .xy(Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth, 20.0)

        listOf(leftBtn, rightBtn, skillBtn1, skillBtn2, skillBtn3, skillBtn4,
               attackBtn, skillBtnHealing, skillBtnMaxHealth, jumpBtn, pauseBtn)
            .forEach { addChild(it) }

        // -------------------------------------------------------
        // DEVELOPER MODE BUTTONS
        // -------------------------------------------------------
        val devBtnW = 120.0
        val devBtnH = 50.0
        val devBtnY = 20.0
        val NEXT_WAVE_TIME_SKIP = 90.0

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

        fun trySpendSkillPoint(skillCfg: SkillConfig, btn: SkillButton) {
            if (progress.level < skillCfg.unlockLevel) return
            if (GameSettings.developerMode) {
                if (skillCfg.requiresPointUnlock && !skillCfg.paidUnlock) {
                    skillCfg.paidUnlock = true
                } else if (skillCfg.canUpgrade) {
                    skillCfg.upgrade()
                }
                btn.updateLabels()
                return
            }
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

        attackBtn.onUpgradeClick         = { trySpendSkillPoint(player.basicAttackSkill,    attackBtn)         }
        skillBtn1.onUpgradeClick         = { trySpendSkillPoint(player.skill1Config,         skillBtn1)         }
        skillBtn2.onUpgradeClick         = { trySpendSkillPoint(player.skill2Config,         skillBtn2)         }
        skillBtn3.onUpgradeClick         = { trySpendSkillPoint(player.skill3Config,         skillBtn3)         }
        skillBtn4.onUpgradeClick         = { trySpendSkillPoint(player.skill4Config,         skillBtn4)         }
        skillBtnHealing.onUpgradeClick   = { trySpendSkillPoint(player.healingSkillConfig,   skillBtnHealing)   }
        skillBtnMaxHealth.onUpgradeClick = { trySpendSkillPoint(player.maxHealthSkillConfig, skillBtnMaxHealth) }

        // -------------------------------------------------------
        // PAUSE MENU — image buttons (no solid-colour rects, no text labels)
        // Each button is an image that dims on hover for feedback.
        //
        // Asset paths (set in GameAssets):
        //   pauseResumeSlice  → "ui/buttons/button_bg.png"
        //   pauseRestartSlice → "ui/buttons/button_bg.png"
        //   pauseQuitSlice    → "ui/buttons/button_bg.png"
        //
        // Replace those files with your actual art — sizing/layout will adapt
        // automatically based on menuBtnW / menuBtnH below.
        // -------------------------------------------------------
        var pauseMenuContainer: Container? = null

        fun createPauseMenu(): Container {
            return container {
                // Dim overlay
                solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) {
                    alpha = 0.7
                }

                // "PAUSED" title
                text("PAUSED", textSize = 80.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    x = Constants.SCREEN_WIDTH / 2.0 - 150.0
                    y = 150.0
                }

                val menuBtnW  = 280.0
                val menuBtnH  = 90.0
                val menuCx    = Constants.SCREEN_WIDTH / 2.0
                val menuStartY = 320.0
                val menuGap   = 110.0

                // ── Resume ──────────────────────────────────────────────
                container {
                    x = menuCx - menuBtnW / 2.0
                    y = menuStartY
                    image(GameAssets.pauseResumeSlice) {
                        width  = menuBtnW
                        height = menuBtnH
                        onOver  { alpha = 0.75 }
                        onOut   { alpha = 1.00 }
                        onClick {
                            isPaused = false
                            pauseMenuContainer?.removeFromParent()
                            pauseMenuContainer = null
                        }
                    }
                    text("RESUME", textSize = 30.0, color = Colors.WHITE, font = GameAssets.customFont) {
                        centerOn(this.parent!!)
                    }
                }

                // ── Restart ─────────────────────────────────────────────
                container {
                    x = menuCx - menuBtnW / 2.0
                    y = menuStartY + menuGap
                    image(GameAssets.pauseRestartSlice) {
                        width  = menuBtnW
                        height = menuBtnH
                        onOver  { alpha = 0.75 }
                        onOut   { alpha = 1.00 }
                        onClick {
                            isPaused = false
                            AttackDisplay.clearAll()
                            launchImmediately { sceneContainer.changeTo { GameScene() } }
                        }
                    }
                    text("RESTART", textSize = 30.0, color = Colors.WHITE, font = GameAssets.customFont) {
                        centerOn(this.parent!!)
                    }
                }

                // ── Quit ────────────────────────────────────────────────
                container {
                    x = menuCx - menuBtnW / 2.0
                    y = menuStartY + menuGap * 2
                    image(GameAssets.pauseQuitSlice) {
                        width  = menuBtnW
                        height = menuBtnH
                        onOver  { alpha = 0.75 }
                        onOut   { alpha = 1.00 }
                        onClick {
                            isPaused = false
                            AttackDisplay.clearAll()
                            launchImmediately { sceneContainer.changeTo { MenuScene() } }
                        }
                    }
                    text("QUIT", textSize = 30.0, color = Colors.WHITE, font = GameAssets.customFont) {
                        centerOn(this.parent!!)
                    }
                }
            }
        }

        // -------------------------------------------------------
        // DEATH SCREEN
        // -------------------------------------------------------
        var deathScreenContainer: Container? = null
        fun createDeathScreen(
            score: Double,
            timeSurvived: Double,
            wavesCleared: Int,
            kills: Int,
            cheatWarning: String?
        ): Container {
            return container {
                solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) {
                    alpha = 0.8
                }
                val deathCx = Constants.SCREEN_WIDTH / 2.0

                text("YOU DIED", textSize = 100.0, color = Colors["#cc2222"], font = GameAssets.customFont) {
                    x = deathCx - 200.0
                    y = 100.0
                }

                val statStartY = 220.0
                val statGap    = 36.0
                val mins = (timeSurvived / 60).toInt()
                val secs = (timeSurvived % 60).toInt()
                val timeStr = String.format("%d:%02d", mins, secs)
                val statLines = listOf(
                    "Score:  ${score.toInt()}",
                    "Time:   $timeStr",
                    "Waves:  $wavesCleared",
                    "Kills:  $kills"
                )
                statLines.forEachIndexed { i, line ->
                    text(line, textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
                        x = deathCx - 120.0
                        y = statStartY + i * statGap
                    }
                }

                val saveStatusY = statStartY + statLines.size * statGap + 16.0
                if (cheatWarning != null) {
                    text("Data NOT recorded", textSize = 22.0, color = Colors["#ff6600"], font = GameAssets.customFont) {
                        x = deathCx - 130.0
                        y = saveStatusY
                    }
                    text(cheatWarning, textSize = 18.0, color = Colors["#ff6600"], font = GameAssets.customFont) {
                        x = deathCx - 130.0
                        y = saveStatusY + 28.0
                    }
                } else if (!AuthManager.isGuest()) {
                    text("Progress saved!", textSize = 22.0, color = Colors["#44cc44"], font = GameAssets.customFont) {
                        x = deathCx - 100.0
                        y = saveStatusY
                    }
                } else {
                    text("Log in to save progress", textSize = 22.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
                        x = deathCx - 140.0
                        y = saveStatusY
                    }
                }

                val deathBtnW   = 240.0
                val deathBtnH   = 80.0
                val deathStartY = saveStatusY + 60.0
                val deathGap    = 100.0

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
        // TIMER / WAVE UI
        // -------------------------------------------------------
        addChild(scoreDisplay)

        val timerText = text("Time: 0:00", textSize = 20.0, color = Colors.WHITE, font = GameAssets.customFont) {
            x = Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth
            y = 20.0 + pauseBtnHeight + 6.0
        }

        val waveText = text("Wave 1", textSize = 18.0, color = Colors.YELLOW, font = GameAssets.customFont) {
            x = Constants.SCREEN_WIDTH - 20.0 - pauseBtnWidth
            y = 20.0 + pauseBtnHeight + 6.0 + 28.0
        }

        fun formatTime(seconds: Double): String {
            val mins = (seconds / 60).toInt()
            val secs = (seconds % 60).toInt()
            return String.format("Time: %d:%02d", mins, secs)
        }

        var prevEscapeDown = false

        // -------------------------------------------------------
        // MAIN UPDATE LOOP
        // -------------------------------------------------------
        addUpdater { dt ->
            val dtSec = dt.seconds

            // Esc → main menu
            val escapeDown = views.input.keys[Key.ESCAPE]
            if (escapeDown && !prevEscapeDown) {
                isPaused = false
                pauseMenuContainer?.removeFromParent();  pauseMenuContainer  = null
                deathScreenContainer?.removeFromParent(); deathScreenContainer = null
                AttackDisplay.clearAll()
                launchImmediately { sceneContainer.changeTo { MenuScene() } }
                prevEscapeDown = escapeDown
                return@addUpdater
            }
            prevEscapeDown = escapeDown

            // ── Timer + wave number ──────────────────────────────
            if (!isPaused && player.isAlive()) {
                gameTime += dtSec
                timerText.text = formatTime(gameTime)

                val currentWave = WaveSystem.getWaveNumber(gameTime)
                waveText.text = "Wave $currentWave"

                // ── Swap background when wave changes ────────────
                // Uses GameAssets.backgroundList which cycles automatically.
                if (currentWave != lastRenderedWave) {
                    lastRenderedWave = currentWave
                    
                    // 1. Swap the bitmap
                    launchImmediately {
                        bg.bitmap = GameAssets.backgroundForWave(currentWave)
                    }
                    
                    // 2. Use scaledWidth/Height (These are verified KorGE properties)
                    bg.scaledWidth = Constants.SCREEN_WIDTH.toDouble()
                    bg.scaledHeight = Constants.SCREEN_HEIGHT.toDouble()
                    
                    // 3. Ensure it's positioned at the origin
//                    bg.xy(0, 0)
                }

            }

            // ── Input ────────────────────────────────────────────
            val touches     = views.input.activeTouches
            val mousePos    = views.input.mousePos
            val isMouseDown = views.input.mouseButtons != 0
            val inputPoints = mutableListOf<Point>()
            touches.forEach { inputPoints.add(Point(it.x, it.y)) }
            if (isMouseDown) inputPoints.add(Point(mousePos.x, mousePos.y))

            // Pause button
            for (point in inputPoints) {
                if (pauseBtn.hitTest(point) != null && !isPaused && player.isAlive()) {
                    isPaused = true
                    pauseMenuContainer = createPauseMenu()
                    this@sceneMain.addChild(pauseMenuContainer!!)
                    break
                }
            }

            // Developer button visibility
            val devOn = GameSettings.developerMode
            levelUpBtn.visible  = devOn
            nextWaveBtn.visible = devOn
            godModeBtn.visible  = devOn

            if (!devOn && GameSettings.godMode) {
                GameSettings.godMode = false
                val bgRect = godModeBtn.children.getOrNull(0) as? korlibs.korge.view.SolidRect
                val txt    = godModeBtn.children.getOrNull(1) as? korlibs.korge.view.Text
                bgRect?.color = korlibs.image.color.RGBA(100, 10, 10, 220)
                txt?.text     = "God: OFF"
            }

            if (isPaused) {
                pauseBtn.isPressed = false
                return@addUpdater
            }

            // ── Death check ──────────────────────────────────────
            if (!player.isAlive() && deathScreenContainer == null) {
                val wavesThisRun = (WaveSystem.getWaveNumber(gameTime) - 1).coerceAtLeast(0)
                val cheatFlags = buildList {
                    if (GameSettings.developerMode) add("Developer Mode is ON")
                    if (GameSettings.showHitbox)    add("Show Hitbox is ON")
                }
                val cheatWarning = if (cheatFlags.isNotEmpty()) cheatFlags.joinToString(" | ") else null

                deathScreenContainer = createDeathScreen(
                    score        = currentScore,
                    timeSurvived = gameTime,
                    wavesCleared = wavesThisRun,
                    kills        = progress.totalKills,
                    cheatWarning = cheatWarning
                )
                this@sceneMain.addChild(deathScreenContainer!!)

                if (cheatWarning == null) {
                    ScoreManager.onGameEnd(
                        currentScore = currentScore,
                        timeSurvived = gameTime,
                        wavesCleared = wavesThisRun,
                        kills        = progress.totalKills
                    )
                }
            }

            if (!player.isAlive()) return@addUpdater

            // ── Touch input ──────────────────────────────────────
            TouchInput.left   = false; TouchInput.right  = false
            TouchInput.jump   = false; TouchInput.attack = false
            TouchInput.skill1 = false; TouchInput.skill2 = false
            TouchInput.skill3 = false; TouchInput.skill4 = false
            TouchInput.heal   = false

            for (point in inputPoints) {
                if (leftBtn.hitTest(point)   != null) TouchInput.left   = true
                if (rightBtn.hitTest(point)  != null) TouchInput.right  = true
                if (jumpBtn.hitTest(point)   != null) TouchInput.jump   = true
                if (attackBtn.hitTest(point) != null) TouchInput.attack = true
                if (!skillBtn1.isUpgradeBtnHit(point) && skillBtn1.hitTest(point) != null) TouchInput.skill1 = true
                if (!skillBtn2.isUpgradeBtnHit(point) && skillBtn2.hitTest(point) != null) TouchInput.skill2 = true
                if (!skillBtn3.isUpgradeBtnHit(point) && skillBtn3.hitTest(point) != null) TouchInput.skill3 = true
                if (!skillBtn4.isUpgradeBtnHit(point) && skillBtn4.hitTest(point) != null) TouchInput.skill4 = true
                if (!skillBtnHealing.isUpgradeBtnHit(point) && skillBtnHealing.hitTest(point) != null) {
                    TouchInput.heal = true
                }
            }

            leftBtn.isPressed   = TouchInput.left;   rightBtn.isPressed  = TouchInput.right
            jumpBtn.isPressed   = TouchInput.jump;   attackBtn.isPressed = TouchInput.attack
            skillBtn1.isPressed = TouchInput.skill1; skillBtn2.isPressed = TouchInput.skill2
            skillBtn3.isPressed = TouchInput.skill3; skillBtn4.isPressed = TouchInput.skill4

            player.update(dtSec, views, { spawner.getEnemies().filterIsInstance<Damageable>() }, this@sceneMain, progress)

            spawner.update(dt = dtSec, playerX = player.x, targets = listOf(player))

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
                                    currentScore += enemy.xpGain
                                    scoreDisplay.text = "Score: ${currentScore.toInt()}"
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

            attackBtn.update(player.mana, progress.level, progress.upgradePoints)
            skillBtn1.update(player.mana, progress.level, progress.upgradePoints)
            skillBtn2.update(player.mana, progress.level, progress.upgradePoints)
            skillBtn3.update(player.mana, progress.level, progress.upgradePoints)
            skillBtn4.update(player.mana, progress.level, progress.upgradePoints)
            skillBtnHealing.update(player.mana, progress.level, progress.upgradePoints)
            skillBtnMaxHealth.update(player.mana, progress.level, progress.upgradePoints)

            val healingLevel = player.healingSkillConfig.upgradeCount + 1
            if (healingLevel >= 6) skillBtnHealing.updateIcon(healingBentoSlice)
            else                   skillBtnHealing.updateIcon(healingRamenSlice)
        }
    }
}
