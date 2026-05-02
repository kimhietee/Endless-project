import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.*
import korlibs.math.geom.Size

/**
 * Heads-Up Display — now with image-based bars and icon labels.
 *
 * Shows:
 *  • HP bar      — image-based (green/yellow/red), top-left with heart icon
 *  • Mana bar    — image-based (blue only), below HP with potion icon
 *  • XP display  — level square (black + rising yellow) + horizontal XP bar + text
 *
 * HEALTH BAR DYNAMICS
 * ───────────────────
 * Health bar changes color based on HP ratio:
 *   > 50%  → Green bar
 *   25-50% → Yellow bar
 *   < 25%  → Red bar
 * 
 * The bar width reduces dynamically as health decreases.
 *
 * MANA BAR DYNAMICS
 * ─────────────────
 * Mana bar uses blue image only.
 * The bar width reduces dynamically as mana decreases.
 *
 * XP SQUARE VISUAL
 * ────────────────
 * The level square uses three layers:
 *   1. yellowFill  — solidRect whose height grows from 0 → SQ_TOTAL based on XP progress.
 *                    Anchored at the BOTTOM of the square; rises upward as XP increases.
 *   2. innerBlack  — slightly smaller black rect positioned SQ_BORDER px inside, always on top.
 *                    The yellow becomes visible around the edges as it rises.
 *   3. levelText   — player level number (or "MAX") centered on innerBlack.
 *
 * This produces the reference image effect:
 *   empty XP → yellow barely visible (only a sliver at the bottom)
 *   50% XP   → yellow visible on the lower half of all four edges
 *   full XP  → yellow border fully surrounds the inner black square
 */
class HUD(
    private val player:   Character,
    private val progress: PlayerProgress
) : Container() {

    // -------------------------------------------------------
    // LAYOUT CONSTANTS
    // -------------------------------------------------------
    private val LEFT_X     = 10.0
    private val BAR_WIDTH  = 300.0
    private val BAR_HEIGHT = 40.0
    private val ICON_SIZE  = 32.0

    private val HP_Y   = 10.0
    private val MANA_Y = HP_Y   + BAR_HEIGHT + 6.0
    private val XP_Y   = MANA_Y + BAR_HEIGHT + 8.0

    // XP square dimensions
    private val SQ_BORDER = 4.0
    private val SQ_INNER  = 52.0
    private val SQ_TOTAL  = SQ_INNER + SQ_BORDER * 2   // 60

    // XP bar (to the right of the square)
    private val XP_BAR_WIDTH  = BAR_WIDTH
    private val XP_BAR_HEIGHT = 16.0
    private val XP_BAR_X      = LEFT_X + SQ_TOTAL + 8.0
    private val XP_BAR_Y      = XP_Y   + (SQ_TOTAL - XP_BAR_HEIGHT) / 2.0

    // -------------------------------------------------------
    // HP BAR VIEWS (Image-based with masking for fill)
    // -------------------------------------------------------
    private val hpIconContainer = container()
    private val hpIcon = image(GameAssets.healthIconSlice).apply {
        smoothing = false
        val s = ICON_SIZE / maxOf(bitmap.width.toDouble(), bitmap.height.toDouble())
        scaleX = s
        scaleY = s
    }
    
    // Three layered bars: green (good health), yellow (medium), red (low)
    private val hpBarGreen = image(GameAssets.hpBarGreenSlice).apply {
        smoothing = false
        width = BAR_WIDTH
        height = BAR_HEIGHT
    }
    private val hpBarYellow = image(GameAssets.hpBarYellowSlice).apply {
        smoothing = false
        width = BAR_WIDTH
        height = BAR_HEIGHT
    }
    private val hpBarRed = image(GameAssets.hpBarRedSlice).apply {
        smoothing = false
        width = BAR_WIDTH
        height = BAR_HEIGHT
    }

    // Masking containers to show fill percentage
    private val hpFillGreen = clipContainer(Size(BAR_WIDTH, BAR_HEIGHT)) { addChild(hpBarGreen) }
    private val hpFillYellow = clipContainer(Size(BAR_WIDTH, BAR_HEIGHT)) { addChild(hpBarYellow) }
    private val hpFillRed = clipContainer(Size(BAR_WIDTH, BAR_HEIGHT)) { addChild(hpBarRed) }
    
    private val hpCounterText = text("200/200", textSize = 18.0, color = Colors.WHITE, font = GameAssets.customFont)

    // -------------------------------------------------------
    // MANA BAR VIEWS (Image-based with masking for fill)
    // -------------------------------------------------------
    private val manaIconContainer = container()
    private val manaIcon = image(GameAssets.manaIconSlice).apply {
        smoothing = false
        val s = ICON_SIZE / maxOf(bitmap.width.toDouble(), bitmap.height.toDouble())
        scaleX = s
        scaleY = s
    }
    
    private val manaBarImage = image(GameAssets.manaBarSlice).apply {
        smoothing = false
        width = BAR_WIDTH
        height = BAR_HEIGHT
    }
    
    private val manaFillContainer = clipContainer(Size(BAR_WIDTH, BAR_HEIGHT)) { addChild(manaBarImage) }
    private val manaCounterText = text("100/100", textSize = 18.0, color = Colors.WHITE, font = GameAssets.customFont)

    // -------------------------------------------------------
    // XP SQUARE VIEWS
    // -------------------------------------------------------
    /** Grows from bottom; visible as a border around innerBlack. */
    private val xpYellowFill = solidRect(SQ_TOTAL, 0.0, RGBA(220, 170, 0, 255))

    /** The inner black square; always drawn on top of yellowFill. */
    private val xpInnerBlack = solidRect(SQ_INNER, SQ_INNER, Colors.BLACK)

    /** Shows the player level (or "MAX"). */
    private val xpLevelText  = text("1", textSize = 22.0, color = RGBA(215, 215, 215, 255), font = GameAssets.customFont)

    // -------------------------------------------------------
    // XP BAR VIEWS
    // -------------------------------------------------------
    private val xpBarBg   = solidRect(XP_BAR_WIDTH, XP_BAR_HEIGHT, RGBA(20, 20, 20, 220))
    private val xpBarFill = solidRect(0.0,           XP_BAR_HEIGHT, RGBA(220, 170, 0, 255))
    private val xpBarText = text("0/30", textSize = 11.0, color = Colors.WHITE, font = GameAssets.customFont)

    // -------------------------------------------------------
    // INIT — position all views
    // -------------------------------------------------------
    init {
        // HP bar with icon to the left
        hpIconContainer.xy(LEFT_X - ICON_SIZE - 4.0, HP_Y + (BAR_HEIGHT - ICON_SIZE) / 2.0)
        hpIconContainer.addChild(hpIcon)
        
        // HP bar layers (only one visible at a time based on health)
        hpFillGreen.xy(LEFT_X, HP_Y)
        hpFillYellow.xy(LEFT_X, HP_Y)
        hpFillRed.xy(LEFT_X, HP_Y)

        hpCounterText.xy(LEFT_X + BAR_WIDTH + 6.0, HP_Y + 8.0)

        // Mana bar with icon to the left
        manaIconContainer.xy(LEFT_X - ICON_SIZE - 4.0, MANA_Y + (BAR_HEIGHT - ICON_SIZE) / 2.0)
        manaIconContainer.addChild(manaIcon)
        
        manaFillContainer.xy(LEFT_X, MANA_Y)
        manaCounterText.xy(LEFT_X + BAR_WIDTH + 6.0, MANA_Y + 8.0)

        // XP square
        xpYellowFill.xy(LEFT_X, XP_Y + SQ_TOTAL)   // starts at bottom (height = 0)
        xpInnerBlack.xy(LEFT_X + SQ_BORDER, XP_Y + SQ_BORDER)

        // XP bar
        xpBarBg.xy(XP_BAR_X, XP_BAR_Y)
        xpBarFill.xy(XP_BAR_X, XP_BAR_Y)
        xpBarText.xy(XP_BAR_X + XP_BAR_WIDTH + 6.0, XP_BAR_Y + 1.0)

        // Painter's order: back → front
        addChild(hpIconContainer)
        addChild(hpFillGreen)
        addChild(hpFillYellow)
        addChild(hpFillRed)
        addChild(hpCounterText)
        addChild(manaIconContainer)
        addChild(manaFillContainer)
        addChild(manaCounterText)
        addChild(xpBarBg)
        addChild(xpBarFill)
        addChild(xpBarText)
        addChild(xpYellowFill)
        addChild(xpInnerBlack)
        addChild(xpLevelText)

        // Do an initial update so text is positioned correctly from frame 1
        update()
    }

    // -------------------------------------------------------
    // PER-FRAME UPDATE — call once per frame from GameScene
    // -------------------------------------------------------
    fun update() {
        updateHp()
        updateMana()
        updateXp()
    }

    private fun updateHp() {
        val ratio = (player.health / player.maxHealth).coerceIn(0.0, 1.0)
        val fillWidth = BAR_WIDTH * ratio
        
        // Show the appropriate bar image based on health ratio and adjust width dynamically
        when {
            ratio > 0.5 -> {
                // Green bar - high health
                hpFillGreen.visible = true
                hpFillYellow.visible = false
                hpFillRed.visible = false
                hpFillGreen.scaledWidth = fillWidth
            }
            ratio > 0.25 -> {
                // Yellow bar - medium health
                hpFillGreen.visible = false
                hpFillYellow.visible = true
                hpFillRed.visible = false
                hpFillYellow.scaledWidth = fillWidth
            }
            else -> {
                // Red bar - low health
                hpFillGreen.visible = false
                hpFillYellow.visible = false
                hpFillRed.visible = true
                hpFillRed.scaledWidth = fillWidth
            }
        }

        hpCounterText.text = "${player.health.toInt()}/${player.maxHealth.toInt()}"
    }

    private fun updateMana() {
        val ratio = (player.mana / player.maxMana).coerceIn(0.0, 1.0)
        val fillWidth = BAR_WIDTH * ratio
        
        // Blue mana bar - width reduces as mana decreases
        manaFillContainer.scaledWidth = fillWidth
        
        manaCounterText.text = "${player.mana.toInt()}/${player.maxMana.toInt()}"
    }

    private fun updateXp() {
        val xpRatio = progress.xpProgress()    // 0.0 – 1.0

        // --- Rising yellow fill (behind innerBlack) ---
        val fillH = SQ_TOTAL * xpRatio
        xpYellowFill.height = fillH
        // y anchored at the bottom of the square area; rises upward
        xpYellowFill.y = XP_Y + SQ_TOTAL - fillH

        // --- Level text (centered on innerBlack) ---
        xpLevelText.text = if (progress.isMaxLevel()) "MAX" else progress.level.toString()
        xpLevelText.xy(
            LEFT_X + SQ_BORDER + (SQ_INNER - xpLevelText.textBounds.width)  / 2.0,
            XP_Y   + SQ_BORDER + (SQ_INNER - xpLevelText.textBounds.height) / 2.0
        )

        // --- XP bar ---
        xpBarFill.width = XP_BAR_WIDTH * xpRatio
        xpBarText.text  = when {
            progress.isMaxLevel() -> "MAX"
            else -> "${progress.currentXp.toInt()} / ${progress.xpForNextLevel().toInt()}"
        }
    }
}