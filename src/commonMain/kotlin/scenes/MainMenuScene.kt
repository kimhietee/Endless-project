package scenes

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
        // BACKGROUND  (placeholder — swap later)
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
            text("(Logout)", textSize = 16.0, color = Colors.LIGHTGRAY) {
                centerXOn(this@updateAuthUI.parent!!)
                y = 70.0
                onOver { color = Colors.WHITE }
                onOut  { color = Colors.LIGHTGRAY }
                onClick {
                    launchImmediately {
                        AuthManager.logout()
                        updateAuthUI()
                    }
                }
            }
        } else {
            // LOGIN BUTTON
            solidRect(115.0, 50.0, Colors.DARKBLUE) {
                x = cx - 120.0
                onOver { alpha = 0.7 }
                onOut  { alpha = 1.0 }
                onClick { showAuthDialog(isSignUp = false) }
            }
            text("LOGIN", textSize = 18.0, color = Colors.WHITE, font = GameAssets.customFont) {
                x = cx - 120.0 + 25.0
                y = 15.0
            }

            // SIGN UP BUTTON
            solidRect(115.0, 50.0, Colors.DARKGREEN) {
                x = cx + 5.0
                onOver { alpha = 0.7 }
                onOut  { alpha = 1.0 }
                onClick { showAuthDialog(isSignUp = true) }
            }
            text("SIGN UP", textSize = 18.0, color = Colors.WHITE, font = GameAssets.customFont) {
                x = cx + 5.0 + 15.0
                y = 15.0
            }
        }
    }

    /**
     * Popup a simple login/signup dialog.
     */
    private fun showAuthDialog(isSignUp: Boolean) {
        val scene = this
        val dialog = scene.sceneView.container {
            // Full screen dim
            solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) { alpha = 0.85 }
            
            val modalW = 400.0
            val modalH = 340.0
            
            val modal = container {
                roundRect(Size(modalW, modalH), RectCorners(10.0), Colors["#222222"])
                centerOn(this@container)

                text(if (isSignUp) "NEW ACCOUNT" else "LOGIN", textSize = 32.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    centerXOn(this@container)
                    y = 25.0
                }

                val inputW = 320.0
                val inputH = 40.0
                
                val emailInput = uiTextInput("Email Address", Size(inputW, inputH)) {
                    centerXOn(this@container)
                    y = 90.0
                }
                
                val passInput = uiTextInput("Password", Size(inputW, inputH)) {
                    centerXOn(this@container)
                    y = 150.0
                }

                val actionBtn = solidRect(inputW, 55.0, if (isSignUp) Colors.DARKGREEN else Colors.DARKBLUE) {
                    centerXOn(this@container)
                    y = 220.0
                    onOver { alpha = 0.8 }
                    onOut  { alpha = 1.0 }
                    onClick {
                        launchImmediately {
                            val success = if (isSignUp) {
                                AuthManager.signUp(emailInput.text, passInput.text)
                            } else {
                                AuthManager.signIn(emailInput.text, passInput.text)
                            }
                            
                            if (success) {
                                this@container.removeFromParent()
                                authContainer.updateAuthUI()
                            } else {
                                // Simple feedback: shake or turn red (for now just print)
                                println("Auth action failed.")
                            }
                        }
                    }
                }
                text(if (isSignUp) "SUBMIT" else "GO", textSize = 22.0, color = Colors.WHITE, font = GameAssets.customFont) {
                    centerOn(actionBtn)
                }

                text("Cancel", textSize = 16.0, color = Colors.LIGHTGRAY) {
                    centerXOn(this@container)
                    y = 295.0
                    onOver { color = Colors.WHITE }
                    onOut  { color = Colors.LIGHTGRAY }
                    onClick { this@container.removeFromParent() }
                }
            }
        }
    }
}
