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
import utils.Constants

class MainMenuScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@MainMenuScene

        GameAssets.load()

        // ── Background ───────────────────────────────────────────
        val bgSlice = GameAssets.bg2Slice
        image(bgSlice).apply {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }
        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK).apply {
            alpha = 0.50
        }

        // ── Title ────────────────────────────────────────────────
        text("Fighting Kimhie", textSize = 90.0, color = Colors.WHITE, font = GameAssets.customFont).apply {
            centerXOn(this@sceneMain)
            y = 100.0
        }

        val cx      = Constants.SCREEN_WIDTH / 2.0
        val btnW    = 300.0
        val btnH    = 80.0
        val spacing = 100.0
        var currentY = 280.0

        // ── PLAY button (always visible) ─────────────────────────
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
        currentY += spacing

        if (AuthManager.isLoggedIn()) {
            // ── Logged-in branch ──────────────────────────────────
            // Show welcome + high score; NO logout button here —
            // logout lives only in SettingsScene.
            val label = AuthManager.userLabel()
            text("Welcome, $label", textSize = 32.0, color = Colors.WHITE, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = currentY
            }

            launchImmediately {
                val stats = ScoreManager.getHighScore()
                text("High Score: ${stats.score.toInt()}", textSize = 28.0, color = Colors.YELLOW, font = GameAssets.customFont) {
                    centerXOn(this@sceneMain)
                    y = currentY + 40.0
                }
            }

        } else {
            // ── Guest branch ──────────────────────────────────────
            // Show guest labels and a LOG IN button.
            // No logout button — guests are not logged in.
            text("Playing as Guest", textSize = 28.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = currentY
            }
            text("Log in to save progress", textSize = 18.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = currentY + 36.0
            }

            val loginBtn = ui.TextButton(btnW, btnH, "LOG IN") {
                launchImmediately { scene.sceneContainer.changeTo { LoginScene() } }
            }.apply {
                centerXOn(this@sceneMain)
                y = currentY + 80.0
            }
            addChild(loginBtn)
        }
    }
}