package ui

import korlibs.image.bitmap.BmpSlice
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.view.*
import korlibs.math.geom.Point
import kotlin.math.ceil
import utils.*
import managers.GameAssets

/**
 * A skill-slot button that shows:
 *  • the skill icon (always fully visible)
 *  • a single dark overlay when unavailable (cooldown OR low mana OR locked)
 *  • lock text at center ("Unlocked in" / "Lvl4", …) when player level is below the gate
 *  • "UNLOCK" at center when level gate is met but a skill point has not been spent yet
 *  • a centered cooldown timer in light-red (whole seconds, ceil) when on cooldown
 *  • damage or special heal hint at top-left — red for damage, green for heal values
 *  • skill level (upgrade rank) at top-right — blue badge
 *  • mana-cost at bottom-right — cyan, larger, dark badge
 *  • larger "+" above the icon for spend-point unlock or upgrade (sits higher so it stays in the UI band)
 *
 * IMPORTANT — touch/click visual feedback (isPressed) is preserved unchanged.
 */
class SkillButton(
    private val btnWidth:  Double,
    private val btnHeight: Double,
    slice: BmpSlice,
    upgradeSlice: BmpSlice, // ✅ ADD THIS
    val skillConfig: SkillConfig,
    /**
     * When true, the top-left numeric badge uses heal green ([healCornerRgb]) — for skills where
     * [SkillConfig.damage] is actually a heal amount (e.g. ramen / bento healing).
     */
    private val cornerNumberIsHealAmount: Boolean = false,
    /**
     * When [SkillConfig.damage] is 0 but this is positive, show this value in the top-left in green
     * (e.g. Wanderer Magician skill 2 aura total heal).
     */
    private val cornerHealTotalWhenDamageZero: Double? = null,
) : Container() {

    // -------------------------------------------------------
    // ICON (base layer)
    // -------------------------------------------------------
    private val icon = image(slice).apply {
        smoothing = false
        width  = btnWidth
        height = btnHeight
    }

    /** Update the icon bitmap (e.g., for icon swaps like healing skill ramen→bento at level 6). */
    fun updateIcon(newSlice: BmpSlice) {
        icon.bitmap = newSlice
    }

    // -------------------------------------------------------
    // DARK OVERLAY — single layer, never stacked
    // Covers cooldown, mana-shortage, and locked states.
    // -------------------------------------------------------
    private val darkOverlay = solidRect(btnWidth, btnHeight, RGBA(0, 0, 0, 160)).apply {
        visible = false
    }

    // -------------------------------------------------------
    // CENTER TEXT — cooldown timer OR lock requirement
    // Both share the same position; only one is shown at a time.
    // -------------------------------------------------------

    /** Shown when skill is on cooldown. Light-red, whole seconds. */
    private val cooldownText = text("", textSize = 22.0, color = RGBA(255, 120, 120, 255), font = GameAssets.customFont).apply {
        visible = false
    }

    /** Shown when player level is below [SkillConfig.unlockLevel] — two lines, centered. */
    private val lockLine1 = text("", textSize = 11.0, color = RGBA(220, 220, 80, 255), font = GameAssets.customFont).apply {
        visible = false
    }
    private val lockLine2 = text("", textSize = 13.0, color = RGBA(240, 230, 100, 255), font = GameAssets.customFont).apply {
        visible = false
    }

    /** Shown when level gate is met but [SkillConfig.paidUnlock] is still false. */
    private val unlockHintText = text("UNLOCK", textSize = 16.0, color = RGBA(120, 220, 255, 255), font = GameAssets.customFont).apply {
        visible = false
    }

    // -------------------------------------------------------
    // Corner badges — shared size/padding so layout stays uniform
    // and most of the icon (~60%+) stays visible.
    // -------------------------------------------------------
    private val labelTextSize = 15.0
    private val labelPad      = 4.0
    private val labelBgAlpha  = 175

    private val dmgBg   = solidRect(1.0, 1.0, RGBA(0, 0, 0, labelBgAlpha))
    private val dmgText = text("", textSize = labelTextSize, color = RGBA(255, 72, 72, 255), font = GameAssets.customFont)

    private val damageCornerRgb = RGBA(255, 72, 72, 255)
    private val healCornerRgb   = RGBA(72, 220, 120, 255)

    private val skillLvlBg   = solidRect(1.0, 1.0, RGBA(0, 0, 0, labelBgAlpha))
    private val skillLvlText = text("", textSize = labelTextSize, color = RGBA(100, 170, 255, 255), font = GameAssets.customFont)

    private val manaBg   = solidRect(1.0, 1.0, RGBA(0, 0, 0, labelBgAlpha))
    private val manaText = text("", textSize = labelTextSize, color = RGBA(0, 235, 255, 255), font = GameAssets.customFont)

    // -------------------------------------------------------
    // "+" — unlock (first point) or upgrade; larger + extra vertical gap so it stays off the playfield
    // -------------------------------------------------------
    private val UPGRADE_SIZE = 50.0

    private val upgradeBtn = image(upgradeSlice).apply {
        smoothing = false
        width  = UPGRADE_SIZE
        height = UPGRADE_SIZE

        xy((btnWidth - UPGRADE_SIZE) / 2, -UPGRADE_SIZE - 14.0)
        visible = false
    }

    /** Set this in GameScene to react to upgrade button taps. */
    var onUpgradeClick: (() -> Unit)? = null

    init {
        upgradeBtn.onClick { onUpgradeClick?.invoke() }
    }

    // -------------------------------------------------------
    // PRESS FEEDBACK (unchanged from original TouchButton logic)
    // -------------------------------------------------------
    var isPressed: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                icon.colorMul = if (value) Colors["#888888"] else Colors.WHITE
            }
        }

    // -------------------------------------------------------
    // INIT — layer order matters (painter's algorithm)
    // -------------------------------------------------------
    init {
        addChild(icon)
        addChild(darkOverlay)
        addChild(dmgBg);       addChild(dmgText)
        addChild(skillLvlBg);  addChild(skillLvlText)
        addChild(manaBg);      addChild(manaText)
        addChild(cooldownText)
        addChild(lockLine1)
        addChild(lockLine2)
        addChild(unlockHintText)
        addChild(upgradeBtn)

        this.alpha = 0.85
        updateLabels()
    }

    // color
    private val MAX_LEVEL = 10

    private fun getSkillLevelColor(level: Int): RGBA {
        return when (level) {
            1 -> Colors.WHITE
            2 -> Colors.WHITE
            3 -> RGBA(160, 32, 240, 255)
            4 -> RGBA(160, 32, 240, 255)
            5 -> RGBA(255, 0, 255, 255)
            6 -> RGBA(255, 0, 255, 255)
            7 -> RGBA(255, 0, 255, 255)
            8 -> RGBA(255, 140, 0, 255)
            9 -> RGBA(255, 140, 0, 255)
            MAX_LEVEL -> RGBA(255, 215, 0, 255)
            else -> RGBA(255, 215, 0, 255)
        }
    }

    // -------------------------------------------------------
    // LABEL UPDATES — call after damage or manaCost changes
    // -------------------------------------------------------
    fun updateLabels() {
        val pad = labelPad

        // Top-left value: damage (red), heal-from-[damage] (green), or aura total when damage is 0 (green)
        val cornerValue = when {
            skillConfig.heal > 0.0 -> skillConfig.heal
            skillConfig.damage > 0.0 -> skillConfig.damage
            cornerHealTotalWhenDamageZero != null && cornerHealTotalWhenDamageZero > 0.0 -> cornerHealTotalWhenDamageZero
            else -> 0.0
        }
        if (cornerValue > 0.0) {
            dmgText.text = cornerValue.toInt().toString()
            val useHealGreen = cornerNumberIsHealAmount || skillConfig.heal > 0.0 ||
                (skillConfig.damage <= 0.0 && cornerHealTotalWhenDamageZero != null && cornerHealTotalWhenDamageZero > 0.0)
            dmgText.color = if (useHealGreen) healCornerRgb else damageCornerRgb
            dmgText.xy(pad, pad)
            val dw = dmgText.textBounds.width  + pad * 2
            val dh = dmgText.textBounds.height + pad
            dmgBg.size(dw, dh).xy(0.0, 0.0)
            dmgBg.visible = true
            dmgText.visible = true
        } else {
            dmgBg.visible = false
            dmgText.visible = false
        }

        // Skill rank inside icon (top-right, blue) — Lv1 = no upgrades yet, then +1 per upgrade
        val level = skillConfig.upgradeCount + 1
        skillLvlText.text = "Lv$level"
        skillLvlText.color = getSkillLevelColor(level)
        val sw = skillLvlText.textBounds.width  + pad * 2
        val sh = skillLvlText.textBounds.height + pad
        skillLvlBg.size(sw, sh).xy(btnWidth - sw, 0.0)
        skillLvlText.xy(btnWidth - sw + pad, pad)

        // Mana cost (bottom-right, cyan) — only show if mana cost > 0
        if (skillConfig.manaCost > 0) {
            manaText.text = skillConfig.manaCost.toString()
            val mw = manaText.textBounds.width  + pad * 2
            val mh = manaText.textBounds.height + pad
            manaText.xy(btnWidth - mw + pad, btnHeight - mh)
            manaBg.size(mw, mh).xy(btnWidth - mw, btnHeight - mh)
        } else {
            manaBg.visible = false
            manaText.visible = false
        }
    }

    // -------------------------------------------------------
    // UPGRADE BUTTON HIT TEST
    // Returns true if a screen-space point is inside the upgrade button.
    // Call this in GameScene BEFORE checking the skill icon itself so that
    // tapping the "+" doesn't simultaneously activate the skill.
    // -------------------------------------------------------
    fun isUpgradeBtnHit(point: Point): Boolean =
        upgradeBtn.visible && upgradeBtn.hitTest(point) != null

    // -------------------------------------------------------
    // PER-FRAME UPDATE
    // Call from GameScene's addUpdater with live player stats.
    //
    // Priority (mutually exclusive center-text):
    //   1. Locked by level  → dark overlay + lock text
    //   2. On cooldown      → dark overlay + cooldown timer
    //   3. Low mana only    → dark overlay, no center text
    //   4. Fully usable     → no overlay, show upgrade btn if points > 0
    // -------------------------------------------------------
    fun update(currentMana: Double, playerLevel: Int, upgradePoints: Int) {
        val levelLocked       = skillConfig.requiresPointUnlock && !skillConfig.meetsLevelRequirement(playerLevel)
        val awaitingPaidUnlock = skillConfig.requiresPointUnlock &&
            skillConfig.meetsLevelRequirement(playerLevel) && !skillConfig.paidUnlock
        val castable          = skillConfig.isUnlockedForUse(playerLevel)

        val onCooldown = !skillConfig.isOffCooldown
        val lowMana    = currentMana < skillConfig.manaCost

        val unavailable = levelLocked || awaitingPaidUnlock || (castable && (onCooldown || lowMana))

        darkOverlay.visible = unavailable

        // --- center text: level lock → unlock hint → cooldown (mutually exclusive) ---
        when {
            levelLocked -> {
                lockLine1.text = "Unlocked in"
                lockLine2.text = "Lvl${skillConfig.unlockLevel}"
                lockLine1.visible = true
                lockLine2.visible = true
                val lineGap = 1.0
                val h1 = lockLine1.textBounds.height
                val h2 = lockLine2.textBounds.height
                val totalH = h1 + lineGap + h2
                var y0 = (btnHeight - totalH) / 2.0
                lockLine1.xy((btnWidth - lockLine1.textBounds.width) / 2.0, y0)
                y0 += h1 + lineGap
                lockLine2.xy((btnWidth - lockLine2.textBounds.width) / 2.0, y0)
                unlockHintText.visible = false
                cooldownText.visible = false
            }
            awaitingPaidUnlock -> {
                unlockHintText.visible = true
                unlockHintText.xy(
                    (btnWidth  - unlockHintText.textBounds.width)  / 2.0,
                    (btnHeight - unlockHintText.textBounds.height) / 2.0
                )
                lockLine1.visible = false
                lockLine2.visible = false
                cooldownText.visible = false
            }
            castable && onCooldown -> {
                val secs = ceil(skillConfig.cooldownRemaining).toInt()
                cooldownText.text = secs.toString()
                cooldownText.visible = true
                cooldownText.xy(
                    (btnWidth  - cooldownText.textBounds.width)  / 2.0,
                    (btnHeight - cooldownText.textBounds.height) / 2.0
                )
                lockLine1.visible = false
                lockLine2.visible = false
                unlockHintText.visible = false
            }
            else -> {
                cooldownText.visible = false
                lockLine1.visible = false
                lockLine2.visible = false
                unlockHintText.visible = false
            }
        }

        // "+" — spend point to unlock, or to upgrade when unlocked and not maxed
        upgradeBtn.visible = when {
            levelLocked -> false
            awaitingPaidUnlock -> upgradePoints > 0
            castable && skillConfig.canUpgrade -> upgradePoints > 0
            else -> false
        }
    }
}
