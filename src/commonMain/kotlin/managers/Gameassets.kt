package managers

import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.image.font.Font
import korlibs.image.font.readFont
import utils.*

object GameAssets {

    private val frameCache = mutableMapOf<String, List<BmpSlice>>()
    var loaded = false
        private set

    // Fallback pixel if something fails to load
    private val fallbackSlice: BmpSlice by lazy { korlibs.image.bitmap.Bitmap32(1, 1, Colors.MAGENTA).slice() }

    // ── Individual named backgrounds ────────────────────────────────────────
    lateinit var bgSlice:  BmpSlice
    lateinit var bg2Slice: BmpSlice
    lateinit var bg3Slice: BmpSlice
    lateinit var bg4Slice: BmpSlice

    val backgroundList = mutableListOf<BmpSlice>()

    fun backgroundForWave(waveNumber: Int): BmpSlice {
        if (backgroundList.isEmpty()) return if (::bgSlice.isInitialized) bgSlice else fallbackSlice
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
    lateinit var pauseResumeSlice:  BmpSlice
    lateinit var pauseRestartSlice: BmpSlice
    lateinit var pauseQuitSlice:    BmpSlice

    suspend fun load() {
        if (loaded) return

        suspend fun safeLoad(path: String, fallback: BmpSlice = fallbackSlice): BmpSlice {
            return try {
                resourcesVfs[path].readBitmapSlice()
            } catch (e: Exception) {
                println("[GameAssets] FAILED to load: $path. Error: ${e.message}")
                fallback
            }
        }

        // ── Named background shortcuts ──────────────────────────────────────
        bgSlice  = safeLoad("bg/background.png")
        bg2Slice = safeLoad("bg/background2.png")
        bg3Slice = safeLoad("bg/background3.png")
        bg4Slice = safeLoad("bg/background4.png")

        // ── Dynamic background list ─────────────────────────────────────────
        backgroundList.clear()
        backgroundList.add(bgSlice)

        val MAX_BG_PROBE = 20
        var consecutiveMisses = 0
        for (i in 2..MAX_BG_PROBE) {
            try {
                val slice = resourcesVfs["bg/background$i.png"].readBitmapSlice()
                backgroundList.add(slice)
                consecutiveMisses = 0
            } catch (_: Exception) {
                consecutiveMisses++
                if (consecutiveMisses >= 3) break
            }
        }

        // ── HUD bars ────────────────────────────────────────────────────────
        hpBarGreenSlice  = safeLoad("ui/bar/green_health_bar.jpg")
        hpBarYellowSlice = safeLoad("ui/bar/yellow_health_bar.jpg")
        hpBarRedSlice    = safeLoad("ui/bar/red_health_bar.jpg")
        manaBarSlice     = safeLoad("ui/bar/mana_bar.jpg")

        // ── HUD icons ───────────────────────────────────────────────────────
        // Trying 'heart (1).png' as primary if heart.PNG fails or is too large
        healthIconSlice = try {
            resourcesVfs["ui/icons/heart (1).png"].readBitmapSlice()
        } catch (_: Exception) {
            safeLoad("ui/icons/heart.PNG")
        }
        manaIconSlice   = safeLoad("ui/icons/potion.png")

        // ── Font ─────────────────────────────────────────────────────────────
        customFont = try {
            resourcesVfs["ui/font/slkscr.ttf"].readFont()
        } catch (e: Exception) {
            println("[GameAssets] Font load failed: ${e.message}")
            korlibs.image.font.DefaultTtfFont
        }

        // ── Character frames ─────────────────────────────────────────────────
        idleFrames   = loadFrames(FrameConfig("fireWizard/idle_pngs",     "image_0-",  startIndex = 0, count = 7))
        runFrames    = loadFrames(FrameConfig("fireWizard/run_pngs",      "Run_",      startIndex = 0, count = 8))
        jumpFrames   = loadFrames(FrameConfig("fireWizard/jump_pngs",     "Jump_",     startIndex = 0, count = 6))
        attackFrames = loadFrames(FrameConfig("fireWizard/slash_pngs",    "Attack_1_", startIndex = 0, count = 10))
        skillFrames  = loadFrames(FrameConfig("fireWizard/fireball_pngs", "image_0-",  startIndex = 0, count = 8))

        // ── UI buttons ───────────────────────────────────────────────────────
        leftSlice         = safeLoad("ui/buttons/btn_left.png")
        rightSlice        = safeLoad("ui/buttons/btn_right.png")
        jumpSlice         = safeLoad("ui/buttons/btn_jump.png")
        attackSlice       = safeLoad("ui/buttons/btn_attack.png")
        skill1Slice       = safeLoad("skill_icons/fire_wizard/1.png")
        skill2Slice       = safeLoad("skill_icons/fire_wizard/2.png")
        skill3Slice       = safeLoad("skill_icons/fire_wizard/3.png")
        skill4Slice       = safeLoad("skill_icons/fire_wizard/4.png")
        healingRamenSlice = safeLoad("ui/icons/ramen.png")
        healingBentoSlice = safeLoad("ui/icons/bento.png")
        maxHealthSlice    = healthIconSlice
        pauseSlice        = safeLoad("ui/buttons/btn_menu.png")
        playSlice         = safeLoad("ui/buttons/btn_play.png")
        buttonBgSlice     = safeLoad("ui/buttons/button_bg.png")
        upgradeSlice      = safeLoad("ui/buttons/btn_upgrade.png")

        pauseResumeSlice  = buttonBgSlice
        pauseRestartSlice = buttonBgSlice
        pauseQuitSlice    = buttonBgSlice

        loaded = true
    }

    suspend fun loadFrames(cfg: FrameConfig): List<BmpSlice> {
        val key = buildKey(cfg)
        return frameCache.getOrPut(key) {
            try {
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
            } catch (e: Exception) {
                println("[GameAssets] loadFrames FAILED for ${cfg.folder}: ${e.message}")
                List(cfg.count) { fallbackSlice }
            }
        }
    }

    private fun buildKey(cfg: FrameConfig) =
        "${cfg.folder}|${cfg.prefix}|${cfg.startIndex}|${cfg.count}|${cfg.zeroPad}|${cfg.extension}|${cfg.sheet?.fileName ?: ""}|${cfg.sheet?.columns ?: 0}|${cfg.sheet?.rows ?: 0}"
}