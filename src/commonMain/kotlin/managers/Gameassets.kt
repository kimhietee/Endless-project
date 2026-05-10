package managers

import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.image.font.Font
import korlibs.image.font.readFont
import utils.*

object GameAssets {

    private val frameCache = mutableMapOf<String, List<BmpSlice>>()
    var loaded = false
        private set

    // ── Individual named backgrounds (kept for scenes that reference them directly) ──
    lateinit var bgSlice:  BmpSlice
    lateinit var bg2Slice: BmpSlice
    lateinit var bg3Slice: BmpSlice
    lateinit var bg4Slice: BmpSlice

    /**
     * All backgrounds available for wave rotation.
     *
     * How it works:
     *  - We attempt to load bg/background.png, bg/background2.png, … bg/background10.png
     *    (and beyond, up to MAX_BG_PROBE).
     *  - Any file that exists is added to the list; missing files are silently skipped.
     *  - You can have fewer OR more than 10 — the code handles it automatically.
     *  - Minimum: if no numbered backgrounds are found at all, the list falls back to
     *    [bgSlice] so the game always has at least one background.
     *
     * To add more backgrounds, just drop bg/background11.png, bg/background12.png, etc.
     * into your resources/bg/ folder — no code changes needed.
     */
    val backgroundList = mutableListOf<BmpSlice>()

    /** Returns the background for a given wave number (1-based), cycling if needed. */
    fun backgroundForWave(waveNumber: Int): BmpSlice {
        if (backgroundList.isEmpty()) return bgSlice
        val index = (waveNumber - 1).coerceAtLeast(0) % backgroundList.size
        return backgroundList[index]
    }

    // ── HUD bars ────────────────────────────────────────────────────────────
    lateinit var hpBarGreenSlice:  BmpSlice
    lateinit var hpBarYellowSlice: BmpSlice
    lateinit var hpBarRedSlice:    BmpSlice
    lateinit var manaBarSlice:     BmpSlice

    // ── HUD icons ───────────────────────────────────────────────────────────
    lateinit var healthIconSlice: BmpSlice
    lateinit var manaIconSlice:   BmpSlice

    // ── Font ─────────────────────────────────────────────────────────────────
    lateinit var customFont: Font

    // ── Character animation frames ───────────────────────────────────────────
    lateinit var idleFrames:   List<BmpSlice>
    lateinit var runFrames:    List<BmpSlice>
    lateinit var jumpFrames:   List<BmpSlice>
    lateinit var attackFrames: List<BmpSlice>
    lateinit var skillFrames:  List<BmpSlice>

    // ── UI button slices ─────────────────────────────────────────────────────
    lateinit var leftSlice:          BmpSlice
    lateinit var rightSlice:         BmpSlice
    lateinit var jumpSlice:          BmpSlice
    lateinit var attackSlice:        BmpSlice
    lateinit var skill1Slice:        BmpSlice
    lateinit var skill2Slice:        BmpSlice
    lateinit var skill3Slice:        BmpSlice
    lateinit var skill4Slice:        BmpSlice
    lateinit var healingRamenSlice:  BmpSlice
    lateinit var healingBentoSlice:  BmpSlice
    lateinit var maxHealthSlice:     BmpSlice
    lateinit var pauseSlice:         BmpSlice
    lateinit var playSlice:          BmpSlice
    lateinit var buttonBgSlice:      BmpSlice
    lateinit var upgradeSlice:       BmpSlice

    // ── Pause menu image buttons ─────────────────────────────────────────────
    // Replace the placeholder paths below with your actual asset paths.
    // e.g. "ui/buttons/btn_resume.png", "ui/buttons/btn_restart.png", "ui/buttons/btn_quit.png"
    lateinit var pauseResumeSlice:  BmpSlice
    lateinit var pauseRestartSlice: BmpSlice
    lateinit var pauseQuitSlice:    BmpSlice

    suspend fun load() {
        if (loaded) return

        // ── Named background shortcuts ──────────────────────────────────────
        bgSlice  = resourcesVfs["bg/background.png"].readBitmapSlice()
        bg2Slice = resourcesVfs["bg/background2.png"].readBitmapSlice()
        bg3Slice = resourcesVfs["bg/background3.png"].readBitmapSlice()
        bg4Slice = resourcesVfs["bg/background4.png"].readBitmapSlice()

        // ── Dynamic background list for wave rotation ───────────────────────
        // Probes bg/background.png, bg/background2.png … bg/backgroundN.png
        // until MAX_BG_PROBE consecutive misses are encountered.
        // The first file uses no number suffix; subsequent files use 2, 3, 4 …
        // Adjust MAX_BG_PROBE if you ever plan to have more than 20 backgrounds.
        val MAX_BG_PROBE = 20
        backgroundList.clear()

        // bg/background.png  (index 1, no number suffix)
        try {
            backgroundList.add(resourcesVfs["bg/background.png"].readBitmapSlice())
        } catch (_: Exception) { }

        // bg/background2.png … bg/background{MAX_BG_PROBE}.png
        var consecutiveMisses = 0
        for (i in 2..MAX_BG_PROBE) {
            try {
                val slice = resourcesVfs["bg/background$i.png"].readBitmapSlice()
                backgroundList.add(slice)
                consecutiveMisses = 0
            } catch (_: Exception) {
                consecutiveMisses++
                // Stop probing after 3 consecutive missing files so startup
                // doesn't stall trying every number up to MAX_BG_PROBE.
                if (consecutiveMisses >= 3) break
            }
        }

        // Fallback: make sure the list is never empty
        if (backgroundList.isEmpty()) backgroundList.add(bgSlice)

        println("[GameAssets] Loaded ${backgroundList.size} background(s) for wave rotation.")

        // ── HUD bars ────────────────────────────────────────────────────────
        hpBarGreenSlice  = resourcesVfs["ui/bar/green_health_bar.jpg"].readBitmapSlice()
        hpBarYellowSlice = resourcesVfs["ui/bar/yellow_health_bar.jpg"].readBitmapSlice()
        hpBarRedSlice    = resourcesVfs["ui/bar/red_health_bar.jpg"].readBitmapSlice()
        manaBarSlice     = resourcesVfs["ui/bar/mana_bar.jpg"].readBitmapSlice()

        // ── HUD icons ───────────────────────────────────────────────────────
        healthIconSlice = resourcesVfs["ui/icons/heart.PNG"].readBitmapSlice()
        manaIconSlice   = resourcesVfs["ui/icons/potion.png"].readBitmapSlice()

        // ── Font ─────────────────────────────────────────────────────────────
        customFont = resourcesVfs["ui/font/slkscr.ttf"].readFont()

        // ── Character frames ─────────────────────────────────────────────────
        idleFrames   = loadFrames(FrameConfig("fireWizard/idle_pngs",     "image_0-",  startIndex = 0, count = 7))
        runFrames    = loadFrames(FrameConfig("fireWizard/run_pngs",      "Run_",      startIndex = 0, count = 8))
        jumpFrames   = loadFrames(FrameConfig("fireWizard/jump_pngs",     "Jump_",     startIndex = 0, count = 6))
        attackFrames = loadFrames(FrameConfig("fireWizard/slash_pngs",    "Attack_1_", startIndex = 0, count = 10))
        skillFrames  = loadFrames(FrameConfig("fireWizard/fireball_pngs", "image_0-",  startIndex = 0, count = 8))

        // Preload shared enemy assets
        loadFrames(FrameConfig("fireWizard/idle_pngs",     "image_0-",  startIndex = 0, count = 7))
        loadFrames(FrameConfig("fireWizard/run_pngs",      "Run_",      startIndex = 0, count = 8))
        loadFrames(FrameConfig("fireWizard/slash_pngs",    "Attack_1_", startIndex = 0, count = 10))
        loadFrames(FrameConfig("fireWizard/fireball_pngs", "image_0-",  startIndex = 0, count = 8))
        loadFrames(FrameConfig("fireWizard/jump_pngs",     "Jump_",     startIndex = 0, count = 6))

        // ── UI buttons ───────────────────────────────────────────────────────
        leftSlice         = resourcesVfs["ui/buttons/btn_left.png"].readBitmapSlice()
        rightSlice        = resourcesVfs["ui/buttons/btn_right.png"].readBitmapSlice()
        jumpSlice         = resourcesVfs["ui/buttons/btn_jump.png"].readBitmapSlice()
        attackSlice       = resourcesVfs["ui/buttons/btn_attack.png"].readBitmapSlice()
        skill1Slice       = resourcesVfs["skill_icons/fire_wizard/1.png"].readBitmapSlice()
        skill2Slice       = resourcesVfs["skill_icons/fire_wizard/2.png"].readBitmapSlice()
        skill3Slice       = resourcesVfs["skill_icons/fire_wizard/3.png"].readBitmapSlice()
        skill4Slice       = resourcesVfs["skill_icons/fire_wizard/4.png"].readBitmapSlice()
        healingRamenSlice = resourcesVfs["ui/icons/ramen.png"].readBitmapSlice()
        healingBentoSlice = resourcesVfs["ui/icons/bento.png"].readBitmapSlice()
        maxHealthSlice    = resourcesVfs["ui/icons/heart.PNG"].readBitmapSlice()
        pauseSlice        = resourcesVfs["ui/buttons/btn_menu.png"].readBitmapSlice()
        playSlice         = resourcesVfs["ui/buttons/btn_play.png"].readBitmapSlice()
        buttonBgSlice     = resourcesVfs["ui/buttons/button_bg.png"].readBitmapSlice()
        upgradeSlice      = resourcesVfs["ui/buttons/btn_upgrade.png"].readBitmapSlice()

        // ── Pause menu image buttons ─────────────────────────────────────────
        // TODO: replace placeholder paths with your actual pause-menu button images.
        pauseResumeSlice  = resourcesVfs["ui/buttons/btn_resume.png"].readBitmapSlice()
        pauseRestartSlice = resourcesVfs["ui/buttons/btn_restart.png"].readBitmapSlice()
        pauseQuitSlice    = resourcesVfs["ui/buttons/btn_quit.png"].readBitmapSlice()

        loaded = true
    }

    suspend fun loadFrames(cfg: FrameConfig): List<BmpSlice> {
        val key = buildKey(cfg)
        return frameCache.getOrPut(key) {
            if (cfg.sheet != null) {
                val sheetPath = "${cfg.folder}/${cfg.sheet.fileName}.${cfg.extension}"
                val sheet = resourcesVfs[sheetPath].readBitmapSlice()
                val frames = mutableListOf<BmpSlice>()
                val frameWidth  = sheet.width  / cfg.sheet.columns
                val frameHeight = sheet.height / cfg.sheet.rows
                for (row in 0 until cfg.sheet.rows) {
                    for (col in 0 until cfg.sheet.columns) {
                        frames += sheet.sliceWithSize(col * frameWidth, row * frameHeight, frameWidth, frameHeight)
                    }
                }
                frames.take(cfg.count)
            } else {
                (cfg.startIndex until cfg.startIndex + cfg.count).map { i ->
                    val index = if (cfg.zeroPad > 0) i.toString().padStart(cfg.zeroPad, '0') else i.toString()
                    resourcesVfs["${cfg.folder}/${cfg.prefix}$index.${cfg.extension}"].readBitmapSlice()
                }
            }
        }
    }

    private fun buildKey(cfg: FrameConfig) =
        "${cfg.folder}|${cfg.prefix}|${cfg.startIndex}|${cfg.count}|${cfg.zeroPad}|${cfg.extension}|${cfg.sheet?.fileName ?: ""}|${cfg.sheet?.columns ?: 0}|${cfg.sheet?.rows ?: 0}"
}