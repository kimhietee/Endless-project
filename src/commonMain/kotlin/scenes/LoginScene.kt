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

class LoginScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        GameAssets.load()
        val scene = this@LoginScene
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
        val inputH = 72.0
        val labelSize = 32.0
        val titleSize = 40.0
        val subtitleSize = 26.0
        val errorSize = 24.0
        val noteSize = 18.0

        val btnW = minOf(260.0, (sw - margin * 2 - 28) / 2)
        val btnH = 80.0
        val cornerBtnW = minOf(220.0, sw - margin * 2)

        val bottomRowY = sh - btnH - margin
        val mainBtnY = bottomRowY - 88.0
        val errorY = mainBtnY - 38.0
        val passInputY = errorY - 14.0 - inputH
        val passLabelY = passInputY - 36.0
        val emailInputY = passLabelY - 28.0 - inputH
        val emailLabelY = emailInputY - 36.0

        val titleY = 36.0
        val headerGap = 6.0
        val noteGap = 10.0

        text("Login", textSize = titleSize, color = Colors.WHITE, font = GameAssets.customFont) {
            y = titleY
            centerXOn(this@sceneMain)
        }
//        text("Sign up", textSize = subtitleSize, color = Colors.LIGHTGRAY, font = GameAssets.customFont) {
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
            eyeSlice = GameAssets.eyeIconSlice
        )

        val errorText = text("", textSize = errorSize, color = Colors.RED, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = errorY
        }

        var isLoading = false
        lateinit var loginBtn: TextButton
        lateinit var guestBtn: TextButton
        lateinit var signUpNav: TextButton

        fun setLoading(loading: Boolean) {
            isLoading = loading
            loginBtn.isEnabled = !loading
            guestBtn.isEnabled = !loading
            signUpNav.isEnabled = !loading
        }

        fun showError(msg: String, isInfo: Boolean = false) {
            errorText.text = wrapToScreenWidth(msg, approxCharsPerLine = 48)
            errorText.color = if (isInfo) Colors.YELLOW else Colors.RED
            errorText.centerXOn(this@sceneMain)
        }

        loginBtn = TextButton(btnW, btnH, "LOG IN") {
            if (isLoading) return@TextButton
            setLoading(true)
            showError("Signing in…", isInfo = true)

            launchImmediately {
                val error = AuthManager.signIn(emailInput.text.trim(), passMasked.realPassword)
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

        signUpNav = TextButton(cornerBtnW, btnH, "SIGN UP") {
            if (isLoading) return@TextButton
            launchImmediately { scene.sceneContainer.changeTo { SignUpScene() } }
        }.apply {
            x = margin
            y = bottomRowY
        }

        addChild(loginBtn)
        addChild(guestBtn)
        addChild(signUpNav)
    }
}

/** Rough word-wrap so status/error lines stay within the screen. */
internal fun wrapToScreenWidth(msg: String, approxCharsPerLine: Int): String {
    if (msg.length <= approxCharsPerLine) return msg
    val words = msg.split(Regex("\\s+"))
    val lines = mutableListOf<String>()
    var line = ""
    for (w in words) {
        val tryLine = if (line.isEmpty()) w else "$line $w"
        if (tryLine.length <= approxCharsPerLine) line = tryLine
        else {
            if (line.isNotEmpty()) lines.add(line)
            line = w
        }
    }
    if (line.isNotEmpty()) lines.add(line)
    return lines.joinToString("\n")
}
