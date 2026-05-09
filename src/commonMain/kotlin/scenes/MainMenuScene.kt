package scenes

import managers.ScoreManager.getHighScore

import korlibs.image.color.Colors
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.*
import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.korge.view.align.centerOn
import korlibs.io.async.launchImmediately
import korlibs.io.*
import korlibs.io.async.*
import korlibs.korge.ui.*
import korlibs.math.geom.*
import managers.GameAssets
import managers.AuthManager
import managers.ScoreManager
import utils.Constants


class MainMenuScene : Scene() {

    private lateinit var authContainer: Container

    override suspend fun SContainer.sceneMain() {
        val scene = this@MainMenuScene

        // Preload all game assets
        GameAssets.load()

        // -------------------------------------------------------
        // BACKGROUND
        // -------------------------------------------------------
        val bgSlice = GameAssets.bg2Slice
        val bg = image(bgSlice).apply {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }
        addChild(bg)

        // Semi-transparent overlay so the text pops
        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK).apply {
            alpha = 0.50
        }

        // -------------------------------------------------------
        // TITLE
        // -------------------------------------------------------
        text("Fighting Kimhie", textSize = 90.0, color = Colors.WHITE, font = GameAssets.customFont).apply {
            centerXOn(this@sceneMain)
            y = 180.0
        }

        // -------------------------------------------------------
        // PLAY BUTTON  →  MenuScene
        // -------------------------------------------------------
        val playSlice = GameAssets.playSlice
        val btnW = 240.0
        val btnH =  80.0
        val cx   = Constants.SCREEN_WIDTH / 2.0

        image(playSlice).apply {
            width  = btnW
            height = btnH
            x = cx - btnW / 2.0
            y = 420.0
            onOver  { alpha = 0.75 }
            onOut   { alpha = 1.00 }
            onClick { launchImmediately { this@MainMenuScene.sceneContainer.changeTo { MenuScene() } } }
        }

        // -------------------------------------------------------
        // AUTH UI SECTION
        // -------------------------------------------------------
        authContainer = container {
            xy(0.0, 520.0)
            updateAuthUI()
        }
    }

    /**
     * Refresh the login/signup buttons or the logged-in user info.
     */
    private fun Container.updateAuthUI() {
        this.removeChildren()
        val cx = Constants.SCREEN_WIDTH / 2.0

        if (AuthManager.isLoggedIn()) {
            val label = AuthManager.userLabel()
            text("Logged in as $label", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
                centerXOn(this@updateAuthUI.parent!!)
            }

            // Show high score if available
            launchImmediately {
                val stats = ScoreManager.getHighScore()
                text("Personal Best: ${stats.score.toInt()}", textSize = 28.0, color = Colors.YELLOW, font = GameAssets.customFont) {
                    centerXOn(this@updateAuthUI.parent!!)
                    y = 35.0
                }
            }

            // Simple logout link
            val logoutBtn = ui.TextButton(120.0, 40.0, "Logout") {
                launchImmediately {
                    AuthManager.logout()
                    sceneContainer.changeTo { LoginScene() }
                }
            }.apply {
                centerXOn(this@updateAuthUI.parent!!)
                y = 70.0
            }
            addChild(logoutBtn)
        } else {
            // LOGIN BUTTON
            val loginBtn = ui.TextButton(140.0, 50.0, "LOGIN") {
                launchImmediately { sceneContainer.changeTo { LoginScene() } }
            }.apply {
                x = cx - 150.0
            }
            addChild(loginBtn)

            // SIGN UP BUTTON
            val signupBtn = ui.TextButton(140.0, 50.0, "SIGN UP") {
                launchImmediately { sceneContainer.changeTo { LoginScene() } }
            }.apply {
                x = cx + 10.0
            }
            addChild(signupBtn)
        }
    }
}