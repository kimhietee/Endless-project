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

        text("Endless Journey", textSize = 70.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 80.0
        }
        
        // Auto-login check
        if (AuthManager.isLoggedIn()) {
            scene.sceneContainer.changeTo { MainMenuScene() }
            return
        }

        val cx = Constants.SCREEN_WIDTH / 2.0

        val inputW = 320.0
        val inputH = 40.0
        
        // Email label
        text("Email", textSize = 20.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 190.0
        }
        
        val emailInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = 200.0
        }
        
        // Password label
        text("Password", textSize = 20.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 250.0
        }
        
        val passInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = 260.0
        }

        val errorText = text("", textSize = 16.0, color = Colors.RED, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 320.0
        }

        val btnW = 200.0
        val btnH = 60.0

        var isLoading = false
        lateinit var loginBtn: TextButton
        lateinit var signupBtn: TextButton
        lateinit var guestBtn: TextButton

        fun setLoading(loading: Boolean) {
            isLoading = loading
            loginBtn.isEnabled = !loading
            signupBtn.isEnabled = !loading
            guestBtn.isEnabled = !loading
        }

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
            y = 360.0
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
            y = 360.0
        }

        guestBtn = TextButton(btnW * 1.5, btnH, "PLAY AS GUEST") {
            if (isLoading) return@TextButton
            launchImmediately {
                scene.sceneContainer.changeTo { MainMenuScene() }
            }
        }.apply {
            centerXOn(this@sceneMain)
            y = 450.0
        }

        text("Progress will not be saved", textSize = 14.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 520.0
        }

        addChild(loginBtn)
        addChild(signupBtn)
        addChild(guestBtn)
    }
}
