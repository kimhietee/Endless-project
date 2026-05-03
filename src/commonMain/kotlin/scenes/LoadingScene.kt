package scenes

import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.image.color.Colors
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.view.align.centerXOn
import korlibs.io.async.launchImmediately
import korlibs.time.*
import kotlinx.coroutines.delay
import korlibs.math.geom.*
import managers.GameAssets
import utils.Constants

class LoadingScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        val scene = this@LoadingScene

        val bgSlice = GameAssets.bg4Slice
        val bg = image(bgSlice).apply {
            width     = Constants.SCREEN_WIDTH.toDouble()
            height    = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
        }
        addChild(bg)

        // Loading text
        text("Loading...", textSize = 60.0, color = Colors.WHITE, font = GameAssets.customFont) {
            centerXOn(this@sceneMain)
            y = Constants.SCREEN_HEIGHT / 2.0 - 100.0
        }

        // Loading spinner icon (placeholder)
        val spinnerSlice = resourcesVfs["ui/buttons/loading_icon.png"].readBitmapSlice()
        val spinner = image(spinnerSlice) {
            width = 100.0
            height = 200.0
            x = Constants.SCREEN_WIDTH / 2.0 - 50.0
            y = Constants.SCREEN_HEIGHT / 2.0 + 20.0
        }

        // Animate the spinner
        addUpdater { dt ->
            spinner.rotation += (dt.seconds * 360.0).degrees  // rotate 360 degrees per second
        }

        // Preload all game assets
        GameAssets.load()

        // Wait a bit to ensure everything is ready
        delay(0.5.seconds)

        // Once loaded, transition to GameScene
        launchImmediately {
            scene.sceneContainer.changeTo { GameScene() }
        }
    }
}
