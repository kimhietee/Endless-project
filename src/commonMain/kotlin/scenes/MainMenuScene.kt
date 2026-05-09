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

    override suspend fun SContainer.sceneMain() {
        val scene = this@MainMenuScene

        // Preload all game assets
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

        // Semi-transparent overlay
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
        // MENU BUTTONS (Centered)
        // -------------------------------------------------------
        val cx = Constants.SCREEN_WIDTH / 2.0
        val btnW = 300.0
        val btnH = 80.0
        val spacing = 100.0
        var currentY = 280.0

        // 1. PLAY BUTTON (Always visible)
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
            // USER INFO & LOGOUT
            val label = AuthManager.userLabel()
            text("Welcome, $label", textSize = 32.0, color = Colors.WHITE, font = GameAssets.customFont) {
                centerXOn(this@sceneMain)
                y = currentY
            }

            // High Score
            launchImmediately {
                val stats = ScoreManager.getHighScore()
                text("High Score: ${stats.score.toInt()}", textSize = 28.0, color = Colors.YELLOW, font = GameAssets.customFont) {
                    centerXOn(this@sceneMain)
                    y = currentY + 40.0
                }
            }

            // LOGOUT BUTTON
            ui.TextButton(btnW, btnH, "LOGOUT") {
                launchImmediately {
                    AuthManager.logout()
                    scene.sceneContainer.changeTo { MainMenuScene() } // Refresh scene
                }
            }.apply {
                centerXOn(this@sceneMain)
                y = currentY + 100.0
                addChild(this)
            }
            
        } else {
            // LOGIN BUTTON
            ui.TextButton(btnW, btnH, "LOGIN") {
                launchImmediately { scene.sceneContainer.changeTo { LoginScene() } }
            }.apply {
                centerXOn(this@sceneMain)
                y = currentY
                currentY += spacing
                addChild(this)
            }

            // SIGN UP BUTTON
            ui.TextButton(btnW, btnH, "SIGN UP") {
                launchImmediately { scene.sceneContainer.changeTo { LoginScene() } }
            }.apply {
                centerXOn(this@sceneMain)
                y = currentY
                addChild(this)
            }
        }
    }
}