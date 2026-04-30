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

        // -------------------------------------------------------
        // CHARACTER ASSETS (from cached GameAssets)
        // -------------------------------------------------------
        val idleFrames   = GameAssets.idleFrames
        val runFrames    = GameAssets.runFrames
        val jumpFrames   = GameAssets.jumpFrames
        val attackFrames = GameAssets.attackFrames
        val skillFrames  = GameAssets.skillFrames

        val basicAtkFrames = GameAssets.loadFrames(FrameConfig("fireWizard/slash_pngs",    "Attack_1_", startIndex = 0, count = 10))
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
        addChild(player)
        player.xy(100.0, Constants.GROUND)

        val enemyContainer = container()
        val spawner = EnemySpawner(enemyContainer)

        // -------------------------------------------------------
        // ENEMY CONFIGS
        // NOTE: for ranged attackDisplayConfig, speed is ALWAYS positive here.
        // Enemy.spawnAttack() flips it based on facingRight at spawn time.
        // -------------------------------------------------------
//        val enemySlashAtkFrames = GameAssets.loadFrames(
//            FrameConfig("fireWizard/slash_pngs", "Attack_1_", startIndex = 0, count = 10)
//        )
//        val enemyArrowAtkFrames = GameAssets.loadFrames(
//            FrameConfig("fireWizard/fireball_pngs", "image_0-", startIndex = 0, count = 8)
//        )
//
//        val meleeEnemyConfig = EnemyConfig(
//            idleConfig   = FrameConfig("enemy", sheet = SpriteSheetConfig("skeleton_run", columns = 10, rows = 1), count = 10),
//            runConfig    = FrameConfig("enemy", sheet = SpriteSheetConfig("skeleton_run", columns = 10, rows = 1), count = 10),
//            attackConfig = FrameConfig("fireWizard/slash_pngs", "Attack_1_", startIndex = 0, count = 10),
//            deathConfig  = FrameConfig("fireWizard/jump_pngs",  "Jump_",     startIndex = 0, count = 6),
//            attackDisplayConfig = AttackConfig(
//                frames          = enemySlashAtkFrames,
//                frameDuration   = 0.08,
//                damage          = 2.0,
//                moving          = true,
//                speed           = 0.0,
//                hitboxScaleX    = 0.7,
//                hitboxScaleY    = 0.7,
//                repeatAnimation = 1
//            ),
//            behavior    = EnemyBehavior.MELEE,
//            maxHealth = 20.0,
//            moveSpeed   = 110.0,
//            attackRange = 30.0,
//            attackCooldown = 1.5
//        )
//
//        val rangedEnemyConfig = EnemyConfig(
//            idleConfig   = FrameConfig("enemy", sheet = SpriteSheetConfig("skeleton_walk", columns = 10, rows = 1), count = 10),
//            runConfig    = FrameConfig("enemy", sheet = SpriteSheetConfig("skeleton_walk", columns = 10, rows = 1), count = 10),
//            attackConfig = FrameConfig("fireWizard/fireball_pngs", "image_0-", startIndex = 0, count = 8),
//            deathConfig  = FrameConfig("fireWizard/jump_pngs",     "Jump_",    startIndex = 0, count = 6),
//            attackDisplayConfig = AttackConfig(
//                frames          = enemyArrowAtkFrames,
//                frameDuration   = 0.10,
//                damage          = 8.0,
//                moving          = true,
//                speed           = 300.0,
//                hitboxScaleX    = 0.6,
//                hitboxScaleY    = 0.6,
//                repeatAnimation = 1
//            ),
//            behavior    = EnemyBehavior.RANGED,
//            maxHealth = 20.0,
//            moveSpeed   = 95.0,
//            attackRange = 500.0,
//            attackCooldown = 2.0
//
//        )
//
//        val enemies = mutableListOf<Enemy>()
//        val enemy1  = Enemy.create(meleeEnemyConfig).also  { it.xy(900.0,  Constants.GROUND); addChild(it); enemies.add(it) }
//        val enemy2  = Enemy.create(rangedEnemyConfig).also { it.xy(1100.0, Constants.GROUND); addChild(it); enemies.add(it) }

        val hud = HUD(player)
        addChild(hud)

        GameAssets.load()

        spawner.schedule(
            // Start with some basic skeletons
            SpawnEvent(3.0, "skeleton", 850.0),
            SpawnEvent(7.0, "skeleton", 900.0),
            SpawnEvent(10.5, "skeleton", 800.0),
            SpawnEvent(18.5, "skeleton", 800.0),

            // Add spearmen
            SpawnEvent(15.0, "skeleton_spearman", 900.0),
            SpawnEvent(18.0, "skeleton_spearman", 800.0),

            // Add archers from far away
            SpawnEvent(10.0, "skeleton_archer", 950.0),
            SpawnEvent(15.0, "skeleton_archer", 750.0),

            // Add wolves
            SpawnEvent(35.0, "wolf1", 900.0),
            SpawnEvent(38.0, "wolf2", 850.0),
            SpawnEvent(40.0, "wolf3", 800.0),

            // Mixed waves
            SpawnEvent(50.0, "skeleton", 900.0),
            SpawnEvent(50.5, "wolf1", 850.0),
            SpawnEvent(51.0, "skeleton_archer", 950.0),
            SpawnEvent(52.0, "skeleton_spearman", 800.0)
        )
        
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
        val pauseSlice  = GameAssets.pauseSlice

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

        val pauseBtnSize = 60.0
        val pauseBtn     = TouchButton(pauseBtnSize, pauseBtnSize, pauseSlice).xy(Constants.SCREEN_WIDTH - 20.0 - pauseBtnSize, 20.0)

        listOf(leftBtn, rightBtn, skillBtn1, skillBtn2, skillBtn3, skillBtn4, attackBtn, jumpBtn, pauseBtn)
            .forEach { addChild(it) }

        // -------------------------------------------------------
        // PAUSE MENU SETUP
        // -------------------------------------------------------
        var pauseMenuContainer: Container? = null
        fun createPauseMenu(): Container {
            return container {
                solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) {
                    alpha = 0.7
                }
                text("PAUSED", textSize = 80.0, color = Colors.WHITE) {
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
                text("Resume", textSize = 28.0, color = Colors.WHITE) {
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
                text("Restart", textSize = 28.0, color = Colors.WHITE) {
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
                text("Quit", textSize = 28.0, color = Colors.WHITE) {
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
                text("YOU DIED", textSize = 100.0, color = Colors["#cc2222"]) {
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
                text("Restart", textSize = 28.0, color = Colors.WHITE) {
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
                text("Quit", textSize = 28.0, color = Colors.WHITE) {
                    x = deathCx - 60.0
                    y = deathStartY + deathGap + (deathBtnH - fontSize) / 2
                }
            }
        }

        // -------------------------------------------------------
        // TIMER UI
        // -------------------------------------------------------
        val timerText = text("Time: 0:00", textSize = 24.0, color = Colors.WHITE) {
            x = Constants.SCREEN_WIDTH - 200.0
            y = 20.0
        }

        fun formatTime(seconds: Double): String {
            val mins = (seconds / 60).toInt()
            val secs = (seconds % 60).toInt()
            return String.format("Time: %d:%02d", mins, secs)
        }

        // -------------------------------------------------------
        // MAIN UPDATE LOOP
        // -------------------------------------------------------
        addUpdater { dt ->
            val dtSec = dt.seconds

            // Update timer (only if game is not paused and player is alive)
            if (!isPaused && player.isAlive()) {
                gameTime += dtSec
                timerText.text = formatTime(gameTime)
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

            // If paused or player dead, skip gameplay updates
            if (isPaused) {
                pauseBtn.isPressed = false
                return@addUpdater
            }

            // Check if player died
            if (!player.isAlive() && deathScreenContainer == null) {
                deathScreenContainer = createDeathScreen()
                this@sceneMain.addChild(deathScreenContainer!!)
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
                if (skillBtn1.hitTest(point) != null) TouchInput.skill1 = true
                if (skillBtn2.hitTest(point) != null) TouchInput.skill2 = true
                if (skillBtn3.hitTest(point) != null) TouchInput.skill3 = true
                if (skillBtn4.hitTest(point) != null) TouchInput.skill4 = true
            }

            leftBtn.isPressed   = TouchInput.left;   rightBtn.isPressed  = TouchInput.right
            jumpBtn.isPressed   = TouchInput.jump;   attackBtn.isPressed = TouchInput.attack
            skillBtn1.isPressed = TouchInput.skill1; skillBtn2.isPressed = TouchInput.skill2
            skillBtn3.isPressed = TouchInput.skill3; skillBtn4.isPressed = TouchInput.skill4

            player.update(dtSec, views, spawner.getEnemies().filterIsInstance<Damageable>(), this@sceneMain)

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

            
        }
    }
}
