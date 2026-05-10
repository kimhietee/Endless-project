package scenes

import korlibs.image.color.Colors
import korlibs.korge.scene.Scene
import korlibs.korge.ui.uiTextInput
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.korge.input.*
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

        // If already logged in, skip to main menu
        if (AuthManager.isLoggedIn()) {
            launchImmediately { scene.sceneContainer.changeTo { MainMenuScene() } }
            return
        }

        val bgSlice = GameAssets.bg3Slice
        image(bgSlice).apply {
            width = Constants.SCREEN_WIDTH.toDouble()
            height = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }

        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK).apply {
            alpha = 0.50
        }

        // REDESIGN: All content centered and spaced
        val cx = Constants.SCREEN_WIDTH / 2.0
        
        text("Welcome to Endless", textSize = 60.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 60.0
        }

        val inputW = 500.0
        val inputH = 70.0
        val labelSize = 28.0
        val inputFontSize = 32.0
        val vGap = 120.0
        var currentY = 160.0

        // --- Email Section ---
        text("Email:", textSize = labelSize, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = currentY
        }
        currentY += 40.0
        
        // Background for email input
        solidRect(inputW, inputH, korlibs.image.color.RGBA(30, 30, 30, 200)) {
            centerXOn(this@sceneMain)
            y = currentY
        }
        
        val emailDisplay = text("", textSize = inputFontSize, color = Colors.WHITE, font = GameAssets.customFont) {
            x = cx - inputW / 2.0 + 15.0
            y = currentY + (inputH - inputFontSize) / 2.0
        }
        
        val emailInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = currentY
            alpha = 0.0 // capture input but hidden
        }
        emailInput.onTextChanged {
            emailDisplay.text = it
        }
        
        currentY += vGap

        // --- Password Section ---
        text("Password:", textSize = labelSize, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = currentY
        }
        currentY += 40.0
        
        // Background for password input
        solidRect(inputW, inputH, korlibs.image.color.RGBA(30, 30, 30, 200)) {
            centerXOn(this@sceneMain)
            y = currentY
        }
        
        var realPassword = ""
        var isPasswordVisible = false
        
        val passDisplay = text("", textSize = inputFontSize, color = Colors.WHITE, font = GameAssets.customFont) {
            x = cx - inputW / 2.0 + 15.0
            y = currentY + (inputH - inputFontSize) / 2.0
        }
        
        val passInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = currentY
            alpha = 0.0 // capture input but hidden
        }
        
        fun refreshPasswordDisplay() {
            passDisplay.text = if (isPasswordVisible) realPassword else "*".repeat(realPassword.length)
        }
        
        passInput.onTextChanged {
            realPassword = it
            refreshPasswordDisplay()
        }
        
        // Eye icon toggle
        val eyeSize = 50.0
        val eyeBtn = image(GameAssets.skill4Slice) {
            width = eyeSize
            height = eyeSize
            x = cx + inputW / 2.0 + 10.0
            y = currentY + (inputH - eyeSize) / 2.0
            alpha = 0.6
            onClick {
                isPasswordVisible = !isPasswordVisible
                alpha = if (isPasswordVisible) 1.0 else 0.6
                refreshPasswordDisplay()
            }
        }
        
        currentY += vGap

        // --- Error Text ---
        val errorText = text("", textSize = 22.0, color = Colors.RED, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = currentY - 30.0
        }

        // --- Buttons ---
        val btnW = 340.0
        val btnH = 80.0
        
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
            errorText.text = "Logging in..."
            errorText.color = Colors.YELLOW
            errorText.centerXOn(this@sceneMain)
            
            launchImmediately {
                val error = AuthManager.signIn(emailInput.text, realPassword)
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
            centerXOn(this@sceneMain)
            y = currentY
        }
        
        currentY += 100.0

        signupBtn = TextButton(btnW, btnH, "SIGN UP") {
            if (isLoading) return@TextButton
            setLoading(true)
            errorText.text = "Signing up..."
            errorText.color = Colors.YELLOW
            errorText.centerXOn(this@sceneMain)
            
            launchImmediately {
                val error = AuthManager.signUp(emailInput.text, realPassword)
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
            centerXOn(this@sceneMain)
            y = currentY
        }
        
        currentY += 100.0

        guestBtn = TextButton(btnW, btnH, "PLAY AS GUEST") {
            if (isLoading) return@TextButton
            launchImmediately {
                // For guest, we just transition to main menu (AuthManager.isLoggedIn will be false)
                scene.sceneContainer.changeTo { MainMenuScene() }
            }
        }.apply {
            centerXOn(this@sceneMain)
            y = currentY
        }

        addChild(loginBtn)
        addChild(signupBtn)
        addChild(guestBtn)
        
        // Info text
        text("Progress only saved when logged in", textSize = 18.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = currentY + 100.0
        }
    }
}
