import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.*

/**
 * Heads-Up Display — image-based bars and icon labels.
 *
 * Shows:
 *  • HP bar      — image-based (green/yellow/red), top-left with heart icon
 *  • Mana bar    — image-based (blue only), below HP with potion icon
 *  • XP display  — level square (black + rising yellow) + horizontal XP bar + text
 *
 * BAR FILL TECHNIQUE
 * ──────────────────
 * Each bar image's .width is set directly every frame to match the current ratio.
 * This stretches/squishes the image horizontally, which works correctly for bar
 * textures that tile or scale. No clipContainer is used — it is not needed and
 * its setSize/scaledWidth API caused compile errors in this KorGE version.
 *
 * HEALTH BAR COLOR TIERS
 * ──────────────────────
 *   > 50%  → green image
 *   25–50% → yellow image
 *   ≤ 25%  → red image
 * Only the active image is visible; the others are hidden.
 *
 * XP SQUARE VISUAL
 * ────────────────
 *   1. xpYellowFill — solidRect that rises from bottom as XP increases.
 *   2. xpInnerBlack — black rect on top, creating a border effect.
 *   3. xpLevelText  — level number centred on the black square.
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

    private val SQ_BORDER = 4.0
    private val SQ_INNER  = 52.0
    private val SQ_TOTAL  = SQ_INNER + SQ_BORDER * 2   // 60

    private val XP_BAR_WIDTH  = BAR_WIDTH
    private val XP_BAR_HEIGHT = 16.0
    private val XP_BAR_X      = LEFT_X + SQ_TOTAL + 8.0
    private val XP_BAR_Y      = XP_Y   + (SQ_TOTAL - XP_BAR_HEIGHT) / 2.0

    // -------------------------------------------------------
    // HP BAR
    // -------------------------------------------------------
    private val hpIcon = image(GameAssets.healthIconSlice).apply {
        smoothing = false
        val s = ICON_SIZE / maxOf(bitmap.width.toDouble(), bitmap.height.toDouble())
        scaleX = s; scaleY = s
    }

    // Dark background behind the bar so the empty area is visible
    private val hpBarBg = solidRect(BAR_WIDTH, BAR_HEIGHT, RGBA(30, 10, 10, 200))

    // Three color variants — only one shown at a time; .width is updated each frame
    private val hpBarGreen  = image(GameAssets.hpBarGreenSlice).apply {
        smoothing = false; width = BAR_WIDTH; height = BAR_HEIGHT
    }
    private val hpBarYellow = image(GameAssets.hpBarYellowSlice).apply {
        smoothing = false; width = BAR_WIDTH; height = BAR_HEIGHT
    }
    private val hpBarRed    = image(GameAssets.hpBarRedSlice).apply {
        smoothing = false; width = BAR_WIDTH; height = BAR_HEIGHT
    }

    private val hpCounterText = text("200/200", textSize = 18.0, color = Colors.WHITE, font = GameAssets.customFont)

    // -------------------------------------------------------
    // MANA BAR
    // -------------------------------------------------------
    private val manaIcon = image(GameAssets.manaIconSlice).apply {
        smoothing = false
        val s = ICON_SIZE / maxOf(bitmap.width.toDouble(), bitmap.height.toDouble())
        scaleX = s; scaleY = s
    }

    private val manaBarBg    = solidRect(BAR_WIDTH, BAR_HEIGHT, RGBA(10, 10, 30, 200))
    private val manaBarImage = image(GameAssets.manaBarSlice).apply {
        smoothing = false; width = BAR_WIDTH; height = BAR_HEIGHT
    }
    private val manaCounterText = text("100/100", textSize = 18.0, color = Colors.WHITE, font = GameAssets.customFont)

    // -------------------------------------------------------
    // XP SQUARE
    // -------------------------------------------------------
    private val xpYellowFill = solidRect(SQ_TOTAL, 0.0, RGBA(220, 170, 0, 255))
    private val xpInnerBlack = solidRect(SQ_INNER, SQ_INNER, Colors.BLACK)
    private val xpLevelText  = text("1", textSize = 22.0, color = RGBA(215, 215, 215, 255), font = GameAssets.customFont)

    // -------------------------------------------------------
    // XP BAR
    // -------------------------------------------------------
    private val xpBarBg   = solidRect(XP_BAR_WIDTH, XP_BAR_HEIGHT, RGBA(20, 20, 20, 220))
    private val xpBarFill = solidRect(0.0,           XP_BAR_HEIGHT, RGBA(220, 170, 0, 255))
    private val xpBarText = text("0/30", textSize = 11.0, color = Colors.WHITE, font = GameAssets.customFont)

    // -------------------------------------------------------
    // INIT
    // -------------------------------------------------------
    init {
        // HP
        hpIcon.xy(LEFT_X - ICON_SIZE - 4.0, HP_Y + (BAR_HEIGHT - ICON_SIZE) / 2.0)
        hpBarBg.xy(LEFT_X, HP_Y)
        hpBarGreen.xy(LEFT_X, HP_Y)
        hpBarYellow.xy(LEFT_X, HP_Y)
        hpBarRed.xy(LEFT_X, HP_Y)
        hpCounterText.xy(LEFT_X + BAR_WIDTH + 6.0, HP_Y + 8.0)

        // Mana
        manaIcon.xy(LEFT_X - ICON_SIZE - 4.0, MANA_Y + (BAR_HEIGHT - ICON_SIZE) / 2.0)
        manaBarBg.xy(LEFT_X, MANA_Y)
        manaBarImage.xy(LEFT_X, MANA_Y)
        manaCounterText.xy(LEFT_X + BAR_WIDTH + 6.0, MANA_Y + 8.0)

        // XP square
        xpYellowFill.xy(LEFT_X, XP_Y + SQ_TOTAL)   // height=0 initially; rises upward
        xpInnerBlack.xy(LEFT_X + SQ_BORDER, XP_Y + SQ_BORDER)

        // XP bar
        xpBarBg.xy(XP_BAR_X, XP_BAR_Y)
        xpBarFill.xy(XP_BAR_X, XP_BAR_Y)
        xpBarText.xy(XP_BAR_X + XP_BAR_WIDTH + 6.0, XP_BAR_Y + 1.0)

        // Painter's order (back → front)
        addChild(hpIcon)
        addChild(hpBarBg)
        addChild(hpBarGreen)
        addChild(hpBarYellow)
        addChild(hpBarRed)
        addChild(hpCounterText)

        addChild(manaIcon)
        addChild(manaBarBg)
        addChild(manaBarImage)
        addChild(manaCounterText)

        addChild(xpBarBg)
        addChild(xpBarFill)
        addChild(xpBarText)
        addChild(xpYellowFill)
        addChild(xpInnerBlack)
        addChild(xpLevelText)

        update()
    }

    // -------------------------------------------------------
    // PER-FRAME UPDATE
    // -------------------------------------------------------
    fun update() {
        updateHp()
        updateMana()
        updateXp()
    }

    private fun updateHp() {
        val ratio     = (player.health / player.maxHealth).coerceIn(0.0, 1.0)
        val fillWidth = (BAR_WIDTH * ratio).coerceAtLeast(0.0)

        when {
            ratio > 0.5 -> {
                hpBarGreen.visible  = true;  hpBarGreen.width  = fillWidth
                hpBarYellow.visible = false
                hpBarRed.visible    = false
            }
            ratio > 0.25 -> {
                hpBarGreen.visible  = false
                hpBarYellow.visible = true;  hpBarYellow.width = fillWidth
                hpBarRed.visible    = false
            }
            else -> {
                hpBarGreen.visible  = false
                hpBarYellow.visible = false
                hpBarRed.visible    = true;  hpBarRed.width    = fillWidth
            }
        }

        hpCounterText.text = "${player.health.toInt()}/${player.maxHealth.toInt()}"
    }

    private fun updateMana() {
        val ratio     = (player.mana / player.maxMana).coerceIn(0.0, 1.0)
        val fillWidth = (BAR_WIDTH * ratio).coerceAtLeast(0.0)

        manaBarImage.width = fillWidth

        manaCounterText.text = "${player.mana.toInt()}/${player.maxMana.toInt()}"
    }

    private fun updateXp() {
        val xpRatio = progress.xpProgress()   // 0.0 – 1.0

        val fillH = SQ_TOTAL * xpRatio
        xpYellowFill.height = fillH
        xpYellowFill.y = XP_Y + SQ_TOTAL - fillH

        xpLevelText.text = if (progress.isMaxLevel()) "MAX" else progress.level.toString()
        xpLevelText.xy(
            LEFT_X + SQ_BORDER + (SQ_INNER - xpLevelText.textBounds.width)  / 2.0,
            XP_Y   + SQ_BORDER + (SQ_INNER - xpLevelText.textBounds.height) / 2.0
        )

        xpBarFill.width = XP_BAR_WIDTH * xpRatio
        xpBarText.text  = when {
            progress.isMaxLevel() -> "MAX"
            else -> "${progress.currentXp.toInt()} / ${progress.xpForNextLevel().toInt()}"
        }
    }
}