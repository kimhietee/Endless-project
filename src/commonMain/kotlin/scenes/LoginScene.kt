package scenes

import korlibs.image.color.Colors
import korlibs.korge.scene.Scene
import korlibs.korge.ui.uiTextInput
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.io.async.launchImmediately
import korlibs.math.geom.Size
import managers.AuthManager
import managers.GameAssets
import ui.TextButton
import utils.Constants

class LoginScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        GameAssets.load()
        val scene = this@LoginScene

        val bgSlice = GameAssets.bg3Slice
        image(bgSlice).apply {
            width = Constants.SCREEN_WIDTH.toDouble()
            height = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }

        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK).apply {
            alpha = 0.50
        }

        // REDESIGN: All content moved to the TOP half of the screen (above 360px)
        // to avoid being covered by the Android soft keyboard.

        text("Login / Signup", textSize = 40.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 20.0
        }
        
        val cx = Constants.SCREEN_WIDTH / 2.0
        val inputW = 450.0 // Wider for better visibility
        val inputH = 60.0  // Taller for easier tapping
        
        // Email section
        text("Email", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 75.0
        }
        
        val emailInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = 105.0
        }
        
        // Password section
        text("Password", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 175.0
        }
        
        val passInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = 205.0
        }

        val errorText = text("", textSize = 20.0, color = Colors.RED, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 275.0
        }

        val btnW = 220.0
        val btnH = 70.0

        var isLoading = false
        lateinit var loginBtn: TextButton
        lateinit var signupBtn: TextButton
        lateinit var guestBtn: TextButton
        lateinit var backBtn: TextButton

        fun setLoading(loading: Boolean) {
            isLoading = loading
            loginBtn.isEnabled = !loading
            signupBtn.isEnabled = !loading
            guestBtn.isEnabled = !loading
            backBtn.isEnabled = !loading
        }

        // Row 1: LOG IN and SIGN UP
        loginBtn = TextButton(btnW, btnH, "LOG IN") {
            if (isLoading) return@TextButton
            setLoading(true)
            errorText.text = "Loading..."
            errorText.color = Colors.YELLOW
            errorText.centerXOn(this@sceneMain)
            
            launchImmediately {
                val error = AuthManager.signIn(emailInput.text, passInput.text)
                if (error == null) {
                    scene.sceneContainer.changeTo { MainMenuScene() }
                } else {
                    setLoading(false)
                    errorText.text = error
                    errorText.color = Colors.RED
                    errorText.centerXOn(this@sceneMain)
                }
            }
        }.apply {
            x = cx - btnW - 10.0
            y = 300.0
        }

        signupBtn = TextButton(btnW, btnH, "SIGN UP") {
            if (isLoading) return@TextButton
            setLoading(true)
            errorText.text = "Loading..."
            errorText.color = Colors.YELLOW
            errorText.centerXOn(this@sceneMain)
            
            launchImmediately {
                val error = AuthManager.signUp(emailInput.text, passInput.text)
                if (error == null) {
                    scene.sceneContainer.changeTo { MainMenuScene() }
                } else {
                    setLoading(false)
                    errorText.text = error
                    errorText.color = Colors.RED
                    errorText.centerXOn(this@sceneMain)
                }
            }
        }.apply {
            x = cx + 10.0
            y = 300.0
        }

        // Row 2: BACK and PLAY AS GUEST
        backBtn = TextButton(btnW, btnH, "BACK") {
            if (isLoading) return@TextButton
            launchImmediately {
                scene.sceneContainer.changeTo { MainMenuScene() }
            }
        }.apply {
            x = cx - btnW - 10.0
            y = 380.0
        }

        guestBtn = TextButton(btnW, btnH, "GUEST") {
            if (isLoading) return@TextButton
            launchImmediately {
                scene.sceneContainer.changeTo { MainMenuScene() }
            }
        }.apply {
            x = cx + 10.0
            y = 380.0
        }

        addChild(loginBtn)
        addChild(signupBtn)
        addChild(backBtn)
        addChild(guestBtn)
        
        text("Progress only saved when logged in", textSize = 16.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 460.0 // Info text can be lower as it's not interactive
        }
    }
}
