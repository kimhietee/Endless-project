package scenes

import korlibs.image.color.Colors
import korlibs.korge.scene.Scene
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.io.async.launchImmediately
import korlibs.math.geom.Size
import managers.AuthManager
import managers.GameAssets
import ui.TextButton
import utils.Constants

class SignUpScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        GameAssets.load()
        val scene = this@SignUpScene
        val sw = Constants.SCREEN_WIDTH.toDouble()
        val sh = Constants.SCREEN_HEIGHT.toDouble()

        image(GameAssets.bg3Slice).apply {
            width = sw
            height = sh
            smoothing = true
        }
        solidRect(sw, sh, Colors.BLACK).apply {
            alpha = 0.55
        }

        val cx = sw / 2.0
        val margin = 60.0
        val inputW = minOf(580.0, sw - margin * 2)
        val inputH = 68.0
        val labelSize = 28.0
        val titleSize = 38.0
        val subtitleSize = 26.0
        val errorSize = 22.0
        val noteSize = 18.0

        val btnW = minOf(260.0, (sw - margin * 2 - 28) / 2)
        val btnH = 80.0
        val cornerBtnW = minOf(220.0, sw - margin * 2)

        val bottomRowY = sh - btnH - margin
        val mainBtnY = bottomRowY - 84.0
        val errorY = mainBtnY - 32.0
        val confirmInputY = errorY - 12.0 - inputH
        val confirmLabelY = confirmInputY - 32.0
        val passInputY = confirmLabelY - 26.0 - inputH
        val passLabelY = passInputY - 32.0
        val emailInputY = passLabelY - 26.0 - inputH
        val emailLabelY = emailInputY - 32.0

        val titleY = 32.0
        val headerGap = 6.0
        val noteGap = 10.0

        text("Sign up", textSize = titleSize, color = Colors.WHITE, font = GameAssets.customFont) {
            y = titleY
            centerXOn(this@sceneMain)
        }
//        text("Log in", textSize = subtitleSize, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
//            y = titleY + titleSize + headerGap
//            centerXOn(this@sceneMain)
//        }
        text(
            wrapToScreenWidth("Progress is only saved when logged in", approxCharsPerLine = 52),
            textSize = noteSize,
            color = Colors.LIGHTGRAY,
            font = GameAssets.customFont
        ) {
            y = titleY + titleSize + headerGap // + subtitleSize + noteGap
            centerXOn(this@sceneMain)
        }

        text("Email", textSize = labelSize, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = emailLabelY
        }
        val emailInput = uiTextInput("", Size(inputW, inputH)) {
            centerXOn(this@sceneMain)
            y = emailInputY
        }

        text("Password", textSize = labelSize, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = passLabelY
        }
        val (_, passMasked) = addMaskedPasswordRow(
            sceneRoot = this@sceneMain,
            cx = cx,
            passInputY = passInputY,
            inputW = inputW,
            inputH = inputH,
            eyeSlice = GameAssets.manaIconSlice
        )

        text("Re-type password", textSize = labelSize, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = confirmLabelY
        }
        val (_, confirmMasked) = addMaskedPasswordRow(
            sceneRoot = this@sceneMain,
            cx = cx,
            passInputY = confirmInputY,
            inputW = inputW,
            inputH = inputH,
            eyeSlice = GameAssets.manaIconSlice
        )

        val errorText = text("", textSize = errorSize, color = Colors.RED, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = errorY
        }

        var isLoading = false
        lateinit var signUpBtn: TextButton
        lateinit var guestBtn: TextButton
        lateinit var loginNav: TextButton

        fun setLoading(loading: Boolean) {
            isLoading = loading
            signUpBtn.isEnabled = !loading
            guestBtn.isEnabled = !loading
            loginNav.isEnabled = !loading
        }

        fun showError(msg: String, isInfo: Boolean = false) {
            errorText.text = wrapToScreenWidth(msg, approxCharsPerLine = 48)
            errorText.color = if (isInfo) Colors.YELLOW else Colors.RED
            errorText.centerXOn(this@sceneMain)
        }

        signUpBtn = TextButton(btnW, btnH, "SIGN UP") {
            if (isLoading) return@TextButton
            if (passMasked.realPassword != confirmMasked.realPassword) {
                showError("Passwords do not match. Please re-type them.")
                return@TextButton
            }
            setLoading(true)
            showError("Creating account…", isInfo = true)

            launchImmediately {
                val error = AuthManager.signUp(emailInput.text.trim(), passMasked.realPassword)
                if (error == null) {
                    scene.sceneContainer.changeTo { MainMenuScene() }
                } else {
                    setLoading(false)
                    showError(error)
                }
            }
        }.apply {
            centerXOn(this@sceneMain)
            y = mainBtnY
        }

        val guestW = sw - margin * 2.0 - cornerBtnW - 16.0
        guestBtn = TextButton(guestW, btnH, "PLAY AS GUEST") {
            if (isLoading) return@TextButton
            launchImmediately {
                AuthManager.logout()
                scene.sceneContainer.changeTo { MainMenuScene() }
            }
        }.apply {
            x = margin + cornerBtnW + 16.0
            y = bottomRowY
        }

        loginNav = TextButton(cornerBtnW, btnH, "LOG IN") {
            if (isLoading) return@TextButton
            launchImmediately { scene.sceneContainer.changeTo { LoginScene() } }
        }.apply {
            x = margin
            y = bottomRowY
        }

        addChild(signUpBtn)
        addChild(guestBtn)
        addChild(loginNav)
    }
}
