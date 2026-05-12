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
import kotlin.math.min

private data class HeroPickerEntry(
    val id: String,
    val displayName: String,
    val portrait: BmpSlice
)

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
    if (damage > 0.0) parts.add("${damage.toInt()} Damage")
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
        val portraitRowY = 458.0

        fun portraitLeft(index: Int) = portraitRowX0 + index * (portraitSize + portraitGap)

        val edgePad = 20.0
        val portraitTextGap = 12.0

        fun skillRowLayout(heroIndex: Int): Pair<Double, Double> {
            return if (heroIndex == 0) {
                val innerW = (portraitLeft(0) - edgePad - portraitTextGap).coerceAtLeast(120.0)
                Pair(edgePad, innerW)
            } else {
                val anchorCx = (portraitLeft(1) + sw) / 2.0
                val maxInner = sw - portraitLeft(1) - edgePad
                val innerW = min(292.0, maxInner).coerceAtLeast(120.0)
                Pair(anchorCx - innerW / 2.0, innerW)
            }
        }

        val iconSize = 36.0
        val iconTextGap = 10.0
        val textPadX = iconSize + iconTextGap
        val skillsTopY = 150.0
        val nameLineH = 18.0
        val descLineH = 14.0

        val skillPanel = Container()

        fun clearSkillPanel() {
            skillPanel.children.toList().forEach { it.removeFromParent() }
        }

        fun populateSkillPanel(heroId: String, heroIndex: Int) {
            clearSkillPanel()
            val (rowX, rowInnerW) = skillRowLayout(heroIndex)
            val textAreaW = rowInnerW - textPadX
            val nameChars = ((textAreaW) / 6.8).toInt().coerceIn(8, 32)
            val descChars = ((textAreaW) / 5.0).toInt().coerceIn(12, 44)
            val cfg = HeroRegistry.configForSessionSelection(heroId)
            val icons = skillIconsForHero(heroId)

            val attackSkill = SkillConfig(
                name = "Basic Attack",
                manaCost = 0,
                cooldownMax = 0.0,
                damage = 5.0
            )   
            // val allSkillsWithAttack = listOf(attackSkill) + cfg.allSkills
            val allSkillsWithAttack =
                (listOf(attackSkill) + cfg.allSkills)
                    .distinctBy { it.name.lowercase() }

            allSkillsWithAttack.forEachIndexed { i, skill ->
                val slice = icons.getOrElse(i) { GameAssets.skill1Slice }
                val descText = skill.selectScreenDescription()
                val wrappedName = wrapToScreenWidth(skill.name, approxCharsPerLine = nameChars)
                val wrappedDesc = wrapToScreenWidth(descText, approxCharsPerLine = descChars)

                // Count lines to size the row correctly
                val nameLines = wrappedName.count { it == '\n' } + 1
                val descLines = wrappedDesc.count { it == '\n' } + 1

                val nameFontSize = 13.0
                val descFontSize = 10.0
                val nameLinePixels = nameFontSize + 4.0   // actual pixel height per name line
                val descLinePixels = descFontSize + 3.0   // actual pixel height per desc line
                val nameBlockH = nameLines * nameLinePixels
                val descBlockH = descLines * descLinePixels
                val contentH = nameBlockH + 10.0 + descBlockH  // 10px gap between name and desc
                val rowH = max(iconSize + 8.0, contentH + 16.0) // 8px top+bottom padding

                val nameY = (rowH - contentH) / 2.0 + nameFontSize  // baseline offset
                val descY = nameY + nameBlockH + 8.0                // desc baseline below name

                val row = Container().apply {
                    x = rowX
                    this.y = skillsTopY + i * (70.0 + 6.0)  // fixed 70px slot per row
                    solidRect(300.0, rowH, Colors.BLACK) {
                        alpha = 0.45
                        x = 0.0
                        y = 0.0
                    }
                    image(slice) {
                        smoothing = true
                        width = iconSize
                        height = iconSize
                        x = 0.0
                        y = (rowH - iconSize) / 2.0
                    }
                    text(wrappedName, textSize = nameFontSize, color = Colors.WHITE, font = GameAssets.customFont) {
                        x = textPadX
                        y = nameY
                    }
                    text(wrappedDesc, textSize = descFontSize, color = RGBA(200, 200, 200, 235), font = GameAssets.customFont) {
                        x = textPadX
                        y = descY
                    }
                }
                skillPanel.addChild(row)
            }
        }

        val btnW = 260.0
        val btnH = 80.0
        val btnGap = 24.0
        val totalBtnW = btnW * 2 + btnGap

        val backBtn = TextButton(btnW, btnH, "BACK") {
            launchImmediately { scene.sceneContainer.changeTo { MenuScene() } }
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
                    populateSkillPanel(hero.id, index)
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

        addChild(skillPanel)
    }  // <-- end of sceneMain
}  // <-- end of HeroSelectionScene
