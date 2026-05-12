package scenes

import managers.ScoreManager.getHighScore
import korlibs.image.color.Colors
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.korge.input.*
import managers.GameAssets
import managers.AuthManager
import managers.ScoreManager
import ui.TextButton
import utils.Constants

class MainMenuScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@MainMenuScene
        val sw = Constants.SCREEN_WIDTH.toDouble()
        val sh = Constants.SCREEN_HEIGHT.toDouble()

        GameAssets.load()

        // ── Background ───────────────────────────────────────────
        image(GameAssets.bg2Slice).apply {
            width = sw
            height = sh
            smoothing = true
        }
        solidRect(sw, sh, Colors.BLACK).apply {
            alpha = 0.50
        }

        // ── Title ────────────────────────────────────────────────
        text("Fighting Kimhie", textSize = 90.0, color = Colors.WHITE, font = GameAssets.customFont).apply {
            centerXOn(this@sceneMain)
            y = 100.0
        }

        // ── PLAY button (Centered in Scene) ──────────────────────
        val btnW = 300.0
        val btnH = 80.0
        val playY = (sh / 2.0) - (btnH / 2.0)
        image(GameAssets.playSlice).apply {
            width = btnW
            height = btnH
            x = (sw / 2.0) - (btnW / 2.0)
            y = playY

            onOver { alpha = 0.75 }
            onOut { alpha = 1.00 }
            onClick { launchImmediately { scene.sceneContainer.changeTo { MenuScene() } } }
        }

        val authBtnW = 280.0
        val authBtnH = 70.0
        val authBtnY = playY + btnH + 24.0
        val authBtnX = (sw - authBtnW) / 2.0
        if (AuthManager.isLoggedIn()) {
            val authBtn = TextButton(authBtnW, authBtnH, "LOG OUT") {
                launchImmediately {
                    AuthManager.logout()
                    scene.sceneContainer.changeTo { LoginScene() }
                }
            }.apply {
                x = authBtnX
                y = authBtnY
            }
            addChild(authBtn)
        } else {
            val authBtn = TextButton(authBtnW, authBtnH, "LOG IN") {
                launchImmediately { scene.sceneContainer.changeTo { LoginScene() } }
            }.apply {
                x = authBtnX
                y = authBtnY
            }
            addChild(authBtn)
        }

        // ── User Info (Bottom Aligned) ───────────────────────────
        val bottomPadding = 60.0
        val textSpacing = 35.0

        if (AuthManager.isLoggedIn()) {
            val rawLabel = AuthManager.userLabel()
            val cleanName = if (rawLabel.contains('@')) rawLabel.substringBefore('@') else rawLabel

            launchImmediately {
                val stats = ScoreManager.getHighScore()
                
                // Welcome Text (Placed at the very bottom)
                val welcomeTxt = text("Welcome, $cleanName", textSize = 32.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    centerXOn(this@sceneMain)
                    y = sh - bottomPadding
                }

                // High Score (Placed above Welcome)
                if (stats.score > 0) {
                    text("High Score: ${stats.score.toInt()}", textSize = 28.0, color = Colors.YELLOW, font = GameAssets.customFont) {
                        centerXOn(this@sceneMain)
                        y = welcomeTxt.y - textSpacing
                    }
                }
            }

        } else {
            // Guest branch
            val welcomeTxt = text("Welcome, Guest", textSize = 32.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = sh - bottomPadding
            }

            text("Log in to save progress", textSize = 18.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = welcomeTxt.y - textSpacing
            }

            // Optional: Move Login Button relative to bottom if needed
            // Currently omitted to keep UI clean, but you can add it here.
        }
    }
}