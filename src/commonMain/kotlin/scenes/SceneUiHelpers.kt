package scenes

import korlibs.image.color.Colors
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.korge.view.text
import managers.GameAssets
import utils.GameSettings

/**
 * Top-left warning when dev or hitbox debug is enabled (progress may not save as expected).
 * Updates every frame so toggles in [SettingsScene] reflect immediately; add this view last
 * in the scene (or keep it above fullscreen backgrounds) so it stays visible.
 */
internal fun SContainer.addNoSaveProgressWarningIfNeeded(padX: Double = 14.0, padY: Double = 12.0) {
    val textSize = 15.0
    val lineGap = 6.0
    val devLine = text("", textSize = textSize, color = Colors.RED, font = GameAssets.customFont) {
        visible = false
    }
    val hitLine = text("", textSize = textSize, color = Colors.RED, font = GameAssets.customFont) {
        visible = false
    }
    addUpdater {
        var y = padY
        if (GameSettings.developerMode) {
            devLine.text = "Developer Mode is turned on. Progress cannot be saved."
            devLine.visible = true
            devLine.x = padX
            devLine.y = y
            y += devLine.textBounds.height + lineGap
        } else {
            devLine.visible = false
        }
        if (GameSettings.showHitbox) {
            hitLine.text = "Show Hitbox is turned on. Progress cannot be saved."
            hitLine.visible = true
            hitLine.x = padX
            hitLine.y = y
        } else {
            hitLine.visible = false
        }
    }
}
