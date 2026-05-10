package scenes

import korlibs.image.color.Colors
import korlibs.korge.scene.Scene
import korlibs.korge.ui.*
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

        val cx     = Constants.SCREEN_WIDTH / 2.0
        val inputW = 450.0
        val inputH = 60.0

        // ── Email section ────────────────────────────────────────
        text("Email", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 75.0
        }

        // Email: dark background for the styled display text
        solidRect(inputW, inputH, korlibs.image.color.RGBA(40, 40, 40, 200)) {
            x = cx - inputW / 2.0
            y = 105.0
        }

        // Email: styled display text (mirrors what the user types)
        val emailDisplay = text("", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
            x = cx - inputW / 2.0 + 10.0
            y = 105.0 + (inputH - 24.0) / 2.0
        }

        // Email: invisible real input for keyboard capture
        val emailInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = 105.0
            alpha = 0.0
        }

        // Mirror email keystrokes to the styled display
        var lastEmail = ""
        emailInput.addUpdater {
            if (emailInput.text != lastEmail) {
                lastEmail = emailInput.text
                emailDisplay.text = lastEmail
            }
        }

        // ── Password section ─────────────────────────────────────
        text("Password", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 175.0
        }

        // Track real password string and visibility toggle
        var realPassword      = ""
        var isPasswordVisible = false

        // Password: dark background
        solidRect(inputW, inputH, korlibs.image.color.RGBA(40, 40, 40, 200)) {
            x = cx - inputW / 2.0
            y = 205.0
        }

        // Password: styled display text (shows masked or plain text)
        val passDisplay = text("", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
            x = cx - inputW / 2.0 + 10.0
            y = 205.0 + (inputH - 24.0) / 2.0
        }

        // Password: invisible real input for keyboard capture
        val passInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = 205.0
            alpha = 0.0
        }

        // Mirror password keystrokes to the styled display
        var lastPass = ""
        passInput.addUpdater {
            if (passInput.text != lastPass) {
                lastPass = passInput.text
                realPassword = lastPass
                passDisplay.text = if (isPasswordVisible) realPassword
                                   else "*".repeat(realPassword.length)
            }
        }

        // ── Eye-icon toggle button ───────────────────────────────
        val eyeSize = 48.0
        val eyeIcon = image(GameAssets.manaIconSlice) {
            width  = eyeSize
            height = eyeSize
            x      = cx + inputW / 2.0 + 8.0
            y      = 205.0 + (inputH - eyeSize) / 2.0
        }
        eyeIcon.onClick {
            isPasswordVisible = !isPasswordVisible
            passDisplay.text  = if (isPasswordVisible) realPassword
                                else "*".repeat(realPassword.length)
            eyeIcon.alpha     = if (isPasswordVisible) 1.0 else 0.5
        }
        eyeIcon.alpha = 0.5

        // ── Error / status text ───────────────────────────────────
        val errorText = text("", textSize = 20.0, color = Colors.RED, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 275.0
        }

        val btnW = 220.0
        val btnH = 70.0

        var isLoading = false
        lateinit var loginBtn:  TextButton
        lateinit var signupBtn: TextButton
        lateinit var guestBtn:  TextButton

        fun setLoading(loading: Boolean) {
            isLoading           = loading
            loginBtn.isEnabled  = !loading
            signupBtn.isEnabled = !loading
            guestBtn.isEnabled  = !loading
        }

        // ── Row 1: LOG IN and SIGN UP ────────────────────────────
        loginBtn = TextButton(btnW, btnH, "LOG IN") {
            if (isLoading) return@TextButton
            setLoading(true)
            errorText.text  = "Loading..."
            errorText.color = Colors.YELLOW
            errorText.centerXOn(this@sceneMain)

            launchImmediately {
                val error = AuthManager.signIn(emailInput.text, realPassword)
                if (error == null) {
                    scene.sceneContainer.changeTo { MainMenuScene() }
                } else {
                    setLoading(false)
                    errorText.text  = error
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
            errorText.text  = "Loading..."
            errorText.color = Colors.YELLOW
            errorText.centerXOn(this@sceneMain)

            launchImmediately {
                val error = AuthManager.signUp(emailInput.text, realPassword)
                if (error == null) {
                    scene.sceneContainer.changeTo { MainMenuScene() }
                } else {
                    setLoading(false)
                    errorText.text  = error
                    errorText.color = Colors.RED
                    errorText.centerXOn(this@sceneMain)
                }
            }
        }.apply {
            x = cx + 10.0
            y = 300.0
        }

        // ── Row 2: PLAY AS GUEST (centered, full width) ──────────
        guestBtn = TextButton(btnW * 2 + 20.0, btnH, "PLAY AS GUEST") {
            if (isLoading) return@TextButton
            launchImmediately {
                scene.sceneContainer.changeTo { MainMenuScene() }
            }
        }.apply {
            x = cx - (btnW * 2 + 20.0) / 2.0
            y = 380.0
        }

        addChild(loginBtn)
        addChild(signupBtn)
        addChild(guestBtn)

        // Bring the display texts and eye icon in front of buttons
        addChild(emailDisplay)
        addChild(passDisplay)
        addChild(eyeIcon)

        text("Progress only saved when logged in", textSize = 16.0, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 460.0
        }
    }
}
