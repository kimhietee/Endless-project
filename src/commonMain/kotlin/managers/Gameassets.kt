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

    lateinit var bgSlice:      BmpSlice
    lateinit var bg2Slice:      BmpSlice
    lateinit var bg3Slice:      BmpSlice
    lateinit var bg4Slice:      BmpSlice

    lateinit var upgradeSlice:      BmpSlice

    // -------------------------------------------------------
    // HUD BAR IMAGES
    // -------------------------------------------------------
    lateinit var hpBarGreenSlice: BmpSlice
    lateinit var hpBarYellowSlice: BmpSlice
    lateinit var hpBarRedSlice: BmpSlice
    lateinit var manaBarSlice: BmpSlice
    
    // -------------------------------------------------------
    // HUD ICON IMAGES
    // -------------------------------------------------------
    lateinit var healthIconSlice: BmpSlice
    lateinit var manaIconSlice: BmpSlice

    // -------------------------------------------------------
    // CUSTOM FONT
    // -------------------------------------------------------
    lateinit var customFont: Font

    lateinit var idleFrames:   List<BmpSlice>
    lateinit var runFrames:    List<BmpSlice>
    lateinit var jumpFrames:   List<BmpSlice>
    lateinit var attackFrames: List<BmpSlice>
    lateinit var skillFrames:  List<BmpSlice>

    // Button assets
    lateinit var leftSlice:   BmpSlice
    lateinit var rightSlice:  BmpSlice
    lateinit var jumpSlice:   BmpSlice
    lateinit var attackSlice: BmpSlice
    lateinit var skill1Slice: BmpSlice
    lateinit var skill2Slice: BmpSlice
    lateinit var skill3Slice: BmpSlice
    lateinit var skill4Slice: BmpSlice
    lateinit var healingRamenSlice: BmpSlice
    lateinit var healingBentoSlice: BmpSlice
    lateinit var maxHealthSlice: BmpSlice
    lateinit var pauseSlice:  BmpSlice
    lateinit var playSlice:   BmpSlice
    lateinit var buttonBgSlice: BmpSlice

    suspend fun load() {
        if (loaded) return

        bgSlice      = resourcesVfs["bg/background.png"].readBitmapSlice()
        bg2Slice      = resourcesVfs["bg/background2.png"].readBitmapSlice()
        bg3Slice      = resourcesVfs["bg/background3.png"].readBitmapSlice()
        bg4Slice      = resourcesVfs["bg/background4.png"].readBitmapSlice()
        upgradeSlice = resourcesVfs["ui/buttons/btn_upgrade.png"].readBitmapSlice()

        // Load HUD bar images
        hpBarGreenSlice = resourcesVfs["ui/bar/green_health_bar.jpg"].readBitmapSlice()
        hpBarYellowSlice = resourcesVfs["ui/bar/yellow_health_bar.jpg"].readBitmapSlice()
        hpBarRedSlice = resourcesVfs["ui/bar/red_health_bar.jpg"].readBitmapSlice()
        manaBarSlice = resourcesVfs["ui/bar/mana_bar.jpg"].readBitmapSlice()

        // Load HUD icon images
        healthIconSlice = resourcesVfs["ui/icons/heart.PNG"].readBitmapSlice()
        manaIconSlice = resourcesVfs["ui/icons/potion.png"].readBitmapSlice()

        // Load custom font
        customFont = resourcesVfs["ui/font/slkscr.ttf"].readFont()

        idleFrames   = loadFrames(FrameConfig("fireWizard/idle_pngs", "image_0-", startIndex = 0, count = 7))
        runFrames    = loadFrames(FrameConfig("fireWizard/run_pngs",   "Run_",     startIndex = 0, count = 8))
        jumpFrames   = loadFrames(FrameConfig("fireWizard/jump_pngs",  "Jump_",    startIndex = 0, count = 6))
        attackFrames = loadFrames(FrameConfig("fireWizard/slash_pngs", "Attack_1_",startIndex = 0, count = 10))
        skillFrames  = loadFrames(FrameConfig("fireWizard/fireball_pngs", "image_0-", startIndex = 0, count = 8))

        // Preload enemy assets that are reused across multiple enemies
        loadFrames(FrameConfig("fireWizard/idle_pngs",  "image_0-",  startIndex = 0, count = 7))
        loadFrames(FrameConfig("fireWizard/run_pngs",   "Run_",      startIndex = 0, count = 8))
        loadFrames(FrameConfig("fireWizard/slash_pngs", "Attack_1_", startIndex = 0, count = 10))
        loadFrames(FrameConfig("fireWizard/fireball_pngs", "image_0-", startIndex = 0, count = 8))
        loadFrames(FrameConfig("fireWizard/jump_pngs",  "Jump_",     startIndex = 0, count = 6))
//        loadFrames(FrameConfig("skeleton_enemy", sheet = SpriteSheetConfig("skeleton_run", columns = 10, rows = 1), count = 10))

        // Load button assets
        leftSlice   = resourcesVfs["ui/buttons/btn_left.png"].readBitmapSlice()
        rightSlice  = resourcesVfs["ui/buttons/btn_right.png"].readBitmapSlice()
        jumpSlice   = resourcesVfs["ui/buttons/btn_jump.png"].readBitmapSlice()
        attackSlice = resourcesVfs["ui/buttons/btn_attack.png"].readBitmapSlice()
        skill1Slice = resourcesVfs["skill_icons/fire_wizard/1.png"].readBitmapSlice()
        skill2Slice = resourcesVfs["skill_icons/fire_wizard/2.png"].readBitmapSlice()
        skill3Slice = resourcesVfs["skill_icons/fire_wizard/3.png"].readBitmapSlice()
        skill4Slice = resourcesVfs["skill_icons/fire_wizard/4.png"].readBitmapSlice()
        healingRamenSlice = resourcesVfs["ui/icons/ramen.png"].readBitmapSlice()
        healingBentoSlice = resourcesVfs["ui/icons/bento.png"].readBitmapSlice()
        maxHealthSlice = resourcesVfs["ui/icons/heart.PNG"].readBitmapSlice()
        pauseSlice  = resourcesVfs["ui/buttons/btn_menu.png"].readBitmapSlice()
        playSlice   = resourcesVfs["ui/buttons/btn_play.png"].readBitmapSlice()
        buttonBgSlice = resourcesVfs["ui/buttons/button_bg.png"].readBitmapSlice()

        loaded = true
    }

    suspend fun loadFrames(cfg: FrameConfig): List<BmpSlice> {
        val key = buildKey(cfg)
        return frameCache.getOrPut(key) {
            if (cfg.sheet != null) {
                val sheetPath = "${cfg.folder}/${cfg.sheet.fileName}.${cfg.extension}"
                val sheet = resourcesVfs[sheetPath].readBitmapSlice()
                val frames = mutableListOf<BmpSlice>()
                val frameWidth = sheet.width / cfg.sheet.columns
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
