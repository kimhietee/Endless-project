package managers

import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.image.font.Font
import korlibs.image.font.readFont
import utils.*

object GameAssets {

    private val frameCache = mutableMapOf<String, List<BmpSlice>>()
    private val bgCache = mutableMapOf<Int, BmpSlice>()
    var globalLoaded = false
        private set
    var loadedHeroId: String? = null
        private set

    // ── Individual named backgrounds ─────────────────────────────────────────
    lateinit var bgSlice:  BmpSlice
    lateinit var bg2Slice: BmpSlice
    lateinit var bg3Slice: BmpSlice


    // ── HUD bars ──────────────────────────────────────────────────────────────
    lateinit var hpBarGreenSlice:  BmpSlice
    lateinit var hpBarYellowSlice: BmpSlice
    lateinit var hpBarRedSlice:    BmpSlice
    lateinit var manaBarSlice:     BmpSlice

    // ── HUD icons ─────────────────────────────────────────────────────────────
    lateinit var healthIconSlice: BmpSlice
    lateinit var manaIconSlice:   BmpSlice
    lateinit var eyeIconSlice:   BmpSlice

    // ── Font ──────────────────────────────────────────────────────────────────
    lateinit var customFont: Font

    // ── Character animation frames ─────────────────────────────────────────────
    lateinit var idleFrames:   List<BmpSlice>
    lateinit var runFrames:    List<BmpSlice>
    lateinit var jumpFrames:   List<BmpSlice>
    lateinit var attackFrames: List<BmpSlice>
    lateinit var skillFrames:  List<BmpSlice>

    // ── Skill / attack frames (previously loaded inline inside GameScene) ──────
    // GameScene now reads these from here instead of calling loadFrames() itself.
    // This prevents the UninitializedPropertyAccessException that caused the
    // "background and ground only" crash on Android.
    lateinit var basicAtkFrames: List<BmpSlice>
    lateinit var skill1Frames:   List<BmpSlice>
    lateinit var skill2Frames:   List<BmpSlice>
    lateinit var skill3Frames:   List<BmpSlice>
    lateinit var skill4Frames:   List<BmpSlice>

    // ── UI button slices ───────────────────────────────────────────────────────
    lateinit var leftSlice:         BmpSlice
    lateinit var rightSlice:        BmpSlice
    lateinit var jumpSlice:         BmpSlice
    lateinit var attackSlice:       BmpSlice
    lateinit var skill1Slice:       BmpSlice
    lateinit var skill2Slice:       BmpSlice
    lateinit var skill3Slice:       BmpSlice
    lateinit var skill4Slice:       BmpSlice
    lateinit var healingRamenSlice: BmpSlice
    lateinit var healingBentoSlice: BmpSlice
    lateinit var maxHealthSlice:    BmpSlice
    lateinit var pauseSlice:        BmpSlice
    lateinit var playSlice:         BmpSlice
    lateinit var buttonBgSlice:     BmpSlice
    lateinit var upgradeSlice:      BmpSlice

    // ── Pause menu image buttons ───────────────────────────────────────────────
    // Swap the placeholder paths below for your actual asset filenames.
    lateinit var pauseResumeSlice:  BmpSlice
    lateinit var pauseRestartSlice: BmpSlice
    lateinit var pauseQuitSlice:    BmpSlice

    // ── Wanderer Magician (optional sheets under wandererMagician/) ────────────
    lateinit var wmIdleFrames:          List<BmpSlice>
    lateinit var wmRunFrames:         List<BmpSlice>
    lateinit var wmJumpFrames:        List<BmpSlice>
    lateinit var wmAttackFrames:      List<BmpSlice>
    lateinit var wmBasicProjectileFrames: List<BmpSlice>
    lateinit var wmSkill1Frames:      List<BmpSlice>
    lateinit var wmSkill2AuraFrames:  List<BmpSlice>
    lateinit var wmSkill3CastFrames:  List<BmpSlice>
    lateinit var wmSkill3ExplodeFrames: List<BmpSlice>
    lateinit var wmSkill4ChargeFrames: List<BmpSlice>
    lateinit var wmSkill4BallFrames:  List<BmpSlice>
    lateinit var wmSkill1Icon:        BmpSlice
    lateinit var wmSkill2Icon:        BmpSlice
    lateinit var wmSkill3Icon:        BmpSlice
    lateinit var wmSkill4Icon:        BmpSlice

    suspend fun load() {
        loadGlobal()
    }

    suspend fun loadGlobal() {
        if (globalLoaded) return

        // ── Named background shortcuts ─────────────────────────────────────────
        // We only load bgSlice and bg3Slice initially as they are used in menus
        bgSlice  = resourcesVfs["bg/background.png"].readBitmapSlice()
        bg2Slice = try { resourcesVfs["bg/background2.png"].readBitmapSlice() } catch(e: Exception) { bgSlice }
        bg3Slice = try { resourcesVfs["bg/background3.png"].readBitmapSlice() } catch(e: Exception) { bgSlice }

        // ── HUD bars ───────────────────────────────────────────────────────────
        hpBarGreenSlice  = resourcesVfs["ui/bar/green_health_bar.jpg"].readBitmapSlice()
        hpBarYellowSlice = resourcesVfs["ui/bar/yellow_health_bar.jpg"].readBitmapSlice()
        hpBarRedSlice    = resourcesVfs["ui/bar/red_health_bar.jpg"].readBitmapSlice()
        manaBarSlice     = resourcesVfs["ui/bar/mana_bar.jpg"].readBitmapSlice()

        // ── HUD icons ──────────────────────────────────────────────────────────
        healthIconSlice = resourcesVfs["ui/icons/heart.PNG"].readBitmapSlice()
        manaIconSlice   = resourcesVfs["ui/icons/potion.png"].readBitmapSlice()
        eyeIconSlice   = resourcesVfs["ui/icons/hidden.png"].readBitmapSlice()


        // ── Font ───────────────────────────────────────────────────────────────
        customFont = resourcesVfs["ui/font/slkscr.ttf"].readFont()

        // ── UI buttons ─────────────────────────────────────────────────────────
        leftSlice         = resourcesVfs["ui/buttons/btn_left.png"].readBitmapSlice()
        rightSlice        = resourcesVfs["ui/buttons/btn_right.png"].readBitmapSlice()
        jumpSlice         = resourcesVfs["ui/buttons/new_jump_icon.png"].readBitmapSlice()
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

        // ── Pause menu image buttons ────────────────────────────────────────────
        pauseResumeSlice  = try { resourcesVfs["ui/buttons/button_bg.png"].readBitmapSlice() } catch(e: Exception) { buttonBgSlice }
        pauseRestartSlice = try { resourcesVfs["ui/buttons/button_bg.png"].readBitmapSlice() } catch(e: Exception) { buttonBgSlice }
        pauseQuitSlice    = try { resourcesVfs["ui/buttons/button_bg.png"].readBitmapSlice() } catch(e: Exception) { buttonBgSlice }

        globalLoaded = true
    }

    suspend fun backgroundForWave(waveNumber: Int): BmpSlice {
        val index = (waveNumber - 1).coerceAtLeast(0) % 10 + 1 // assuming 10 bgs
        return bgCache.getOrPut(index) {
            try {
                if (index == 1) bgSlice
                else if (index == 3) bg3Slice
                else resourcesVfs["bg/background$index.png"].readBitmapSlice()
            } catch (e: Exception) {
                bgSlice
            }
        }
    }

    suspend fun loadHeroAssets(heroId: String) {
        if (loadedHeroId == heroId) return
        
        // Clear hero assets to free memory
        // Note: In a real app we might want to be more careful, but for simplicity:
        frameCache.clear()

        if (heroId == entities.heroes.FireWizardHero.ID) {
            // ── Fire Wizard animation frames ──────────────────────────────────────────
            idleFrames   = loadFrames(FrameConfig("fireWizard/idle_pngs",     "image_0-",  startIndex = 0, count = 7))
            runFrames    = loadFrames(FrameConfig("fireWizard/run_pngs",      "Run_",      startIndex = 0, count = 8))
            jumpFrames   = loadFrames(FrameConfig("fireWizard/jump_pngs",     "Jump_",     startIndex = 0, count = 6))
            attackFrames = loadFrames(FrameConfig("fireWizard/slash_pngs",    "Attack_1_", startIndex = 0, count = 10))
            skillFrames  = loadFrames(FrameConfig("fireWizard/fireball_pngs", "image_0-",  startIndex = 0, count = 8))

            // ── Skill / attack frames ───────────────────────────────────────────────
            basicAtkFrames = loadFrames(FrameConfig(
                folder = "fireWizard/skills/slash",
                sheet  = SpriteSheetConfig(fileName = "playerSlash", columns = 5, rows = 1),
                count  = 5
            ))
            skill1Frames = loadFrames(FrameConfig("fireWizard/skills/skill_1", "tile",  startIndex = 0, count = 12, zeroPad = 3))
            skill2Frames = loadFrames(FrameConfig("fireWizard/skills/skill_2", "",      startIndex = 0, count = 53, zeroPad = 2))
            skill3Frames = loadFrames(FrameConfig("fireWizard/skills/skill_3", "png_",  startIndex = 0, count = 34, zeroPad = 2))
            skill4Frames = loadFrames(FrameConfig("fireWizard/skills/skill_4", "",      startIndex = 0, count = 28, zeroPad = 2))
        } else if (heroId == entities.heroes.WandererMagicianHero.ID) {
            loadWandererMagicianAssets()
        }
        
        loadedHeroId = heroId
    }

    /**
     * Minimal load for HeroSelectionScene portraits.
     */
    suspend fun loadHeroPortraits() {
        if (!this::idleFrames.isInitialized) {
            idleFrames = loadFrames(FrameConfig("fireWizard/idle_pngs", "image_0-", startIndex = 0, count = 1))
        }
        if (!this::wmIdleFrames.isInitialized) {
            wmIdleFrames = loadFrames(FrameConfig(folder = "wandererMagician/idle_pngs", prefix = "", extension = "png", startIndex = 0, count = 1,
                sheet = SpriteSheetConfig(fileName = "Idle", columns = 8, rows = 1)))
        }
        if (!this::wmSkill1Icon.isInitialized) {
            wmSkill1Icon = try { resourcesVfs["skill_icons/wanderer_magician/1.jpeg"].readBitmapSlice() } catch(e: Exception) { skill1Slice }
            wmSkill2Icon = try { resourcesVfs["skill_icons/wanderer_magician/2.png"].readBitmapSlice() } catch(e: Exception) { skill2Slice }
            wmSkill3Icon = try { resourcesVfs["skill_icons/wanderer_magician/3.png"].readBitmapSlice() } catch(e: Exception) { skill3Slice }
            wmSkill4Icon = try { resourcesVfs["skill_icons/wanderer_magician/4.png"].readBitmapSlice() } catch(e: Exception) { skill4Slice }
        }
    }


    private suspend fun loadWandererMagicianAssets() {
        suspend fun sheet(folder: String, file: String, ext: String, cols: Int, rows: Int, count: Int) =
            loadFrames(FrameConfig(folder = folder, prefix = "", extension = ext, startIndex = 0, count = count,
                sheet = SpriteSheetConfig(fileName = file, columns = cols, rows = rows)))

        try {
            wmIdleFrames = sheet("wandererMagician/idle_pngs", "Idle", "png", 8, 1, 8)
            wmAttackFrames = sheet("wandererMagician/attack_pngs", "Attack_1", "png", 7, 1, 7)
            wmRunFrames = sheet("wandererMagician/run_pngs", "Run", "png", 8, 1, 8)
            wmJumpFrames = sheet("wandererMagician/jump_pngs", "Jump", "png", 8, 1, 8)
            wmBasicProjectileFrames = sheet("wandererMagician/skills/projectile", "Charge_2", "png", 6, 1, 6)
            wmSkill1Frames = sheet("wandererMagician/skills/skill_1", "Charge_1", "png", 9, 1, 9)
            wmSkill2AuraFrames = sheet("wandererMagician/skills/skill_2", "513", "PNG", 5, 10, 50)
            wmSkill3CastFrames = sheet("wandererMagician/explode_pngs", "Attack_2", "png", 9, 1, 9)
            wmSkill3ExplodeFrames = sheet("wandererMagician/skills/skill_3", "334", "PNG", 5, 7, 35)
            wmSkill4ChargeFrames = sheet("wandererMagician/charging_pngs", "Magic_sphere", "png", 16, 1, 16)
            wmSkill4BallFrames = listOf(
                resourcesVfs["wandererMagician/skills/skill_4/vv1.png"].readBitmapSlice(),
                resourcesVfs["wandererMagician/skills/skill_4/vv2.png"].readBitmapSlice(),
                resourcesVfs["wandererMagician/skills/skill_4/vv3.png"].readBitmapSlice()
            )
        } catch (e: Exception) {
            println("Wanderer Magician asset load failed, using Fire Wizard frames: ${e.message}")
            wmIdleFrames = idleFrames
            wmAttackFrames = attackFrames
            wmRunFrames = runFrames
            wmJumpFrames = jumpFrames
            wmBasicProjectileFrames = basicAtkFrames
            wmSkill1Frames = skill1Frames
            wmSkill2AuraFrames = skill2Frames
            wmSkill3CastFrames = skill3Frames
            wmSkill3ExplodeFrames = skill3Frames
            wmSkill4ChargeFrames = skillFrames
            wmSkill4BallFrames = skill4Frames
        }
        wmSkill1Icon = try { resourcesVfs["skill_icons/wanderer_magician/1.jpeg"].readBitmapSlice() } catch(e: Exception) { skill1Slice }
        wmSkill2Icon = try { resourcesVfs["skill_icons/wanderer_magician/2.png"].readBitmapSlice() } catch(e: Exception) { skill2Slice }
        wmSkill3Icon = try { resourcesVfs["skill_icons/wanderer_magician/3.png"].readBitmapSlice() } catch(e: Exception) { skill3Slice }
        wmSkill4Icon = try { resourcesVfs["skill_icons/wanderer_magician/4.png"].readBitmapSlice() } catch(e: Exception) { skill4Slice }
    }

    suspend fun loadFrames(cfg: FrameConfig): List<BmpSlice> {
        val key = buildKey(cfg)

        return frameCache.getOrPut(key) {
            val frames = mutableListOf<BmpSlice>()

            if (cfg.sheet != null) {
                try {
                    val sheetPath = "${cfg.folder}/${cfg.sheet.fileName}.${cfg.extension}"

                    val sheet = resourcesVfs[sheetPath].readBitmapSlice()

                    val frameWidth  = sheet.width  / cfg.sheet.columns
                    val frameHeight = sheet.height / cfg.sheet.rows

                    for (row in 0 until cfg.sheet.rows) {
                        for (col in 0 until cfg.sheet.columns) {
                            frames += sheet.sliceWithSize(
                                col * frameWidth,
                                row * frameHeight,
                                frameWidth,
                                frameHeight
                            )
                        }
                    }

                    frames.take(cfg.count)
                } catch (e: Exception) {
                    println("❌ FAILED to load sheet: ${e.message}")
                    emptyList()
                }
            } else {
                for (i in cfg.startIndex until cfg.startIndex + cfg.count) {
                    val index = if (cfg.zeroPad > 0)
                        i.toString().padStart(cfg.zeroPad, '0')
                    else i.toString()

                    val path = "${cfg.folder}/${cfg.prefix}$index.${cfg.extension}"

                    try {
                        val bmp = resourcesVfs[path].readBitmapSlice()
                        frames.add(bmp)
                    } catch (e: Exception) {
                        println("❌ Missing frame: $path")
                    }
                }

                frames
            }
        }
    }

    private fun buildKey(cfg: FrameConfig) =
        "${cfg.folder}|${cfg.prefix}|${cfg.startIndex}|${cfg.count}|${cfg.zeroPad}|${cfg.extension}|${cfg.sheet?.fileName ?: ""}|${cfg.sheet?.columns ?: 0}|${cfg.sheet?.rows ?: 0}"
}
