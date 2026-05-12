package scenes

import korlibs.image.bitmap.BmpSlice
import korlibs.image.color.Colors
import korlibs.io.async.launchImmediately
import korlibs.korge.input.onClick
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.scene.Scene
import korlibs.korge.view.Container
import korlibs.korge.view.SContainer
import korlibs.korge.view.View
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.align.centerXOn
import entities.heroes.FireWizardHero
import entities.heroes.WandererMagicianHero
import managers.GameAssets
import managers.GameSession
import ui.TextButton
import utils.Constants

private data class HeroPickerEntry(
    val id: String,
    val displayName: String,
    val portrait: BmpSlice
)

/**
 * Add entries here as new playable heroes and portraits are available.
 * [portrait] is resolved after [GameAssets.load].
 */
private fun heroPickerEntries(): List<HeroPickerEntry> = listOf(
    HeroPickerEntry(
        id = FireWizardHero.ID,
        displayName = "Fire Wizard",
        portrait = GameAssets.idleFrames.firstOrNull() ?: GameAssets.skill1Slice
    ),
    HeroPickerEntry(
        id = WandererMagicianHero.ID,
        displayName = "Wanderer Magician",
        portrait = GameAssets.wmIdleFrames.firstOrNull() ?: GameAssets.idleFrames.firstOrNull() ?: GameAssets.skill1Slice
    )
)

class HeroSelectionScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@HeroSelectionScene
        GameAssets.loadGlobal()
        GameAssets.loadHeroPortraits()
        GameSession.clearSelectedHero()

        val sw = Constants.SCREEN_WIDTH.toDouble()
        val sh = Constants.SCREEN_HEIGHT.toDouble()
        val cx = Constants.SCREEN_WIDTH / 2.0

        image(GameAssets.bg3Slice) {
            width = sw
            height = sh
            smoothing = true
        }
        solidRect(sw, sh, Colors.BLACK) {
            alpha = 0.4
        }

        text("SELECT HERO", textSize = 56.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 120.0
        }

        val heroes = heroPickerEntries()
        val portraitSize = 140.0
        val gap = 140.0 // Increased from 28.0 to prevent names from overlapping
        val totalW = heroes.size * portraitSize + (heroes.size - 1).coerceAtLeast(0) * gap
        val rowX0 = sw / 2.0 - totalW / 2.0
        val rowY = 240.0

        val btnW = 260.0
        val btnH = 80.0
        val btnGap = 24.0
        val totalBtnW = btnW * 2 + btnGap

        val backBtn = TextButton(btnW, btnH, "BACK") {
            launchImmediately { scene.sceneContainer.changeTo { MainMenuScene() } }
        }.apply {
            x = cx - totalBtnW / 2.0
            y = 520.0
        }
        addChild(backBtn)

        val startGameBtn = TextButton(btnW, btnH, "START") {
            if (GameSession.selectedHeroId == null) return@TextButton
            launchImmediately { scene.sceneContainer.changeTo { LoadingScene() } }
        }.apply {
            x = cx - totalBtnW / 2.0 + btnW + btnGap
            y = 520.0
            visible = false
        }
        addChild(startGameBtn)

        val selectionRings = mutableListOf<View>()

        heroes.forEachIndexed { index, hero ->
            val ring = solidRect(portraitSize + 14.0, portraitSize + 14.0, Colors["#FFCC00"]) {
                alpha = 0.55
                visible = false
                x = -7.0
                y = -7.0
            }
            selectionRings += ring

            val cell = Container().apply {
                x = rowX0 + index * (portraitSize + gap)
                y = rowY
            }
            cell.addChild(ring)

            val portraitBtn = image(hero.portrait) {
                width = portraitSize
                height = portraitSize
                smoothing = true
                onOver { alpha = 0.85 }
                onOut { alpha = 1.0 }
                onClick {
                    GameSession.setSelectedHero(hero.id)
                    selectionRings.forEach { it.visible = false }
                    selectionRings[index].visible = true
                    startGameBtn.visible = true
                }
            }
            cell.addChild(portraitBtn)

            text(hero.displayName, textSize = 22.0, color = Colors.WHITE, font = GameAssets.customFont) {
                centerXOn(portraitBtn)
                y = portraitSize + 10.0
            }.also { cell.addChild(it) }

            addChild(cell)
        }
    }
}
