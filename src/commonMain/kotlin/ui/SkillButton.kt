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
 *  • lock text at center ("LV2", …) when player level is below the gate
 *  • "UNLOCK" at center when level gate is met but a skill point has not been spent yet
 *  • a centered cooldown timer in light-red (whole seconds, ceil) when on cooldown
 *  • damage text at top-left — red, larger, dark badge (uniform with other corner labels)
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

    /** Shown when player level is below [SkillConfig.unlockLevel]. */
    private val lockText = text("", textSize = 18.0, color = RGBA(220, 220, 80, 255), font = GameAssets.customFont).apply {
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
        addChild(lockText)
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

        // Damage (top-left, red) — only show if damage > 0
        if (skillConfig.damage > 0.0) {
            dmgText.text = skillConfig.damage.toInt().toString()
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
                lockText.text    = "LV${skillConfig.unlockLevel}"
                lockText.visible = true
                lockText.xy(
                    (btnWidth  - lockText.textBounds.width)  / 2.0,
                    (btnHeight - lockText.textBounds.height) / 2.0
                )
                unlockHintText.visible = false
                cooldownText.visible   = false
            }
            awaitingPaidUnlock -> {
                unlockHintText.visible = true
                unlockHintText.xy(
                    (btnWidth  - unlockHintText.textBounds.width)  / 2.0,
                    (btnHeight - unlockHintText.textBounds.height) / 2.0
                )
                lockText.visible       = false
                cooldownText.visible   = false
            }
            castable && onCooldown -> {
                val secs = ceil(skillConfig.cooldownRemaining).toInt()
                cooldownText.text    = secs.toString()
                cooldownText.visible = true
                cooldownText.xy(
                    (btnWidth  - cooldownText.textBounds.width)  / 2.0,
                    (btnHeight - cooldownText.textBounds.height) / 2.0
                )
                lockText.visible       = false
                unlockHintText.visible = false
            }
            else -> {
                cooldownText.visible   = false
                lockText.visible       = false
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
