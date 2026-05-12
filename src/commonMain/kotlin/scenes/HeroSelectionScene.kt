package scenes

import korlibs.image.bitmap.BmpSlice
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
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
import entities.heroes.HeroRegistry
import entities.heroes.WandererMagicianHero
import managers.GameAssets
import managers.GameSession
import ui.TextButton
import utils.Constants
import utils.SkillConfig
import kotlin.math.max

private data class HeroPickerEntry(
    val id: String,
    val displayName: String,
    val portrait: BmpSlice
)

/**
 * Add entries here as new playable heroes and portraits are available.
 * [portrait] is resolved after [GameAssets.load].
 */
private fun skillIconsForHero(heroId: String): List<BmpSlice> {
    val wm = heroId == WandererMagicianHero.ID
    return listOf(
        GameAssets.attackSlice,
        if (wm) GameAssets.wmSkill1Icon else GameAssets.skill1Slice,
        if (wm) GameAssets.wmSkill2Icon else GameAssets.skill2Slice,
        if (wm) GameAssets.wmSkill3Icon else GameAssets.skill3Slice,
        if (wm) GameAssets.wmSkill4Icon else GameAssets.skill4Slice,
        GameAssets.healingRamenSlice,
        GameAssets.maxHealthSlice
    )
}

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

/** Short line for the hero picker (mana, cooldown, damage, or passive note). */
private fun SkillConfig.selectScreenDescription(): String {
    if (name.equals("Max Health", ignoreCase = true)) {
        return "Raises max HP when unlocked and upgraded."
    }
    val parts = mutableListOf<String>()
    if (manaCost > 0) parts.add("$manaCost MP")
    if (cooldownMax > 0.0) {
        val cd = if (kotlin.math.abs(cooldownMax - cooldownMax.toInt()) < 1e-6) {
            "${cooldownMax.toInt()}s CD"
        } else {
            "%.1fs CD".format(cooldownMax)
        }
        parts.add(cd)
    }
    if (damage > 0.0) parts.add("${damage.toInt()} dmg")
    return parts.joinToString(" · ").ifEmpty { "Passive / no cost." }
}

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
            y = 96.0
        }

        val heroes = heroPickerEntries()
        val portraitSize = 102.0
        val portraitGap = 96.0
        val totalPortraitW = heroes.size * portraitSize + (heroes.size - 1).coerceAtLeast(0) * portraitGap
        val portraitRowX0 = sw / 2.0 - totalPortraitW / 2.0
        /** Below skill list so rows do not overlap portraits. */
        val portraitRowY = 458.0

        val iconSize = 38.0
        val textPadX = iconSize + 10.0
        val rowInnerW = 278.0
        val skillsTopY = 108.0

        heroes.forEachIndexed { col, hero ->
            val portraitCenterX = portraitRowX0 + col * (portraitSize + portraitGap) + portraitSize / 2.0
            val cfg = HeroRegistry.configForSessionSelection(hero.id)
            val icons = skillIconsForHero(hero.id)
            var y = skillsTopY

            cfg.allSkills.forEachIndexed { i, skill ->
                val slice = icons.getOrElse(i) { GameAssets.skill1Slice }
                val descText = skill.selectScreenDescription()
                val wrappedName = wrapToScreenWidth(skill.name, approxCharsPerLine = 20)
                val wrappedDesc = wrapToScreenWidth(descText, approxCharsPerLine = 32)

                var rowH = 0.0
                val row = Container().apply {
                    x = portraitCenterX - rowInnerW / 2.0
                    this.y = y
                    image(slice) {
                        smoothing = true
                        width = iconSize
                        height = iconSize
                        x = 0.0
                        y = 4.0
                    }
                    val nameLbl = text(wrappedName, textSize = 12.0, color = Colors.WHITE, font = GameAssets.customFont) {
                        x = textPadX
                        y = 0.0
                    }
                    val descLbl = text(wrappedDesc, textSize = 9.0, color = RGBA(200, 200, 200, 235), font = GameAssets.customFont) {
                        x = textPadX
                        y = nameLbl.textBounds.height + 3.0
                    }
                    rowH = max(iconSize + 6.0, nameLbl.textBounds.height + 3.0 + descLbl.textBounds.height + 4.0)
                }
                addChild(row)
                y += rowH + 5.0
            }
        }

        val btnW = 260.0
        val btnH = 80.0
        val btnGap = 24.0
        val totalBtnW = btnW * 2 + btnGap

        val backBtn = TextButton(btnW, btnH, "BACK") {
            launchImmediately { scene.sceneContainer.changeTo { MainMenuScene() } }
        }.apply {
            x = cx - totalBtnW / 2.0
            y = 632.0
        }
        addChild(backBtn)

        val startGameBtn = TextButton(btnW, btnH, "START") {
            if (GameSession.selectedHeroId == null) return@TextButton
            launchImmediately { scene.sceneContainer.changeTo { LoadingScene() } }
        }.apply {
            x = cx - totalBtnW / 2.0 + btnW + btnGap
            y = 632.0
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
                x = portraitRowX0 + index * (portraitSize + portraitGap)
                y = portraitRowY
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

            text(hero.displayName, textSize = 20.0, color = Colors.WHITE, font = GameAssets.customFont) {
                centerXOn(portraitBtn)
                y = portraitSize + 10.0
            }.also { cell.addChild(it) }

            addChild(cell)
        }
    }
}
