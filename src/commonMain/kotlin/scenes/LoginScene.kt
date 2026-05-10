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

        // ── Background ───────────────────────────────────────────
        val bgSlice = GameAssets.bg3Slice
        image(bgSlice).apply {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }
        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK).apply {
            alpha = 0.55
        }

        // ── Layout constants ─────────────────────────────────────
        // Panel is wider and taller to fill the empty space.
        val cx     = Constants.SCREEN_WIDTH / 2.0
        val inputW = 580.0   // was 450
        val inputH = 72.0    // was 60
        val labelSize  = 32.0  // was 24
        val titleSize  = 60.0  // was 40
        val errorSize  = 26.0  // was 20  — large enough to read at a glance
        val noteSize   = 20.0  // was 16

        // Vertical rhythm — every section has generous padding
        val titleY     = 60.0
        val emailLabelY = 160.0
        val emailInputY = emailLabelY + 44.0          // label → input gap: 44 px
        val passLabelY  = emailInputY + inputH + 50.0 // input → next label gap: 50 px
        val passInputY  = passLabelY + 44.0
        val errorY      = passInputY + inputH + 36.0  // generous space before error
        val row1BtnY    = errorY + 58.0               // error → buttons gap: 58 px
        val row2BtnY    = row1BtnY + 100.0            // row gap between button rows
        val noteY       = row2BtnY + 100.0

        // ── Title ────────────────────────────────────────────────
        text("Login / Sign Up", textSize = titleSize, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = titleY
        }

        // ── Email section ────────────────────────────────────────
        text("Email", textSize = labelSize, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = emailLabelY
        }
        val emailInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = emailInputY
        }

        // ── Password section ─────────────────────────────────────
        text("Password", textSize = labelSize, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = passLabelY
        }

        var realPassword      = ""
        var isPasswordVisible = false
        var isUpdatingPass    = false
        var lastPassLen       = 0

        val passInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = passInputY
        }

        // Mask/unmask password as user types
        passInput.addUpdater {
            if (isUpdatingPass) return@addUpdater
            val currentText = passInput.text
            if (currentText.length != lastPassLen) {
                isUpdatingPass = true
                if (currentText.length > lastPassLen) {
                    val newChars = currentText.substring(lastPassLen)
                    realPassword += newChars
                } else {
                    realPassword = realPassword.take(currentText.length)
                }
                lastPassLen = realPassword.length
                passInput.text = if (isPasswordVisible) realPassword
                                 else "*".repeat(realPassword.length)
                isUpdatingPass = false
            }
        }

        // ── Eye-icon toggle button ───────────────────────────────
        val eyeSize = 56.0   // slightly larger so it's easy to tap
        val eyeIcon = image(GameAssets.manaIconSlice) {
            width  = eyeSize
            height = eyeSize
            x      = cx + inputW / 2.0 + 12.0
            y      = passInputY + (inputH - eyeSize) / 2.0
        }
        eyeIcon.onClick {
            isPasswordVisible = !isPasswordVisible
            isUpdatingPass = true
            passInput.text = if (isPasswordVisible) realPassword
                             else "*".repeat(realPassword.length)
            lastPassLen = realPassword.length
            isUpdatingPass = false
            eyeIcon.alpha = if (isPasswordVisible) 1.0 else 0.5
        }
        eyeIcon.alpha = 0.5

        // ── Error / status text ──────────────────────────────────
        // Large, centred, clearly visible — yellow while loading, red on error.
        val errorText = text("", textSize = errorSize, color = Colors.RED, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = errorY
        }

        // ── Buttons ──────────────────────────────────────────────
        val btnW = 260.0
        val btnH = 80.0

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

        fun showError(msg: String, isInfo: Boolean = false) {
            errorText.text  = msg
            errorText.color = if (isInfo) Colors.YELLOW else Colors.RED
            errorText.centerXOn(this@sceneMain)
        }

        // ── Row 1: LOG IN  |  SIGN UP ────────────────────────────
        loginBtn = TextButton(btnW, btnH, "LOG IN") {
            if (isLoading) return@TextButton
            setLoading(true)
            showError("Signing in…", isInfo = true)

            launchImmediately {
                val error = AuthManager.signIn(emailInput.text.trim(), realPassword)
                if (error == null) {
                    scene.sceneContainer.changeTo { MainMenuScene() }
                } else {
                    setLoading(false)
                    showError(error)
                }
            }
        }.apply {
            x = cx - btnW - 14.0   // small gap in the centre
            y = row1BtnY
        }

        signupBtn = TextButton(btnW, btnH, "SIGN UP") {
            if (isLoading) return@TextButton
            setLoading(true)
            showError("Creating account…", isInfo = true)

            launchImmediately {
                val error = AuthManager.signUp(emailInput.text.trim(), realPassword)
                if (error == null) {
                    scene.sceneContainer.changeTo { MainMenuScene() }
                } else {
                    setLoading(false)
                    showError(error)
                }
            }
        }.apply {
            x = cx + 14.0
            y = row1BtnY
        }

        // ── Row 2: PLAY AS GUEST (full width, centred) ───────────
        guestBtn = TextButton(btnW * 2 + 28.0, btnH, "PLAY AS GUEST") {
            if (isLoading) return@TextButton
            launchImmediately { scene.sceneContainer.changeTo { MainMenuScene() } }
        }.apply {
            x = cx - (btnW * 2 + 28.0) / 2.0
            y = row2BtnY
        }

        addChild(loginBtn)
        addChild(signupBtn)
        addChild(guestBtn)

        // ── Bottom note ──────────────────────────────────────────
        text("Progress is only saved when logged in", textSize = noteSize, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = noteY
        }
    }
}