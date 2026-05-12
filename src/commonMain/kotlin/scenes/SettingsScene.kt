package scenes

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.io.async.launchImmediately
import korlibs.korge.input.*
import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import managers.GameAssets
import utils.Constants
import utils.GameSettings

/**
 * SettingsScene — toggle Show Hitbox and Developer Mode.
 *
 * Log in / log out is on the main menu (below Play).
 *
 * Navigation: Back button → MenuScene.
 */
class SettingsScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@SettingsScene

        // ── Background ──────────────────────────────────────────────────────
        val bgSlice = GameAssets.bg3Slice
        image(bgSlice) {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }
        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) {
            alpha = 0.55
        }

        // ── Title ───────────────────────────────────────────────────────────
        text("SETTINGS", textSize = 70.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 100.0
        }

        // ── Layout constants ─────────────────────────────────────────────────
        val btnW   = 420.0
        val btnH   = 90.0
        val cx     = Constants.SCREEN_WIDTH / 2.0
        val startY = 230.0
        val gapY   = 120.0

        val buttonBgSlice = GameAssets.buttonBgSlice

        // ── Helper: build one toggle button ─────────────────────────────────
        fun buildToggleButton(
            labelPrefix: String,
            isOn: () -> Boolean,
            yPos: Double,
            onToggle: () -> Unit
        ): Text {
            image(buttonBgSlice) {
                width     = btnW
                height    = btnH
                x         = cx - btnW / 2.0
                y         = yPos
                smoothing = false
            }

            val badgeW = 90.0
            val badgeH = 48.0
            val badge = solidRect(badgeW, badgeH, RGBA(0, 0, 0, 180)) {
                x = cx + btnW / 2.0 - badgeW - 16.0
                y = yPos + (btnH - badgeH) / 2.0
            }
            val badgeFill = solidRect(badgeW - 6.0, badgeH - 6.0, Colors.DARKGREEN) {
                x = badge.x + 3.0
                y = badge.y + 3.0
            }

            fun refreshBadge() {
                badgeFill.colorMul = if (isOn()) Colors.GREEN else Colors.RED
            }
            refreshBadge()

            val badgeText = text(if (isOn()) "ON" else "OFF", textSize = 18.0, color = Colors.WHITE, font = GameAssets.customFont) {
                x = badge.x + (badgeW - textBounds.width) / 2.0
                y = badge.y + (badgeH - textBounds.height) / 2.0
            }

            fun refreshBadgeText() {
                badgeText.text = if (isOn()) "ON" else "OFF"
                badgeText.x = badge.x + (badgeW - badgeText.textBounds.width) / 2.0
                badgeText.y = badge.y + (badgeH - badgeText.textBounds.height) / 2.0
            }

            val label = text("$labelPrefix: ${if (isOn()) "ON" else "OFF"}", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
                x = cx - btnW / 2.0 + 20.0
                y = yPos + (btnH - textBounds.height) / 2.0
            }

            solidRect(btnW, btnH, RGBA(0, 0, 0, 0)) {
                x = cx - btnW / 2.0
                y = yPos
                onClick {
                    onToggle()
                    label.text = "$labelPrefix: ${if (isOn()) "ON" else "OFF"}"
                    refreshBadge()
                    refreshBadgeText()
                }
                onOver { alpha = 0.15 }
                onOut  { alpha = 0.0  }
            }

            return label
        }

        // ── Toggle 1: Show Hitbox ─────────────────────────────────────────────
        buildToggleButton(
            labelPrefix = "Show Hitbox",
            isOn        = { GameSettings.showHitbox },
            yPos        = startY
        ) {
            GameSettings.showHitbox = !GameSettings.showHitbox
        }

        // ── Toggle 2: Developer Mode ──────────────────────────────────────────
        buildToggleButton(
            labelPrefix = "Developer Mode",
            isOn        = { GameSettings.developerMode },
            yPos        = startY + gapY
        ) {
            GameSettings.developerMode = !GameSettings.developerMode
        }

        // ── Info text ─────────────────────────────────────────────────────────
        text("Dev Mode unlocks: infinite upgrades, Level Up & Next Wave buttons.", textSize = 16.0, color = RGBA(200, 200, 200, 200), font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = startY + gapY * 2 + 20.0
        }
        text("Changes take effect immediately in the next game session.", textSize = 14.0, color = RGBA(160, 160, 160, 180), font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = startY + gapY * 2 + 52.0
        }

        // ── Bottom: Back ─────────────────────────────────────────────────────
        val backBtnW = 220.0
        val backBtnH = 70.0
        val backBtn = ui.TextButton(backBtnW, backBtnH, "BACK") {
            launchImmediately { scene.sceneContainer.changeTo { MenuScene() } }
        }.apply {
            x = cx - backBtnW / 2.0
            y = 560.0
        }
        addChild(backBtn)

        addNoSaveProgressWarningIfNeeded()
    }
}