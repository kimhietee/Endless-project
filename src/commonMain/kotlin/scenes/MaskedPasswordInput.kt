package scenes

import korlibs.image.bitmap.BmpSlice
import korlibs.korge.input.onClick
import korlibs.korge.ui.UITextInput
import korlibs.korge.ui.uiTextInput
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.math.geom.Size

/**
 * Password field that masks with '*' and optional visibility toggle via [eyeIcon].
 */
class MaskedPasswordInput(
    private val passInput: UITextInput,
    private val eyeIcon: Image
) {
    var realPassword: String = ""
        private set
    private var isPasswordVisible = false
    private var isUpdatingPass = false
    private var lastPassLen = 0

    init {
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
    }

}

fun SContainer.addMaskedPasswordRow(
    sceneRoot: SContainer,
    cx: Double,
    passInputY: Double,
    inputW: Double,
    inputH: Double,
    eyeSlice: BmpSlice
): Pair<UITextInput, MaskedPasswordInput> {
    val passInput = uiTextInput("", Size(inputW, inputH)) {
        centerXOn(sceneRoot)
        y = passInputY
    }
    val eyeSize = 56.0
    val eyeIcon = image(eyeSlice) {
        width = eyeSize
        height = eyeSize
        x = cx + inputW / 2.0 + 12.0
        y = passInputY + (inputH - eyeSize) / 2.0
    }
    return passInput to MaskedPasswordInput(passInput, eyeIcon)
}
