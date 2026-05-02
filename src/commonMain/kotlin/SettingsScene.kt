import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.io.async.launchImmediately
import korlibs.io.file.std.resourcesVfs
import korlibs.image.format.readBitmapSlice
import korlibs.korge.input.*
import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn

/**
 * SettingsScene — toggle Show Hitbox and Developer Mode.
 *
 * Both toggles use "ui/buttons/button_bg.png" as their background image,
 * exactly as specified.  The label on each button updates immediately when
 * tapped to reflect the new ON/OFF state.
 *
 * Navigation: Back button → MenuScene.
 */
class SettingsScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@SettingsScene

        // ── Background ─────────────────────────────────────────────────────
        val bgSlice = GameAssets.bg3Slice
        image(bgSlice) {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }

        // Dark overlay
        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) {
            alpha = 0.55
        }

        // ── Title ───────────────────────────────────────────────────────────
        text("SETTINGS", textSize = 70.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 100.0
        }

        // ── Layout constants ─────────────────────────────────────────────────
        val btnW  = 420.0
        val btnH  = 90.0
        val cx    = Constants.SCREEN_WIDTH / 2.0
        val startY = 230.0
        val gapY   = 120.0

        // Load the button background image declared in spec
        val buttonBgSlice = resourcesVfs["ui/buttons/button_bg.png"].readBitmapSlice()

        // ── Helper: build one toggle button ─────────────────────────────────
        //  Returns the label Text so the caller can update it on click.
        fun buildToggleButton(
            labelPrefix: String,
            isOn: () -> Boolean,
            yPos: Double,
            onToggle: () -> Unit
        ): Text {
            // Background image button
            image(buttonBgSlice) {
                width  = btnW
                height = btnH
                x      = cx - btnW / 2.0
                y      = yPos
                smoothing = false
            }

            // Status badge (ON / OFF coloured rect on the right side)
            val badgeW = 90.0
            val badgeH = 48.0
            val badge = solidRect(badgeW, badgeH, RGBA(0, 0, 0, 180)) {
                x = cx + btnW / 2.0 - badgeW - 16.0
                y = yPos + (btnH - badgeH) / 2.0
            }

            // Badge fill colour (green = ON, red = OFF)
            val badgeFill = solidRect(badgeW - 6.0, badgeH - 6.0, Colors.DARKGREEN) {
                x = badge.x + 3.0
                y = badge.y + 3.0
            }

            fun refreshBadge() {
                badgeFill.colorMul = if (isOn()) Colors.GREEN else Colors.RED
            }
            refreshBadge()

            // Badge text (ON / OFF)
            val badgeText = text(if (isOn()) "ON" else "OFF", textSize = 18.0, color = Colors.WHITE, font = GameAssets.customFont) {
                x = badge.x + (badgeW - textBounds.width) / 2.0
                y = badge.y + (badgeH - textBounds.height) / 2.0
            }

            fun refreshBadgeText() {
                badgeText.text = if (isOn()) "ON" else "OFF"
                badgeText.x = badge.x + (badgeW - badgeText.textBounds.width) / 2.0
                badgeText.y = badge.y + (badgeH - badgeText.textBounds.height) / 2.0
            }

            // Label text (left side of button)
            val label = text("$labelPrefix: ${if (isOn()) "ON" else "OFF"}", textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
                x = cx - btnW / 2.0 + 20.0
                y = yPos + (btnH - textBounds.height) / 2.0
            }

            // Invisible hit rect — covers the whole button for clean tap detection
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
            // Hitbox overlays are evaluated every frame via GameSettings.showHitbox,
            // so the change is visible immediately when the game is running.
        }

        // ── Toggle 2: Developer Mode ──────────────────────────────────────────
        buildToggleButton(
            labelPrefix = "Developer Mode",
            isOn        = { GameSettings.developerMode },
            yPos        = startY + gapY
        ) {
            GameSettings.developerMode = !GameSettings.developerMode
            // GameScene reads developerMode each frame; dev-only buttons
            // are shown/hidden reactively — no extra cleanup needed here.
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

        // ── Back Button ──────────────────────────────────────────────────────
        val backBtnW = 220.0
        val backBtnH = 70.0
        solidRect(backBtnW, backBtnH, Colors.DARKRED) {
            x = cx - backBtnW / 2.0
            y = 580.0
            onOver { alpha = 0.7 }
            onOut  { alpha = 1.0 }
            onClick { launchImmediately { scene.sceneContainer.changeTo { MenuScene() } } }
        }
        text("BACK", textSize = 26.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 580.0 + (backBtnH - fontSize) / 2.0
        }
    }
}