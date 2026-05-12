package scenes

import korlibs.image.color.Colors
import korlibs.korge.view.SContainer
import korlibs.korge.view.text
import managers.GameAssets
import utils.GameSettings

/**
 * Top-left warning when dev or hitbox debug is enabled (progress may not save as expected).
 */
internal fun SContainer.addNoSaveProgressWarningIfNeeded(padX: Double = 14.0, padY: Double = 12.0) {
    if (!GameSettings.developerMode && !GameSettings.showHitbox) return
    val lines = buildList {
        if (GameSettings.developerMode) {
            add("Developer Mode is turned on. Progress cannot be saved.")
        }
        if (GameSettings.showHitbox) {
            add("Show Hitbox is turned on. Progress cannot be saved.")
        }
    }
    val textSize = 15.0
    val lineGap = 6.0
    var y = padY
    for (line in lines) {
        val t = text(line, textSize = textSize, color = Colors.RED, font = GameAssets.customFont) {
            x = padX
            this.y = y
        }
        y += t.textBounds.height + lineGap
    }
}
