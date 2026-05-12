package ui

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.*
import entities.Character
import managers.*
import utils.*

/**
 * Heads-Up Display.
 *
 * Layout (left side, shifted right from screen edge):
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  [❤] ████████████████████████░░░░  200/200                  │  ← HP bar
 *  │  [🧪] ████████████░░░░░░░░░░░░░░  100/100                  │  ← Mana bar
 *  │  [LVL]  ══════════════════════════  0/50                    │  ← XP row
 *  └─────────────────────────────────────────────────────────────┘
 *
 * KEY DESIGN DECISIONS
 * ─────────────────────
 * 1. Bar images are fixed at BAR_WIDTH/BAR_HEIGHT at all times.
 *    We NEVER change their .width — doing so causes scaleX to reach 0 which
 *    corrupts the image node.  Instead, we use a SolidRect mask that sits on
 *    top of a dark background: the mask covers the "empty" region from
 *    fillWidth → BAR_WIDTH using the background color, giving the illusion of
 *    a shrinking bar without ever resizing the image itself.
 *
 * 2. Icons are added AFTER the bar layers so they always render in front.
 *
 * 3. XP bar fill uses the yellow health-bar image (hpBarYellowSlice) instead
 *    of a plain solidRect, per spec.
 *
 * 4. Timer is NOT owned by HUD — it lives in GameScene (under the pause button).
 *    HUD exposes nothing about the timer.
 */
class HUD(
    private val player:   Character,
    private val progress: PlayerProgress
) : Container() {

    // ─────────────────────────────────────────────
    // LAYOUT
    // ─────────────────────────────────────────────

    // HP/Mana bars are shifted right so they don't hug the very left edge
    private val BAR_LEFT_X  = 60.0          // bar starts here (leaves room for icon to the left)
    private val BAR_WIDTH   = 280.0
    private val BAR_HEIGHT  = 36.0
    private val ICON_SIZE   = 34.0
    private val ICON_PAD    = 6.0           // gap between icon right edge and bar left edge

    private val HP_Y   = 14.0
    private val MANA_Y = HP_Y + BAR_HEIGHT + 8.0

    // XP row — keep original left anchor (no shift requested)
    private val XP_LEFT_X  = 10.0
    private val XP_Y       = MANA_Y + BAR_HEIGHT + 10.0

    private val SQ_BORDER  = 4.0
    private val SQ_INNER   = 52.0
    private val SQ_TOTAL   = SQ_INNER + SQ_BORDER * 2   // 60

    private val XP_BAR_X      = XP_LEFT_X + SQ_TOTAL + 8.0
    private val XP_BAR_WIDTH  = 280.0
    private val XP_BAR_HEIGHT = 16.0
    private val XP_BAR_Y      = XP_Y + (SQ_TOTAL - XP_BAR_HEIGHT) / 2.0

    // ─────────────────────────────────────────────
    // HELPERS — computed once
    // ─────────────────────────────────────────────
    private val ICON_X get() = BAR_LEFT_X - ICON_SIZE - ICON_PAD

    // ─────────────────────────────────────────────
    // HP BAR
    // Strategy: dark bg rect always BAR_WIDTH wide. Bar image always BAR_WIDTH
    // wide and fully opaque. An overlay "empty" rect sits on the right side,
    // sized (BAR_WIDTH - fillWidth) wide, covering the unfilled portion.
    // ─────────────────────────────────────────────
    private val hpBarBg      = solidRect(BAR_WIDTH, BAR_HEIGHT, RGBA(20,  5,  5, 210))
    private val hpBarGreen   = image(GameAssets.hpBarGreenSlice).apply  { smoothing = false; width = BAR_WIDTH; height = BAR_HEIGHT }
    private val hpBarYellow  = image(GameAssets.hpBarYellowSlice).apply { smoothing = false; width = BAR_WIDTH; height = BAR_HEIGHT }
    private val hpBarRed     = image(GameAssets.hpBarRedSlice).apply    { smoothing = false; width = BAR_WIDTH; height = BAR_HEIGHT }
    // Overlay that masks the unfilled portion — same color as the background
    private val hpEmptyMask  = solidRect(0.0, BAR_HEIGHT, RGBA(20, 5, 5, 230))

    private val hpCounterText = text("", textSize = 16.0, color = Colors.WHITE, font = GameAssets.customFont)

    // Icon rendered LAST (above bars)
    private val hpIcon = image(GameAssets.healthIconSlice).apply {
        smoothing = false
        val s = ICON_SIZE / maxOf(bitmap.width.toDouble(), bitmap.height.toDouble())
        scaleX = s; scaleY = s
    }

    // ─────────────────────────────────────────────
    // MANA BAR  (same masking strategy)
    // ─────────────────────────────────────────────
    private val manaBarBg     = solidRect(BAR_WIDTH, BAR_HEIGHT, RGBA(5, 5, 20, 210))
    private val manaBarImage  = image(GameAssets.manaBarSlice).apply { smoothing = false; width = BAR_WIDTH; height = BAR_HEIGHT }
    private val manaEmptyMask = solidRect(0.0, BAR_HEIGHT, RGBA(5, 5, 20, 230))

    private val manaCounterText = text("", textSize = 16.0, color = Colors.WHITE, font = GameAssets.customFont)

    private val manaIcon = image(GameAssets.manaIconSlice).apply {
        smoothing = false
        val s = ICON_SIZE / maxOf(bitmap.width.toDouble(), bitmap.height.toDouble())
        scaleX = s; scaleY = s
    }

    // ─────────────────────────────────────────────
    // XP SQUARE
    // ─────────────────────────────────────────────
    private val xpSquareBg   = solidRect(SQ_TOTAL, SQ_TOTAL, RGBA(10, 10, 10, 200))
    private val xpYellowFill = solidRect(SQ_TOTAL, 0.0,      RGBA(220, 170, 0, 255))
    private val xpInnerBlack = solidRect(SQ_INNER, SQ_INNER, Colors.BLACK)
    private val xpLevelLabel = text("Level", textSize = 11.0, color = RGBA(190, 190, 190, 255), font = GameAssets.customFont)
    private val xpLevelText  = text("1", textSize = 20.0, color = RGBA(215, 215, 215, 255), font = GameAssets.customFont)

    // ─────────────────────────────────────────────
    // XP BAR — fill uses the yellow health bar image (per spec)
    // Same masking strategy: image is always full width; mask covers empty part
    // ─────────────────────────────────────────────
    private val xpBarBg       = solidRect(XP_BAR_WIDTH, XP_BAR_HEIGHT, RGBA(20, 20, 20, 220))
    private val xpBarFillImg  = image(GameAssets.hpBarYellowSlice).apply { smoothing = false; width = XP_BAR_WIDTH; height = XP_BAR_HEIGHT }
    private val xpBarEmptyMask = solidRect(0.0, XP_BAR_HEIGHT, RGBA(20, 20, 20, 230))
    private val xpBarText     = text("", textSize = 11.0, color = Colors.WHITE, font = GameAssets.customFont)

    // ─────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────
    init {
        // ── HP row ──────────────────────────────────────────────
        hpBarBg.xy(BAR_LEFT_X, HP_Y)
        hpBarGreen.xy(BAR_LEFT_X, HP_Y)
        hpBarYellow.xy(BAR_LEFT_X, HP_Y)
        hpBarRed.xy(BAR_LEFT_X, HP_Y)
        hpEmptyMask.xy(BAR_LEFT_X, HP_Y)           // x shifted right on update
        hpCounterText.xy(BAR_LEFT_X + BAR_WIDTH + 8.0, HP_Y + (BAR_HEIGHT - 16.0) / 2.0)
        hpIcon.xy(ICON_X, HP_Y + (BAR_HEIGHT - ICON_SIZE) / 2.0)

        // ── Mana row ────────────────────────────────────────────
        manaBarBg.xy(BAR_LEFT_X, MANA_Y)
        manaBarImage.xy(BAR_LEFT_X, MANA_Y)
        manaEmptyMask.xy(BAR_LEFT_X, MANA_Y)
        manaCounterText.xy(BAR_LEFT_X + BAR_WIDTH + 8.0, MANA_Y + (BAR_HEIGHT - 16.0) / 2.0)
        manaIcon.xy(ICON_X, MANA_Y + (BAR_HEIGHT - ICON_SIZE) / 2.0)

        // ── XP square ───────────────────────────────────────────
        xpSquareBg.xy(XP_LEFT_X, XP_Y)
        xpYellowFill.xy(XP_LEFT_X, XP_Y + SQ_TOTAL)   // height=0, rises upward
        xpInnerBlack.xy(XP_LEFT_X + SQ_BORDER, XP_Y + SQ_BORDER)

        // ── XP bar ──────────────────────────────────────────────
        xpBarBg.xy(XP_BAR_X, XP_BAR_Y)
        xpBarFillImg.xy(XP_BAR_X, XP_BAR_Y)
        xpBarEmptyMask.xy(XP_BAR_X, XP_BAR_Y)
        xpBarText.xy(XP_BAR_X + XP_BAR_WIDTH + 6.0, XP_BAR_Y + 1.0)

        // ── Painter's order (back → front) ──────────────────────
        // HP
        addChild(hpBarBg)
        addChild(hpBarGreen)
        addChild(hpBarYellow)
        addChild(hpBarRed)
        addChild(hpEmptyMask)      // mask over bar images
        addChild(hpCounterText)
        addChild(hpIcon)           // icon LAST → always in front

        // Mana
        addChild(manaBarBg)
        addChild(manaBarImage)
        addChild(manaEmptyMask)
        addChild(manaCounterText)
        addChild(manaIcon)         // icon LAST → always in front

        // XP square (back-to-front)
        addChild(xpSquareBg)
        addChild(xpYellowFill)
        addChild(xpInnerBlack)
        addChild(xpLevelLabel)
        addChild(xpLevelText)

        // XP bar
        addChild(xpBarBg)
        addChild(xpBarFillImg)
        addChild(xpBarEmptyMask)
        addChild(xpBarText)

        update()
    }

    // ─────────────────────────────────────────────
    // PER-FRAME UPDATE
    // ─────────────────────────────────────────────
    fun update() {
        updateHp()
        updateMana()
        updateXp()
    }

    /**
     * Bar fill technique:
     *  - All three color images remain at full BAR_WIDTH — never resized.
     *  - Only the correct color image is visible.
     *  - An empty-region mask rect is placed at (BAR_LEFT_X + fillWidth) with
     *    width = (BAR_WIDTH - fillWidth), covering the unfilled right portion.
     *  - fillWidth is always at least 1px so the image is never collapsed to 0.
     */
    private fun updateHp() {
        val ratio     = (player.health / player.maxHealth).coerceIn(0.0, 1.0)
        // Keep at least 1 px so image width never reaches 0
        val fillWidth = (BAR_WIDTH * ratio).coerceAtLeast(1.0)
        val emptyWidth = (BAR_WIDTH - fillWidth).coerceAtLeast(0.0)

        hpBarGreen.visible  = ratio > 0.5
        hpBarYellow.visible = ratio in 0.25..0.5
        hpBarRed.visible    = ratio < 0.25

        // Position the empty mask on the right side of the filled area
        hpEmptyMask.width = emptyWidth
        hpEmptyMask.x     = BAR_LEFT_X + fillWidth

        hpCounterText.text = "${player.health.toInt()}/${player.maxHealth.toInt()}"
    }

    private fun updateMana() {
        val ratio      = (player.mana / player.maxMana).coerceIn(0.0, 1.0)
        val fillWidth  = (BAR_WIDTH * ratio).coerceAtLeast(1.0)
        val emptyWidth = (BAR_WIDTH - fillWidth).coerceAtLeast(0.0)

        manaEmptyMask.width = emptyWidth
        manaEmptyMask.x     = BAR_LEFT_X + fillWidth

        manaCounterText.text = "${player.mana.toInt()}/${player.maxMana.toInt()}"
    }

    private fun updateXp() {
        val xpRatio = progress.xpProgress()   // 0.0–1.0

        // XP square — yellow fill rises from the bottom
        val fillH = SQ_TOTAL * xpRatio
        xpYellowFill.height = fillH
        xpYellowFill.y      = XP_Y + SQ_TOTAL - fillH

        // "Level" label + number (or MAX) stacked and centred in the inner square
        xpLevelText.text = if (progress.isMaxLevel()) "MAX" else progress.level.toString()
        val lineGap = 2.0
        val lh = xpLevelLabel.textBounds.height
        val nh = xpLevelText.textBounds.height
        val stackH = lh + lineGap + nh
        val baseY = XP_Y + SQ_BORDER + (SQ_INNER - stackH) / 2.0
        val innerLeft = XP_LEFT_X + SQ_BORDER
        xpLevelLabel.xy(
            innerLeft + (SQ_INNER - xpLevelLabel.textBounds.width) / 2.0,
            baseY
        )
        xpLevelText.xy(
            innerLeft + (SQ_INNER - xpLevelText.textBounds.width) / 2.0,
            baseY + lh + lineGap
        )

        // XP bar — mask the unfilled right portion
        val xpFillWidth  = (XP_BAR_WIDTH * xpRatio).coerceAtLeast(1.0)
        val xpEmptyWidth = (XP_BAR_WIDTH - xpFillWidth).coerceAtLeast(0.0)
        xpBarEmptyMask.width = xpEmptyWidth
        xpBarEmptyMask.x     = XP_BAR_X + xpFillWidth

        xpBarText.text = when {
            progress.isMaxLevel() -> "MAX"
            else -> "${progress.currentXp.toInt()} / ${progress.xpForNextLevel().toInt()}"
        }
    }
}