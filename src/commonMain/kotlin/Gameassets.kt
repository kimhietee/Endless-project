import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs

object GameAssets {

    lateinit var bgSlice:      BmpSlice
    lateinit var idleFrames:   List<BmpSlice>
    lateinit var runFrames:    List<BmpSlice>
    lateinit var jumpFrames:   List<BmpSlice>
    lateinit var attackFrames: List<BmpSlice>
    lateinit var skillFrames:  List<BmpSlice>

    suspend fun load() {
        bgSlice      = resourcesVfs["bg/background.png"].readBitmapSlice()
        idleFrames   = loadFrames("fireWizard", "idle_pngs",    "image_0-",  7)
        runFrames    = loadFrames("fireWizard", "run_pngs",      "Run_",      8)
        jumpFrames   = loadFrames("fireWizard", "jump_pngs",     "Jump_",     6)
        attackFrames = loadFrames("fireWizard", "slash_pngs",    "Attack_1_", 10)
        skillFrames  = loadFrames("fireWizard", "fireball_pngs", "image_0-",  8)
    }

    private suspend fun loadFrames(
        hero: String,
        folder: String,
        prefix: String,
        count: Int
    ): List<BmpSlice> = (0 until count).map { i ->
        resourcesVfs["$hero/$folder/${prefix}$i.png"].readBitmapSlice()
    }
}
