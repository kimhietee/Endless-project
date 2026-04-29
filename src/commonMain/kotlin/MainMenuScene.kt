import korlibs.image.color.Colors
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.*
import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.io.async.launchImmediately


class MainMenuScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@MainMenuScene

        // -------------------------------------------------------
        // BACKGROUND  (placeholder — swap later)
        // -------------------------------------------------------
        val bgSlice = resourcesVfs["bg/background.png"].readBitmapSlice()
        image(bgSlice).apply {
            width  = Constants.SCREEN_WIDTH.toDouble()
            height = Constants.SCREEN_HEIGHT.toDouble()
        }

        // Semi-transparent overlay so the text pops
        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK).apply {
            alpha = 0.50
        }

        // -------------------------------------------------------
        // TITLE
        // -------------------------------------------------------
        text("Fighting Kimhie", textSize = 90.0, color = Colors.WHITE).apply {
            centerXOn(this@sceneMain)
            y = 180.0
        }

        // -------------------------------------------------------
        // PLAY BUTTON  →  MenuScene
        // -------------------------------------------------------
        val playSlice = resourcesVfs["ui/buttons/btn_play.png"].readBitmapSlice()
        val btnW = 240.0
        val btnH =  80.0
        val cx   = Constants.SCREEN_WIDTH / 2.0

        image(playSlice).apply {
            width  = btnW
            height = btnH
            x = cx - btnW / 2.0
            y = 420.0
            onOver  { alpha = 0.75 }
            onOut   { alpha = 1.00 }
            onClick { launchImmediately { this@MainMenuScene.sceneContainer.changeTo { GameScene() } } }
        }
    }
}
