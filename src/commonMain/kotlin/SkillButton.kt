import korlibs.image.bitmap.BmpSlice
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.*
import kotlin.math.ceil

/**
 * A skill-slot button that shows:
 *  • the skill icon (always fully visible)
 *  • a single dark overlay when the skill is unavailable (cooldown OR low mana)
 *  • a centered cooldown timer in light-red (whole seconds, ceil)
 *  • damage text at top-left with dark background
 *  • mana-cost text at bottom-right with dark background (cyan)
 *
 * IMPORTANT – touch/click visual feedback (isPressed) is preserved
 * from the original TouchButton and is NOT modified here.
 */
class SkillButton(
    private val btnWidth: Double,
    private val btnHeight: Double,
    slice: BmpSlice,
    val skillConfig: SkillConfig
) : Container() {

    // -------------------------------------------------------
    // ICON (base layer)
    // -------------------------------------------------------
    private val icon = image(slice).apply {
        smoothing = false
        width  = btnWidth
        height = btnHeight
    }

    // -------------------------------------------------------
    // DARK OVERLAY — single layer, never stacked
    // Uses colorMul on a solid black rect so the icon stays
    // fully visible underneath (disabled look, not see-through).
    // -------------------------------------------------------
    private val darkOverlay = solidRect(btnWidth, btnHeight, RGBA(0, 0, 0, 160)).apply {
        visible = false
    }

    // -------------------------------------------------------
    // COOLDOWN TIMER — light-red, centered, whole seconds
    // -------------------------------------------------------
    private val cooldownText = text("", textSize = 22.0, color = RGBA(255, 120, 120, 255)).apply {
        visible = false
    }

    // -------------------------------------------------------
    // DAMAGE TEXT — top-left, small, dark background
    // -------------------------------------------------------
    private val dmgBg = solidRect(1.0, 1.0, RGBA(0, 0, 0, 180))
    private val dmgText = text("", textSize = 11.0, color = Colors.WHITE)

    // -------------------------------------------------------
    // MANA COST TEXT — bottom-right, small, cyan, dark bg
    // -------------------------------------------------------
    private val manaBg = solidRect(1.0, 1.0, RGBA(0, 0, 0, 180))
    private val manaText = text("", textSize = 11.0, color = RGBA(0, 220, 255, 255))

    // -------------------------------------------------------
    // PRESS FEEDBACK (unchanged from TouchButton logic)
    // -------------------------------------------------------
    var isPressed: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                icon.colorMul = if (value) Colors["#888888"] else Colors.WHITE
            }
        }

    // -------------------------------------------------------
    // INIT
    // -------------------------------------------------------
    init {
        addChild(icon)
        addChild(darkOverlay)
        addChild(cooldownText)
        addChild(dmgBg)
        addChild(dmgText)
        addChild(manaBg)
        addChild(manaText)

        this.alpha = 0.85

        // static labels (update once; damage/mana values may change later via updateLabels)
        updateLabels()
    }

    // -------------------------------------------------------
    // LABEL UPDATES  — call when damage / manaCost changes
    // -------------------------------------------------------
    fun updateLabels() {
        val dmg = skillConfig.damage.toInt().toString()
        dmgText.text = dmg

        // Position damage text at top-left with padding
        val pad = 3.0
        dmgText.xy(pad, pad)
        // Size the bg to fit
        val dmgTextW = dmgText.textBounds.width + pad * 2
        val dmgTextH = dmgText.textBounds.height + pad
        dmgBg.size(dmgTextW, dmgTextH)
        dmgBg.xy(0.0, 0.0)

        val mp = skillConfig.manaCost.toString()
        manaText.text = mp

        // Position mana text at bottom-right
        val manaTextW = manaText.textBounds.width + pad * 2
        val manaTextH = manaText.textBounds.height + pad
        manaText.xy(btnWidth - manaTextW + pad, btnHeight - manaTextH)
        manaBg.size(manaTextW, manaTextH)
        manaBg.xy(btnWidth - manaTextW, btnHeight - manaTextH)
    }

    // -------------------------------------------------------
    // PER-FRAME UPDATE  — called from GameScene's addUpdater
    // -------------------------------------------------------
    fun update(currentMana: Double) {
        val onCooldown  = !skillConfig.isOffCooldown
        val lowMana     = currentMana < skillConfig.manaCost
        val unavailable = onCooldown || lowMana

        // --- single dark overlay (never stacked) ---
        darkOverlay.visible = unavailable

        // --- cooldown timer (centered, light red, whole seconds) ---
        if (onCooldown) {
            val secs = ceil(skillConfig.cooldownRemaining).toInt()
            cooldownText.text = secs.toString()
            cooldownText.visible = true

            // center the text on the icon
            val tw = cooldownText.textBounds.width
            val th = cooldownText.textBounds.height
            cooldownText.xy(
                (btnWidth  - tw) / 2.0,
                (btnHeight - th) / 2.0
            )
        } else {
            cooldownText.visible = false
        }
    }
}
