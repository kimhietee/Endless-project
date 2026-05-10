package scenes

import korlibs.image.color.Colors
import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.io.async.launchImmediately
import korlibs.korge.ui.*
import korlibs.math.geom.*
import managers.GameAssets
import managers.AuthManager
import managers.ScoreManager
import utils.Constants

class MainMenuScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@MainMenuScene
        GameAssets.load()

        // -------------------------------------------------------
        // BACKGROUND
        // -------------------------------------------------------
        val bgSlice = GameAssets.bg2Slice
        image(bgSlice).apply {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }

        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK).apply {
            alpha = 0.50
        }

        // -------------------------------------------------------
        // TITLE
        // -------------------------------------------------------
        text("Fighting Kimhie", textSize = 90.0, color = Colors.WHITE, font = GameAssets.customFont).apply {
            centerXOn(this@sceneMain)
            y = 100.0
        }

        // -------------------------------------------------------
        // MENU BUTTONS
        // -------------------------------------------------------
        val cx = Constants.SCREEN_WIDTH / 2.0
        val btnW = 300.0
        val btnH = 80.0
        var currentY = 280.0

        // 1. PLAY BUTTON
        val playSlice = GameAssets.playSlice
        image(playSlice).apply {
            width  = btnW
            height = btnH
            x = cx - btnW / 2.0
            y = currentY
            onOver  { alpha = 0.75 }
            onOut   { alpha = 1.00 }
            onClick { launchImmediately { scene.sceneContainer.changeTo { MenuScene() } } }
        }
        
        currentY += 120.0

        // 2. USER INFO / STATUS
        if (AuthManager.isLoggedIn()) {
            val label = AuthManager.userLabel()
            text("Welcome, $label", textSize = 32.0, color = Colors.WHITE, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = currentY
            }

            launchImmediately {
                val stats = ScoreManager.getHighScore()
                text("High Score: ${stats.score.toInt()}", textSize = 28.0, color = Colors.YELLOW, font = GameAssets.customFont) {
                    centerXOn(this@sceneMain)
                    y = currentY + 45.0
                }
            }
        } else {
            text("Guest Mode", textSize = 32.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = currentY
            }
            text("Log in to save progress", textSize = 20.0, color = Colors.GRAY, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = currentY + 45.0
            }
        }

        // 3. STANDALONE LOGOUT BUTTON (Phase 4a)
        // Positioned at the bottom center, clearly visible but out of the way of the main Play flow.
        val logoutBtnW = 220.0
        val logoutBtnH = 60.0
        uiTextButton(logoutBtnW, logoutBtnH, "LOG OUT") {
            launchImmediately {
                AuthManager.logout()
                scene.sceneContainer.changeTo { LoginScene() }
            }
        }.apply {
            centerXOn(this@sceneMain)
            y = Constants.SCREEN_HEIGHT - 100.0
        }
    }
}