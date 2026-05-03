package scenes

import korlibs.image.color.Colors
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.io.async.launchImmediately
import korlibs.korge.input.*
import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import managers.GameAssets
import utils.Constants
import utils.AttackDisplay

class MenuScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@MenuScene

        // -------------------------------------------------------
        // BACKGROUND (placeholder — swap menu_bg.png when ready)
        // -------------------------------------------------------
//        val bg = resourcesVfs["bg/background.png"].readBitmapSlice()
//        image(bg) {
//            width  = Constants.SCREEN_WIDTH.toDouble()
//            height = Constants.SCREEN_HEIGHT.toDouble()
//        }
        val bgSlice = GameAssets.bg3Slice
        val bg = image(bgSlice).apply {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }
        addChild(bg)

        solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT.toDouble(), Colors.BLACK) {
            alpha = 0.4
        }

        // -------------------------------------------------------
        // TITLE
        // -------------------------------------------------------
        text("GAME MENU", textSize = 70.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 150.0
        }

        val btnW = 260.0
        val btnH =  80.0
        val cx   = Constants.SCREEN_WIDTH / 2.0

        // -------------------------------------------------------
        // START BUTTON → LoadingScene → GameScene
        // -------------------------------------------------------
        solidRect(btnW, btnH, Colors.DARKGREEN) {
            x = cx - btnW / 2
            y = 350.0
            onOver { alpha = 0.7 }
            onOut  { alpha = 1.0 }
            onClick { launchImmediately { scene.sceneContainer.changeTo { LoadingScene() } } }
        }
        text("START", textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 350.0 + (btnH - fontSize) / 2
        }

        // -------------------------------------------------------
        // SETTINGS BUTTON → SettingsScene
        // -------------------------------------------------------
        solidRect(btnW, btnH, Colors.DARKBLUE) {
            x = cx - btnW / 2
            y = 470.0
            onOver { alpha = 0.7 }
            onOut  { alpha = 1.0 }
            onClick { launchImmediately { scene.sceneContainer.changeTo { SettingsScene() } } }
        }
        text("SETTINGS", textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 470.0 + (btnH - fontSize) / 2
        }

        // -------------------------------------------------------
        // BACK BUTTON → MainMenuScene
        // -------------------------------------------------------
        solidRect(btnW, btnH, Colors.DARKRED) {
            x = cx - btnW / 2
            y = 590.0
            onOver { alpha = 0.7 }
            onOut  { alpha = 1.0 }
            onClick { launchImmediately { scene.sceneContainer.changeTo { MainMenuScene() } } }
        }
        text("BACK", textSize = 28.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = 590.0 + (btnH - fontSize) / 2
        }
    }
}